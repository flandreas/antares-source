package ch.scorpion.jabbah.edit.select

import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.base.time.Timer
import ch.scorpion.jabbah.edit.AbstractEditIntegrationTest
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.model.rectangle.RectangleComponent
import ch.scorpion.jabbah.edit.module.EditModule

abstract class AbstractRubberBandHandlerTest : AbstractEditIntegrationTest() {

	protected lateinit var rectangle: Component
		private set

	protected fun setTimer(timer: Timer?) {
		editor.selectionTool.rubberBandHandler.delaySelectTimer = timer
	}

	override fun setup() {
		super.setup()
		rectangle = EditModule.drawingAppService.add(
			RectangleComponent(shape = Rectangle2D(10, 10, 20, 20)),
			editor.view)
	}

	protected fun fullyEncloseRectangle() {
		driver.mouseMoveTo(0, 0)
		driver.pressMouseAt(0, 0)
		driver.dragMouseTo(100, 100)
	}

	protected fun partiallyEncloseRectangle() {
		driver.mouseMoveTo(0, 0)
		driver.pressMouseAt(0, 0)
		driver.dragMouseTo(15, 15)
	}

	protected fun notEncloseRectangle() {
		driver.mouseMoveTo(100, 100)
		driver.pressMouseAt(100, 100)
		driver.dragMouseTo(200, 200)
	}
}