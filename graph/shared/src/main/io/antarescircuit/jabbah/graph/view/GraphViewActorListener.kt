package io.antarescircuit.jabbah.graph.view

import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.event.EventHandler
import io.antarescircuit.jabbah.base.event.PropertyChangeEvent
import io.antarescircuit.jabbah.base.event.PropertyChangeListener
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.edit.DrawingView
import io.antarescircuit.jabbah.execution.SignalHandler
import io.antarescircuit.jabbah.execution.actor.Actor
import io.antarescircuit.jabbah.execution.actor.ActorData
import io.antarescircuit.jabbah.execution.actor.ActorListener
import io.antarescircuit.jabbah.execution.scheduler.SchedulerActivationStateEvent
import io.antarescircuit.jabbah.graph.GraphApplicationContextHolder
import io.antarescircuit.jabbah.graph.model.Graph
import io.antarescircuit.jabbah.graph.model.GraphActorData
import io.antarescircuit.jabbah.graph.model.GraphElement
import io.antarescircuit.jabbah.graph.view.module.GraphViewModule

/**
 * Acts as [ActorListener] on all [GraphElement]s of a [DrawingView]'s [GraphView].
 * This ensures that these [GraphElement]s can operate as breakpoints during execution.
 */
class GraphViewActorListener(
	private val drawingView: DrawingView<GraphView>,
	private val applicationContextHolder: GraphApplicationContextHolder,
	private val currentGraphAnimationType: CurrentGraphViewAnimationType = GraphViewModule.currentGraphViewAnimationType,
	private val eventBus: EventBus = BaseModule.eventBus
) : ActorListener {

	val executionAnimator = GraphViewExecutionAnimator(this, drawingView, applicationContextHolder)

	private val animatorProxy = AnimatorProxy(executionAnimator)

	private val schedulerActivationStateHandler: EventHandler<SchedulerActivationStateEvent> = { handle(it) }

	/** Listen for exchanges of the current GraphView in order to be an ActorListener on all GraphElements of its Graph */
	private val graphViewExchangeListener = GraphViewExchangeListener()

	init {
		eventBus.register(SchedulerActivationStateEvent::class, schedulerActivationStateHandler)
		drawingView.addPropertyChangeListener(graphViewExchangeListener)

		if (applicationContextHolder.scheduler.isActive) {
			registerActorListener(drawingView.drawing.graph!!)
		}
	}

	fun dispose() {
		executionAnimator.dispose()
		drawingView.drawing.graph?.let { unregisterActorListener(it) }
		eventBus.unregister(schedulerActivationStateHandler)
		drawingView.removePropertyChangeListener(graphViewExchangeListener)
	}

	override fun actingRequested(actor: Actor, signalHandler: SignalHandler, data: ActorData) {
		animatorProxy.actingRequested(actor, signalHandler, data)
	}

	override fun acted(actor: Actor, signalHandler: SignalHandler, data: ActorData) {
		animatorProxy.acted(actor, signalHandler, data)
	}

	private fun handle(@Suppress("UNUSED_PARAMETER") event: SchedulerActivationStateEvent) {
		if (event.scheduler === applicationContextHolder.scheduler) {
			if (applicationContextHolder.scheduler.isActive) {
				registerActorListener(drawingView.drawing.graph!!)
			} else {
				unregisterActorListener(drawingView.drawing.graph!!)
			}
		}
	}

	private fun handleGraphViewChanged(oldGraphView: GraphView?, newGraphView: GraphView?) {
		if (oldGraphView != null) {
			unregisterActorListener(oldGraphView.graph!!)
		}
		if (newGraphView != null) {
			registerActorListener(newGraphView.graph!!)
		}
	}

	private fun registerActorListener(graph: Graph) {
		graph.elements.forEach { it.addActorListener(this) }
	}


	private fun unregisterActorListener(graph: Graph) {
		graph.elements.forEach { it.removeActorListener(this) }
	}

	private inner class GraphViewExchangeListener : PropertyChangeListener<Any> {
		override fun propertyChanged(e: PropertyChangeEvent<Any>) {
			if (e.name == DrawingView.PROP_DRAWING) {
				handleGraphViewChanged(e.oldValue as GraphView, e.newValue as GraphView)
			}
		}
	}

	/**
	 * Forwards [ActorListener] methods to the specified [ActorListener] if animation is required
	 * as of [CurrentGraphViewAnimationType]. Otherwise completes the animation cycle immediately.
	 */
	private inner class AnimatorProxy(private val animator: GraphViewExecutionAnimator) : ActorListener {

		private val animationRequired get() = currentGraphAnimationType.graphViewAnimationType == GraphViewAnimationType.Animation

		override fun actingRequested(actor: Actor, signalHandler: SignalHandler, data: ActorData) {
			if (animationRequired) {
				animator.actingRequested(actor, data)
			}
		}

		override fun acted(actor: Actor, signalHandler: SignalHandler, data: ActorData) {
			if (animationRequired) {
				animator.acted(actor, signalHandler, data as GraphActorData)
			} else {
				actor.actingVisualized(signalHandler, this@GraphViewActorListener, data)
			}
		}
	}
}