package ch.scorpion.antares.view

import ch.scorpion.antares.model.port.DigitalPort
import ch.scorpion.antares.view.net.DigitalEdgeView
import ch.scorpion.antares.view.net.DigitalEdgeViewNetAnimation
import ch.scorpion.jabbah.animation.AnimationModule
import ch.scorpion.jabbah.animation.AnimationTask
import ch.scorpion.jabbah.animation.AnimationTaskAdapter
import ch.scorpion.jabbah.animation.Animator
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.event.PropertyChangeEvent
import ch.scorpion.jabbah.base.event.PropertyChangeListener
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.DrawableContainerEvent
import ch.scorpion.jabbah.draw.container.DrawableContainerAdapter
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
import ch.scorpion.jabbah.execution.speed.SystemSpeedCategoryEvent
import ch.scorpion.jabbah.graph.model.*
import ch.scorpion.jabbah.graph.view.*
import ch.scorpion.jabbah.graph.view.module.GraphViewModule

/**
 * Performs animations of [GraphElementView] execution and signal flow across [EdgeView]s.
 *
 * [GraphViewAnimator] registers itself as [ActorListener] on an [GraphElement]s of the current [Graph].
 * Since this happens only for visible [GraphView]s,
 *
 * A [GraphViewAnimator] is active if either the current [GraphViewAnimationType] is [GraphViewAnimationType.Animation],
 * which requires signal flow animation, or if the [Scheduler]'s [SchedulerRunningState] is [SchedulerRunningState.PAUSED],
 * which requires [TransparentAnimation] of executing [GraphElement]s.
 */
