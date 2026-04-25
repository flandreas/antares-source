package io.antarescircuit.jabbah.graph.ui

import io.antarescircuit.jabbah.base.event.*
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.draw.InputEventContext
import io.antarescircuit.jabbah.edit.DrawingView
import io.antarescircuit.jabbah.graph.GraphApplicationContextHolder
import io.antarescircuit.jabbah.graph.app.ApplicationMode.EXEC_USECASE
import io.antarescircuit.jabbah.graph.view.GraphElementView
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.Usecase

/**
 * Handles input events on a [GraphView] while a [Usecase] is being executed.
 */
class GraphViewUsecaseExecutionHandler(
	view: DrawingView<GraphElementView<*>, GraphView>,
	private val applicationContextHolder: GraphApplicationContextHolder,
	eventBus: EventBus = BaseModule.eventBus
) : AbstractGraphViewExecutionHandler(view, eventBus) {

	override fun createMouseHandler(): MouseAdapter = MouseHandler()

	override fun createKeyHandler(): KeyAdapter = KeyHandler()

	override val activationCondition: Boolean get() = currentMode === EXEC_USECASE

	private inner class MouseHandler : MouseAdapter() {

		override fun mouseMoved(e: MouseEvent) {
			val p = view.viewToModel(e.location)
			tooltipHandler.handle(view, view.drawing, InputEventContext(view, e, x = p.x, y = p.y, readonly = true))
		}
	}

	private inner class KeyHandler : KeyAdapter() {

		override fun keyPressed(e: KeyEvent) {
			if (e.key == ' '.code) {
				if (applicationContextHolder.scheduler.isSingleStepMode) {
					applicationContextHolder.scheduler.systemSpeedCategory.systemSpeed.resume()
				}
			}
		}
	}
}