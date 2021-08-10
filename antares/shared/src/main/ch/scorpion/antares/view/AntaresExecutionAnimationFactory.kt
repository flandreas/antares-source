package ch.scorpion.antares.view

import ch.scorpion.antares.model.port.DigitalPort
import ch.scorpion.antares.view.net.DigitalEdgeView
import ch.scorpion.antares.view.net.DigitalEdgeViewNetAnimation
import ch.scorpion.jabbah.animation.Animator
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.execution.actor.ActorData
import ch.scorpion.jabbah.execution.actor.ActorListener
import ch.scorpion.jabbah.execution.scheduler.Scheduler
import ch.scorpion.jabbah.graph.model.Port
import ch.scorpion.jabbah.graph.view.AbstractGraphViewExecutionAnimationFactory
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.EdgeViewNetAnimation
import ch.scorpion.jabbah.graph.view.GraphView

class AntaresExecutionAnimationFactory : AbstractGraphViewExecutionAnimationFactory() {

	override fun createEdgeViewNetAnimation(
		actorListener: ActorListener,
		actorData: ActorData,
		startEdgeView: EdgeView<*>,
		startPort: Port<*>,
		drawingView: DrawingView<GraphView>,
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