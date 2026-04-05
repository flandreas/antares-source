package io.antarescircuit.jabbah.edit.view

import io.antarescircuit.jabbah.base.geom.Rectangle2D
import io.antarescircuit.jabbah.edit.AbstractEditIntegrationTest
import io.antarescircuit.jabbah.edit.model.rectangle.RectangleComponent
import io.antarescircuit.jabbah.edit.module.EditModule
import kotlin.test.Test
import kotlin.test.assertEquals

class ZoomPanIntegrationTest : AbstractEditIntegrationTest() {

	@Test
	fun shouldNotChangeZoomPanOnUndo() {
		view.navigator.panBy(20, 30)

		// Use "delete" because DeleteCommand is NOT Undoable and leads to setting new content in the DrawingView
		// when replaying the snapshot upon "Undo"
		val component = EditModule.drawingAppService.add(RectangleComponent(shape = Rectangle2D(0, 0, 200, 100)), view)
		EditModule.drawingAppService.delete(listOf(component), view)

		val origTransform = view.transformation.copy()
		editor.commandManager.undo()

		assertEquals(origTransform.affineTransform, view.transformation.affineTransform)
	}
}