class GraphViewAnimator(
	private val drawingView: DrawingView<GraphView<GraphElementView<*>>>,
	private val scheduler: Scheduler = ExecutionModule.scheduler,
	private val animator: Animator = AnimationModule.animator,
	private val systemSpeedCategory: CurrentSystemSpeedCategory = ExecutionModule.currentSystemSpeedCategory,
	val eventBus: EventBus = BaseModule.eventBus,
	private val currentGraphAnimationType: CurrentGraphViewAnimationType = GraphViewModule.currentGraphViewAnimationType,
	val styleProvider: StyleProvider = DrawStyleModule.styleProvider
) : ActorListener {

	companion object {
		private val LOG by logger(GraphViewAnimator::class)
	}

	/**
	 * Maps a [Net] to all [DigitalEdgeViewNetAnimation]s currently running on it. Note that there can be
	 * more than one animation for the same [Net] if multiple [OutputPort]s assert their startup values to the same bus.
	 */
	private val netAnimationMap = mutableMapOf<Net<*>, MutableList<DigitalEdgeViewNetAnimation>>()

	/**
	 * Listens for add/remove of [GraphElementView]s in order to add/remove this [GraphViewAnimator] as
	 * [ActorListener].
	 */
	private val graphViewListener = GraphViewListener()

	private var isActive: Boolean = calculateIsActive()
		set(value) {
			if (field == value) {
				return
			}
			if (value) {
				registerActorListener(drawingView.drawing.graph!!)
			} else {
				unregisterActorListener(drawingView.drawing.graph!!)
			}
			field = value
		}

	/** Listens for changes of the [CurrentGraphViewAnimationType].*/
	private val currentGraphViewAnimationTypeHandler: EventHandler<CurrentGraphAnimationTypeEvent> = {
		isActive = calculateIsActive()
	}

	private val systemSpeedCategoryHandler: EventHandler<SystemSpeedCategoryEvent> = {
		isActive = calculateIsActive()
	}

	/** Listens for changes of the [SchedulerActivationState].*/
	private val schedulerActivationStateHandler: EventHandler<SchedulerActivationStateEvent> = {
		if (!it.scheduler.isActive) {
			// TODO Should stop only animations related to this GraphViewAnimator
			animator.stopAllTasks()
			netAnimationMap.clear()
		}
		isActive = calculateIsActive()
	}

	private fun calculateIsActive(): Boolean {
		return scheduler.isActive && (requireEdgeViewAnimation() || requireVerticeGlowAnimation())
	}

	init {
		eventBus.register(CurrentGraphAnimationTypeEvent::class, currentGraphViewAnimationTypeHandler)
		eventBus.register(SchedulerActivationStateEvent::class, schedulerActivationStateHandler)
		eventBus.register(SystemSpeedCategoryEvent::class, systemSpeedCategoryHandler)

		// Listen for exchanges of the current GraphView in order to be an ActorListener
		// on all GraphElements of its Graph
		drawingView.addPropertyChangeListener(object : PropertyChangeListener<Any> {
			override fun propertyChanged(e: PropertyChangeEvent<Any>) {
				if (isActive && e.name == DrawingView.PROP_DRAWING) {
					handleGraphViewChanged(e.oldValue as GraphView<GraphElementView<*>>, e.newValue as GraphView<GraphElementView<*>>)
				}
			}
		})

		if (isActive) {
			registerActorListener(drawingView.drawing.graph!!)
		}
	}

	/** ---- [ActorListener] interface */

	override fun actingRequested(actor: Actor, signalHandler: SignalHandler, data: ActorData) {
		if (actor is Net<*>) {
			handleNetActingRequested(actor, data as GraphActorData)
		} else if ((data as GraphActorData).isInput) {
			handleGraphElementActingRequested(actor as GraphElement)
		}
	}

	override fun acted(actor: Actor, signalHandler: SignalHandler, data: ActorData) {
		if (actor is Net<*>) {
			handleNetActed(actor, data)
		} else {
			handleGraphElementActed(actor as GraphElement)
			actor.actingVisualized(signalHandler, this, data)
		}
	}

	private fun handleNetActingRequested(net: Net<*>, actorData: GraphActorData) {
		// The acting of a Net has been requested, because an Output of a Vertice has asserted
		// a signal onto the net (which is still buffered in the Net and not yet forwarded).
		// Setup a animation of the signal that will flow along the corresponding EdgeView,
		// if requested by current settings.

		scheduler.logActorTrace(net) { "handleNetActingRequested"}

		val changedPort: DigitalPort = actorData.changedPort as DigitalPort

		if (!requireEdgeViewAnimation()) {
			return
		}

		val edgeView = drawingView.drawing.getEdgeView(changedPort) as DigitalEdgeView
		val signal = edgeView.model.signalBuffer

		// Creating the DigitalEdgeViewNetAnimation will make it visible in the View, but the animation
		// is not started yet. The outgoing DigitalEdgeAnimationView waits at the OutputPortView
		// until the scheduling slot is scheduled by the Scheduler, which will be notified
		// by receiving a SchedulerEvent.
		//
		// Note that it is the responsibility of the DigitalEdgeViewNetAnimation to continue
		// the simulation by calling SignalHandler#actingDone() for the Net after the animation
		// has finished.

		registerAnimation(net, DigitalEdgeViewNetAnimation(
			actorListener = this,
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
			net.actingVisualized(scheduler, this, data)
			return
		}

		netAnimationMap[net]?.forEach {
			scheduler.logActorTrace(net) { "Starting DigitalEdgeViewNetAnimation" }
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

		// If the ElementView that displays the Actor for which an acting has been requested
		// supports the Transparent interface, we start a TransparentAnimation that indicates that an acting
		// has been requested and this Actor waits to be scheduled by the Scheduler.

		if (elementViews.size == 1 && elementViews[0] is Transparent) {
			LOG.debug("start glow animation")
			val transparent = elementViews[0] as Transparent
			val glowAnimation = TransparentAnimation.glow(transparent, 300.0)
			glowAnimation.addListener(object : AnimationTaskAdapter() {
				override fun ended(task: AnimationTask) {
					transparent.transparency = Transparent.FULLY_OPAQUE
					transparent.validate()
				}
			})

			animator.schedule(glowAnimation)
			glowAnimation.start()
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

	private fun registerAnimation(net: Net<*>, animation: DigitalEdgeViewNetAnimation) {
		scheduler.logActorTrace(net) { "register net animation" }
		netAnimationMap.getOrPut(net) { mutableListOf() }.add(animation)
	}

	private fun unregisterAnimation(net: Net<*>, animation: DigitalEdgeViewNetAnimation) {
		scheduler.logActorTrace(net) { "unregister net animation" }
		netAnimationMap[net]?.remove(animation)
	}

	/** Determines whether [DigitalEdgeViewNetAnimation] is required based on the current system settings.*/
	private fun requireEdgeViewAnimation(): Boolean {
		return currentGraphAnimationType.graphViewAnimationType == GraphViewAnimationType.Animation
			&& systemSpeedCategory.systemSpeedCategory == SystemSpeedCategory.Explore
	}

	/** Determines whether an animation is to be shown while [VerticeView]s are calculating. */
	private fun requireVerticeGlowAnimation(): Boolean {
		return currentGraphAnimationType.graphViewAnimationType == GraphViewAnimationType.Animation
			&& scheduler.isPaused
	}

	/** ---- [GraphViewAnimator] */

	fun dispose() {
		unregisterActorListener(drawingView.drawing.graph!!)
		eventBus.unregister(CurrentGraphAnimationTypeEvent::class, currentGraphViewAnimationTypeHandler)
		eventBus.unregister(SchedulerActivationStateEvent::class, schedulerActivationStateHandler)
		eventBus.unregister(SystemSpeedCategoryEvent::class, systemSpeedCategoryHandler)
	}

	private fun handleGraphViewChanged(oldGraphView: GraphView<GraphElementView<*>>?, newGraphView: GraphView<GraphElementView<*>>?) {
		if (oldGraphView != null) {
			oldGraphView.removeDrawableContainerListener(graphViewListener)
			unregisterActorListener(oldGraphView.graph!!)
		}
		if (newGraphView != null) {
			newGraphView.addDrawableContainerListener(graphViewListener)
			registerActorListener(newGraphView.graph!!)
		}
	}

	private fun registerActorListener(graph: Graph) {
		graph.elements.forEach { it.addActorListener(this) }
	}

	private fun unregisterActorListener(graph: Graph) {
		graph.elements.forEach { it.removeActorListener(this) }
	}

	/**
	 * Listens for adding and removing [GraphElementView]s in the current [GraphView] and adds or removes
	 * the this [GraphViewAnimator] as [ActorListener] accordingly.
	 */
	private inner class GraphViewListener : DrawableContainerAdapter<GraphElementView<*>>() {

		override fun drawableAdded(event: DrawableContainerEvent<GraphElementView<*>>) {
			if (event.child is GraphElementView<*>) {
				(event.child as GraphElementView<*>).model.addActorListener(this@GraphViewAnimator)
			}
		}

		override fun drawableRemoved(event: DrawableContainerEvent<GraphElementView<*>>) {
			if (event.child is GraphElementView<*>) {
				(event.child as GraphElementView<*>).model.removeActorListener(this@GraphViewAnimator)
			}
		}
	}
}