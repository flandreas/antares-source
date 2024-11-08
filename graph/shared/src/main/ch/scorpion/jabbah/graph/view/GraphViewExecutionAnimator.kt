package ch.scorpion.jabbah.graph.view

import ch.scorpion.jabbah.animation.AnimationModule
import ch.scorpion.jabbah.animation.AnimationTask
import ch.scorpion.jabbah.animation.AnimationTaskAdapter
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.time.SystemSpeed
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.drawable.SynchronizedGlowAnimation
import ch.scorpion.jabbah.draw.drawable.Transparent
import ch.scorpion.jabbah.draw.drawable.TransparentAnimation
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.execution.actor.Actor
import ch.scorpion.jabbah.execution.actor.ActorData
import ch.scorpion.jabbah.execution.actor.ActorListener
import ch.scorpion.jabbah.execution.scheduler.*
import ch.scorpion.jabbah.execution.speed.SystemSpeedCategory
import ch.scorpion.jabbah.execution.speed.SystemSpeedCategoryEvent
import ch.scorpion.jabbah.graph.GraphApplicationContextHolder
import ch.scorpion.jabbah.graph.model.*
import ch.scorpion.jabbah.graph.view.module.GraphViewModule

/**
 * Performs animations of [GraphElementView] execution and signal flow across [EdgeView]s.
 *
 * [GraphViewExecutionAnimator] registers itself as [ActorListener] on all [GraphElement]s of the current [Graph].
 * Since this happens only for visible [GraphView]s, soft breakpoints are only active for displayed [Graph]s.
 *
 * A [GraphViewExecutionAnimator] is active if either the current [GraphViewAnimationType] is [GraphViewAnimationType.Animation],
 * which requires signal flow animation, or if [SystemSpeed.isPaused], which requires [TransparentAnimation]
 * of executing [GraphElement]s.
 *
 * This class should be part of the [ch.scorpion.jabbah.graph] module, but classes needed for signal flow animation
 * have not yet been generalized.
 *
 * @param actorListener the [ActorListener] that has been registered with the [Actor]s
 */
