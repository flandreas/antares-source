package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.base.event.*
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.execution.module.ExecutionModule
import ch.scorpion.jabbah.execution.scheduler.Scheduler
import ch.scorpion.jabbah.graph.app.ApplicationMode
import ch.scorpion.jabbah.graph.app.ApplicationMode.EXEC_USECASE
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.Usecase

/**
 * Handles input events on a [GraphView] while a [Usecase] is being executed.
 */
class GraphViewUsecaseExecutionHandler(
	view: DrawingView<GraphView>,
	private val scheduler: Scheduler = ExecutionModule.scheduler,
	eventBus: EventBus = BaseModule.eventBus,
	applicationMode: ApplicationMode
) : AbstractGraphViewExecutionHandler(view, eventBus, applicationMode) {

	override fun createMouseHandler(): MouseAdapter = MouseHandler()

	override fun createKeyHandler(): KeyAdapter = KeyHandler()

	override val activationCondition: Boolean get() = currentMode === EXEC_USECASE

	private inner class MouseHandler : MouseAdapter() {

		override fun mouseMoved(e: MouseEvent) {
			val p = view.viewToModel(e.location)
			tooltipHandler.handle(view, view.drawing, p.x, p.y)
		}
	}

	private inner class KeyHandler : KeyAdapter() {

		override fun keyPressed(e: KeyEvent) {
			if (e.key == ' '.code) {
				if (scheduler.isPaused) {
					scheduler.resume()
				}
			}
		}
	}
}