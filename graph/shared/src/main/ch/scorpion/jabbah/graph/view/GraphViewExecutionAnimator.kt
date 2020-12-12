package ch.scorpion.jabbah.graph.view

import ch.scorpion.jabbah.animation.AnimationModule
import ch.scorpion.jabbah.animation.AnimationTask
import ch.scorpion.jabbah.animation.AnimationTaskAdapter
import ch.scorpion.jabbah.animation.Animator
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.module.BaseModule
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
import ch.scorpion.jabbah.execution.module.ExecutionModule
import ch.scorpion.jabbah.execution.scheduler.Scheduler
import ch.scorpion.jabbah.execution.scheduler.SchedulerActivationState
import ch.scorpion.jabbah.execution.scheduler.SchedulerActivationStateEvent
import ch.scorpion.jabbah.execution.scheduler.SchedulerRunningState
import ch.scorpion.jabbah.execution.speed.CurrentSystemSpeedCategory
import ch.scorpion.jabbah.execution.speed.SystemSpeedCategory
import ch.scorpion.jabbah.graph.model.*
import ch.scorpion.jabbah.graph.view.module.GraphViewModule

/**
 * Performs animations of [GraphElementView] execution and signal flow across [EdgeView]s.
 *
 * [GraphViewExecutionAnimator] registers itself as [ActorListener] on all [GraphElement]s of the current [Graph].
 * Since this happens only for visible [GraphView]s, soft breakpoints are only active for displayed [Graph]s.
 *
 * A [GraphViewExecutionAnimator] is active if either the current [GraphViewAnimationType] is [GraphViewAnimationType.Animation],
 * which requires signal flow animation, or if the [Scheduler]'s [SchedulerRunningState] is [SchedulerRunningState.PAUSED],
 * which requires [TransparentAnimation] of executing [GraphElement]s.
 *
 * This class should be part of the [ch.scorpion.jabbah.graph] module, but classes needed for signal flow animation
 * have not yet been generalized.
 *
 * @param actorListener the [ActorListener] that has been registered with the [Actor]s
 */