class GraphViewExecutionAnimator(
	private val actorListener: ActorListener,
	private val drawingView: DrawingView<GraphView>,
	private val applicationContextHolder: GraphApplicationContextHolder,
	private val animationFactory: GraphViewExecutionAnimationFactory = GraphViewModule.graphViewExecutionAnimationFactory,
	private val eventBus: EventBus = BaseModule.eventBus,
	private val styleProvider: StyleProvider = DrawStyleModule.styleProvider
) {

	companion object {
		private val LOG by logger(GraphViewExecutionAnimator::class)
	}

	data class NetAnimationData(
		val actorData: ActorData,
		val animations: MutableList<EdgeViewNetAnimation> = mutableListOf()
	)

	/**
	 * Maps a [Net] to all [EdgeViewNetAnimation]s currently running on it. Note that there can be
	 * more than one animation for the same [Net] if multiple [OutputPort]s assert their startup values to the same bus.
	 */
	private val netAnimationMap = mutableMapOf<Net<*>, NetAnimationData>()

	/** Listens for changes of the [SchedulerActivationState].*/
	private val schedulerActivationStateHandler: EventHandler<SchedulerActivationStateEvent> = {
		if (it.scheduler === applicationContextHolder.scheduler) {
			if (!it.scheduler.isActive) {
				// TODO Should stop only animations related to this GraphViewAnimator
				applicationContextHolder.animator.stopAllTasks()
				stopAllVerticeViewActingAnimations()
				netAnimationMap.clear()
			}
		}
	}

	private val systemSpeedCategoryHandler: EventHandler<SystemSpeedCategoryEvent> = {
		if (applicationContextHolder.scheduler.isActive && it.source === applicationContextHolder.currentSystemSpeedCategory) {
			if (it.oldValue == SystemSpeedCategory.Explore && it.newValue.ordinal < it.oldValue.ordinal) {
				stopAllVerticeViewActingAnimations()
				interruptAllNetActingAnimations()
			}
		}
	}

	private val schedulerSingleStepModeHandler: EventHandler<SchedulerSingleStepModeEvent> = {
		if (it.scheduler === applicationContextHolder.scheduler) {
			stopAllVerticeViewActingAnimations()
		}
	}

	init {
		eventBus.register(SchedulerActivationStateEvent::class, schedulerActivationStateHandler)
		eventBus.register(SchedulerSingleStepModeEvent::class, schedulerSingleStepModeHandler)
		eventBus.register(SystemSpeedCategoryEvent::class, systemSpeedCategoryHandler)
	}

	fun dispose() {
		eventBus.unregister(schedulerActivationStateHandler)
		eventBus.unregister(schedulerSingleStepModeHandler)
		eventBus.unregister(systemSpeedCategoryHandler)
	}

	fun actingRequested(actor: Actor, data: ActorData) {
		if (actor is Net<*>) {
			handleNetActingRequested(actor, data as GraphActorData)
		} else if ((data as GraphActorData).isInput) {
			handleGraphElementActingRequested(actor as GraphElement)
		}
	}

	fun acted(actor: Actor, signalHandler: SignalHandler, data: GraphActorData) {
		if (actor is Net<*>) {
			handleNetActed(actor, data)
		} else {
			handleGraphElementActed(actor as GraphElement)
			actor.actingVisualized(signalHandler, actorListener, data)
		}
	}

	private fun handleNetActingRequested(net: Net<*>, actorData: GraphActorData) {
		// The acting of a Net has been requested, because an Output of a Vertice has asserted
		// a signal onto the net (which is still buffered in the Net and not yet forwarded).
		// Setup an animation of the signal that will flow along the corresponding EdgeView,
		// if requested by current settings.

		applicationContextHolder.scheduler.logActorTrace(net) { "handleNetActingRequested" }

		val changedPort = actorData.immediatePort!!

		if (!requireEdgeViewAnimation(net)) {
			return
		}

		val edgeView = drawingView.drawing.getEdgeView(changedPort)
			?: return

		val signal = edgeView.model.signalBuffer

		changedPort.captureTemporarySignal()

		// Creating the EdgeViewNetAnimation will make it visible in the View, but the animation
		// is not started yet. The outgoing EdgeAnimationView waits at the OutputPortView
		// until the scheduling slot is scheduled by the Scheduler, which will be notified
		// by receiving a SchedulerEvent.
		//
		// Note that it is the responsibility of the EdgeViewNetAnimation to continue
		// the simulation by calling SignalHandler#actingDone() for the Net after the animation
		// has finished.

		registerAnimation(
			net,
			actorData,
			animationFactory.createEdgeViewNetAnimation(
				actorListener = actorListener,
				actorData = actorData,
				startEdgeView = edgeView,
				startPort = changedPort,
				drawingView = drawingView,
				animator = applicationContextHolder.animator,
				scheduler = applicationContextHolder.scheduler,
				styleProvider = styleProvider)
		)

		EditModule.attentionDrawerFactory.invoke(signal).drawAttentionTo(
			edgeView.getConnectionEndpointType(edgeView.getConnection(changedPort)!!)!!.getLocation(edgeView),
			drawingView,
			AnimationModule.constantSpeedAnimator
		)

		applicationContextHolder.scheduler.logActorTrace(edgeView.model) { "Registered EdgeView animation for EdgeView '${edgeView.id}'" }
	}

	private fun handleNetActed(net: Net<*>, data: GraphActorData) {
		// The simulation of a Net has been scheduled by the Scheduler. Lookup all pending net animations
		// and start them. If there are no pending net animations, the acted Net belongs to a SubVertice
		// whose views are not displayed by the GraphView managed by this GraphViewAnimator.

		applicationContextHolder.scheduler.logActorTrace(net) { "handleNetActed" }

		if (!requireEdgeViewAnimation(net)) {
			net.actingVisualized(applicationContextHolder.scheduler, actorListener, data)
			return
		}

		netAnimationMap[net]?.animations?.forEach {
			applicationContextHolder.scheduler.logActorTrace(net) { "Starting EdgeViewNetAnimation" }
			val task = it.start()
			task.addListener(object : AnimationTaskAdapter() {
				override fun ended(task: AnimationTask) {
					data.changedPort?.resetTemporarySignal()
					unregisterAnimation(net, it)
				}
			})
		}
	}

	private fun handleGraphElementActingRequested(graphElement: GraphElement) {
		applicationContextHolder.scheduler.logActorTrace(graphElement) { "handleGraphElementActingRequested" }
		if (requireVerticeViewGlowAnimation()) {
			startVerticeViewActingAnimation(graphElement)
		}
	}

	private fun handleGraphElementActed(graphElement: GraphElement) {
		applicationContextHolder.scheduler.logActorTrace(graphElement) { "handleGraphElementActed" }
		if (requireVerticeViewGlowAnimation()) {
			stopVerticeViewActingAnimation(graphElement)
		}
	}

	/**
	 * Start an animation indicating that acting has been requested and this [Actor] waits to be scheduled
	 * by the [Scheduler]
	 */
	private fun startVerticeViewActingAnimation(graphElement: GraphElement) {
		val elementViews = drawingView.drawing.getElementViews(graphElement)
		if (elementViews.size == 1 && elementViews[0] is VerticeView) {
			val verticeView = elementViews[0] as VerticeView<*>
			if (verticeView is Transparent) {
				LOG.trace("Start VerticeView acting animation on ${verticeView::class.simpleName} with ID ${verticeView.id}")
				SynchronizedGlowAnimation.add(verticeView)
			}
		}
	}

	private fun stopVerticeViewActingAnimation(graphElement: GraphElement) {
		val elementViews = drawingView.drawing.getElementViews(graphElement)
		if (elementViews.size == 1 && elementViews[0] is Transparent) {
			val verticeView = elementViews[0] as VerticeView<*>
			if (verticeView is Transparent) {
				SynchronizedGlowAnimation.remove(verticeView)
			}
		}
	}

	private fun stopAllVerticeViewActingAnimations() {
		SynchronizedGlowAnimation.removeAll()
	}

	private fun interruptAllNetActingAnimations() {
		netAnimationMap.keys.forEach { net ->
			net.actingVisualized(applicationContextHolder.scheduler, actorListener, netAnimationMap[net]!!.actorData)
		}
	}

	private fun registerAnimation(net: Net<*>, actorData: ActorData, animation: EdgeViewNetAnimation) {
		applicationContextHolder.scheduler.logActorTrace(net) { "register net animation" }
		netAnimationMap.getOrPut(net) { NetAnimationData(actorData) }.animations.add(animation)
	}

	private fun unregisterAnimation(net: Net<*>, animation: EdgeViewNetAnimation) {
		applicationContextHolder.scheduler.logActorTrace(net) { "unregister net animation" }
		netAnimationMap[net]?.animations?.remove(animation)
	}

	/** Determines whether [EdgeViewNetAnimation] is required based on the current system settings.*/
	private fun requireEdgeViewAnimation(net: Net<*>): Boolean =
		applicationContextHolder.scheduler.executionTime > (drawingView.drawing.graph!!.startupTime ?: 0)
			&& applicationContextHolder.currentSystemSpeedCategory.systemSpeedCategory == SystemSpeedCategory.Explore
			&& SignalUtil.differ(net.signal, net.signalBuffer)

	/** Determines whether an animation is to be shown while [VerticeView]s are calculating. */
	private fun requireVerticeViewGlowAnimation(): Boolean = applicationContextHolder.scheduler.isSingleStepMode
}