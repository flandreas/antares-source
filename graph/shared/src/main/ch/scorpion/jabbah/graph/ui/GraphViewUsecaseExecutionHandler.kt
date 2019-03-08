package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.MouseAdapter
import ch.scorpion.jabbah.base.event.MouseEvent
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.graph.view.GraphElementView
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.Usecase

/**
 * Handles input events on a [GraphView] while a [Usecase] is being executed.
 */
class GraphViewUsecaseExecutionHandler(
	view: DrawingView<GraphView<GraphElementView<*>>>,
	eventBus: EventBus
) : AbstractGraphViewExecutionHandler(view, eventBus) {

	override fun createMouseHandler(): MouseAdapter = MouseHandler()

	override val activationCondition: Boolean get() = currentMode === ch.scorpion.jabbah.graph.ApplicationMode.EXEC_USECASE

	private inner class MouseHandler : MouseAdapter() {

		override fun mouseMoved(e: MouseEvent) {
			val x = view.viewToModelX(e.x.toDouble())
			val y = view.viewToModelY(e.y.toDouble())
			tooltipHandler.handle(view, view.drawing, x, y)
		}
	}
}