class GraphViewExecutionAnimator(
	private val actorListener: ActorListener,
	private val drawingView: DrawingView<GraphView>,
	private val animationFactory: GraphViewExecutionAnimationFactory = GraphViewModule.graphViewExecutionAnimationFactory,
	private val scheduler: Scheduler = ExecutionModule.scheduler,
	private val animator: Animator = AnimationModule.animator,
	private val systemSpeedCategory: CurrentSystemSpeedCategory = ExecutionModule.currentSystemSpeedCategory,
	private val eventBus: EventBus = BaseModule.eventBus,
	private val styleProvider: StyleProvider = DrawStyleModule.styleProvider
) {

	/**
	 * Maps a [Net] to all [EdgeViewNetAnimation]s currently running on it. Note that there can be
	 * more than one animation for the same [Net] if multiple [OutputPort]s assert their startup values to the same bus.
	 */
	private val netAnimationMap = mutableMapOf<Net<*>, MutableList<EdgeViewNetAnimation>>()

	/** Listens for changes of the [SchedulerActivationState].*/
	private val schedulerActivationStateHandler: EventHandler<SchedulerActivationStateEvent> = {
		if (!it.scheduler.isActive) {
			// TODO Should stop only animations related to this GraphViewAnimator
			animator.stopAllTasks()
			netAnimationMap.clear()
		}
	}

	init {
		eventBus.register(SchedulerActivationStateEvent::class, schedulerActivationStateHandler)
	}

	fun dispose() {
		eventBus.unregister(SchedulerActivationStateEvent::class, schedulerActivationStateHandler)
	}

	fun actingRequested(actor: Actor, signalHandler: SignalHandler, data: ActorData) {
		if (actor is Net<*>) {
			handleNetActingRequested(actor, data as GraphActorData)
		} else if ((data as GraphActorData).isInput) {
			handleGraphElementActingRequested(actor as GraphElement)
		}
	}

	fun acted(actor: Actor, signalHandler: SignalHandler, data: ActorData) {
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
		// Setup a animation of the signal that will flow along the corresponding EdgeView,
		// if requested by current settings.

		scheduler.logActorTrace(net) { "handleNetActingRequested" }

		val changedPort = actorData.changedPort!!

		if (!requireEdgeViewAnimation()) {
			return
		}

		val edgeView = drawingView.drawing.getEdgeView(changedPort)!!
		val signal = edgeView.model.signalBuffer

		// Creating the EdgeViewNetAnimation will make it visible in the View, but the animation
		// is not started yet. The outgoing EdgeAnimationView waits at the OutputPortView
		// until the scheduling slot is scheduled by the Scheduler, which will be notified
		// by receiving a SchedulerEvent.
		//
		// Note that it is the responsibility of the EdgeViewNetAnimation to continue
		// the simulation by calling SignalHandler#actingDone() for the Net after the animation
		// has finished.

		registerAnimation(net, animationFactory.createEdgeViewNetAnimation(
			actorListener = actorListener,
			actorData = actorData,
			startEdgeView = edgeView,
			startPort = changedPort,
			drawingView = drawingView,
			animator = animator,
			scheduler = scheduler,
			styleProvider = styleProvider
		))

		EditModule.attentionDrawerFactory.invoke(signal).drawAttentionTo(
			edgeView.getConnectionEndpointType(edgeView.getConnection(changedPort)!!)!!.getLocation(edgeView),
			drawingView,
			animator
		)

		scheduler.logActorTrace(edgeView.model) { "Registered EdgeView animation for EdgeView '${edgeView.id}'" }
	}

	private fun handleNetActed(net: Net<*>, data: ActorData) {
		// The simulation of a Net has been scheduled by the Scheduler. Lookup all pending net animations
		// and start them. If there are no pending net animations, the acted Net belongs to a SubVertice
		// whose views are not displayed by the GraphView managed by this GraphViewAnimator.

		scheduler.logActorTrace(net) { "handleNetActed" }

		if (!requireEdgeViewAnimation()) {
			net.actingVisualized(scheduler, actorListener, data)
			return
		}

		netAnimationMap[net]?.forEach {
			scheduler.logActorTrace(net) { "Starting EdgeViewNetAnimation" }
			val task = it.start()
			task.addListener(object : AnimationTaskAdapter() {
				override fun ended(task: AnimationTask) {
					unregisterAnimation(net, it)
				}
			})
		}
	}

	private fun handleGraphElementActingRequested(graphElement: GraphElement) {
		scheduler.logActorTrace(graphElement) { "handleGraphElementActingRequested" }

		if (!requireVerticeGlowAnimation()) {
			return
		}
		if (graphElement.propagationDelay == 0L) {
			return
		}

		val elementViews = drawingView.drawing.getElementViews(graphElement)

		// Start an animation indicating that acting has been requested and this Actor waits to be scheduled
		// by the Scheduler
		if (elementViews.size == 1 && elementViews[0] is VerticeView) {
			animationFactory.createVerticeViewActingAnimation(elementViews[0] as VerticeView<*>)?.let {
				animator.schedule(it)
				it.start()
			}
		}
	}

	private fun handleGraphElementActed(graphElement: GraphElement) {
		scheduler.logActorTrace(graphElement) { "handleGraphElementActed" }
		if (!requireVerticeGlowAnimation()) {
			return
		}
		val elementViews = drawingView.drawing.getElementViews(graphElement)
		// Stop VerticeView animation, if any
		if (elementViews.size == 1 && elementViews[0] is Transparent) {
			animator.getTasksForTarget(elementViews[0]).forEach { it.stop() }
		}
	}

	private fun registerAnimation(net: Net<*>, animation: EdgeViewNetAnimation) {
		scheduler.logActorTrace(net) { "register net animation" }
		netAnimationMap.getOrPut(net) { mutableListOf() }.add(animation)
	}

	private fun unregisterAnimation(net: Net<*>, animation: EdgeViewNetAnimation) {
		scheduler.logActorTrace(net) { "unregister net animation" }
		netAnimationMap[net]?.remove(animation)
	}

	/** Determines whether [EdgeViewNetAnimation] is required based on the current system settings.*/
	private fun requireEdgeViewAnimation(): Boolean {
		return systemSpeedCategory.systemSpeedCategory == SystemSpeedCategory.Explore
	}

	/** Determines whether an animation is to be shown while [VerticeView]s are calculating. */
	private fun requireVerticeGlowAnimation(): Boolean {
		return scheduler.isPaused
	}
}