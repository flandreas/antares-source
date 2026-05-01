package io.antarescircuit.antares.view

import io.antarescircuit.antares.model.port.DigitalPort
import io.antarescircuit.antares.view.net.DigitalEdgeView
import io.antarescircuit.antares.view.net.DigitalEdgeViewNetAnimation
import io.antarescircuit.jabbah.animation.Animator
import io.antarescircuit.jabbah.draw.style.StyleProvider
import io.antarescircuit.jabbah.edit.DrawingView
import io.antarescircuit.jabbah.execution.actor.ActorData
import io.antarescircuit.jabbah.execution.actor.ActorListener
import io.antarescircuit.jabbah.execution.scheduler.Scheduler
import io.antarescircuit.jabbah.graph.model.Port
import io.antarescircuit.jabbah.graph.view.AbstractGraphViewExecutionAnimationFactory
import io.antarescircuit.jabbah.graph.view.EdgeView
import io.antarescircuit.jabbah.graph.view.EdgeViewNetAnimation
import io.antarescircuit.jabbah.graph.view.GraphElementView
import io.antarescircuit.jabbah.graph.view.GraphView

class AntaresExecutionAnimationFactory : AbstractGraphViewExecutionAnimationFactory() {

	override fun createEdgeViewNetAnimation(
		actorListener: ActorListener,
		actorData: ActorData,
		startEdgeView: EdgeView<*>,
		startPort: Port<*>,
		drawingView: DrawingView<GraphElementView<*>, GraphView>,
		scheduler: Scheduler,
		animator: Animator,
		styleProvider: StyleProvider
	): EdgeViewNetAnimation {

		return DigitalEdgeViewNetAnimation(
			actorListener = actorListener,
			actorData = actorData,
			startEdgeView = startEdgeView as DigitalEdgeView,
			startPort = startPort as DigitalPort,
			drawingView = drawingView,
			animator = animator,
			scheduler = scheduler,
			styleProvider = styleProvider
		)
	}
}