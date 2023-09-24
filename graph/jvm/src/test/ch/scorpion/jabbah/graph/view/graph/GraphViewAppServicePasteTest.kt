package ch.scorpion.jabbah.graph.view.graph

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.graph.view.AbstractGraphViewEditingTest
import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import ch.scorpion.jabbah.graph.view.app.GraphViewAppServiceImpl
import ch.scorpion.jabbah.graph.view.vertice.TestVerticeView
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class GraphViewAppServicePasteTest : AbstractGraphViewEditingTest(snapshotSize = 1) {

	private lateinit var vv1: TestVerticeView
	private lateinit var vv2: TestVerticeView
	private lateinit var vv3: TestVerticeView

	override fun setupCircuit() {
		vv1 = builder.addVerticeView(TestVerticeView.createSouthInputVerticeView("vv1", 0, 0))
		vv2 = builder.addVerticeView(TestVerticeView.createSouthInputVerticeView("vv2", 100, 0))
		vv3 = builder.addVerticeView(TestVerticeView.createSouthInputVerticeView("vv3", 200, 0))
	}

	@BeforeTest
	fun setup() {
		GraphViewTestRule.configure()
		EditModule.drawingAppService = GraphViewAppServiceImpl(GraphViewCopyPasteService())
	}

	@Test
	fun shouldUndoAcrossSnapshots() {
		//editor.commandManager.reset()

		editor.view.selectionManager.select(vv1)
		EditModule.drawingAppService.copy(editor.view)
		EditModule.drawingAppService.paste(editor.view)
		val pastedVV = editor.drawing.getWithId(4)!!
		val offset = Point2D(0, 100).subtract(pastedVV.location) // 4 at 0,100

		// Perform a move like the UI does and register a MoveCommand via DrawingAppService afterwards.
		// Since snapshotSize is 2, the Command created in move() service would lead to a new Snapshot.
		// However, a registered Command MUST NOT be in the Snapshot, because if it was, the displacement
		// of the Component would happen twice
		pastedVV.moveBy(offset.x, offset.y)
		EditModule.drawingAppService.move(listOf(pastedVV), offset, editor, register = true)

		assertComponentLocations()

		editor.commandManager.undo()
		editor.commandManager.redo()

		assertComponentLocations()
	}

	private fun assertComponentLocations() {
		assertEquals(Point2D(0, 0), editor.drawing.getWithId(1)!!.location)
		assertEquals(Point2D(100, 0), editor.drawing.getWithId(2)!!.location)
		assertEquals(Point2D(200, 0), editor.drawing.getWithId(3)!!.location)
		assertEquals(Point2D(0, 100), editor.drawing.getWithId(4)!!.location)
	}
}