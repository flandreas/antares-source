package ch.scorpion.jabbah.graph.view.oscilloscope

import ch.scorpion.jabbah.base.geom.Dimension2D
import ch.scorpion.jabbah.draw.Canvas
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.editor.EditEditorModule
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.graph.model.oscilloscope.OscilloscopeProbeVertice
import ch.scorpion.jabbah.graph.view.*
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.graph.view.vertice.TestVerticeView
import io.mockk.every
import io.mockk.mockk
import kotlin.test.*

class OscilloscopeEditIntegrationTest {

	companion object {
		init {
			GraphViewTestRule.configure()
		}
	}

	private val builder: GraphViewBuilder<Boolean> = GraphViewBuilder {
		builder -> view.setDrawing(builder.graphView)
	}
	private val view = EditModule.drawingViewFactory.create(builder.graphView as Drawing<Component>, null, false) as DrawingView<GraphView>
	private val editor: Editor = EditEditorModule.createEditor(view as DrawingView<Drawing<Component>>)
	private val driver = EditorToolDriver(editor)

	init {
		val canvas = mockk<Canvas>(relaxed = true)
		every { canvas.dimension } returns Dimension2D(1000, 1000)
		every { canvas.devicePixelRatio } returns 1
		view.canvas = canvas

		editor.commandManager.bindDataHolder(builder)
	}

	@BeforeTest
	fun setupCircuit() {
		val vv1 = builder.addVerticeView(TestVerticeView.createEastOutputVerticeView("P", 0, 0))
		val vv2 = builder.addVerticeView(TestVerticeView.createEastOutputVerticeView("Q", 100, 0))
		val vv3 = builder.addVerticeView(TestVerticeView.createEastOutputVerticeView("R", 200, 0))
		builder.connect(vv1, vv2)
		builder.connect(vv2, vv3)
	}

	@Test
	fun shouldCreateOscilloscopeView() {
		GraphViewModule.oscilloscopeViewService.displayOscilloscope(view)
		assertTrue(GraphViewModule.oscilloscopeViewService.isOscilloscopeDisplayed(view.drawing))
		assertTrue(EditModule.commandManager.canUndo())
	}

	@Test
	fun shouldUndoCreateOscilloscopeView() {
		GraphViewModule.oscilloscopeViewService.displayOscilloscope(view)
		EditModule.commandManager.undo()
		assertFalse(GraphViewModule.oscilloscopeViewService.isOscilloscopeDisplayed(view.drawing))
	}

	@Test
	fun shouldHideOscilloscopeView() {
		GraphViewModule.oscilloscopeViewService.displayOscilloscope(view)
		EditModule.commandManager.reset()
		GraphViewModule.oscilloscopeViewService.hideOscilloscope(view)
		assertFalse(GraphViewModule.oscilloscopeViewService.isOscilloscopeDisplayed(view.drawing))
		assertTrue(EditModule.commandManager.canUndo())
	}

	@Test
	fun shouldUndoHideOscilloscopeView() {
		GraphViewModule.oscilloscopeViewService.displayOscilloscope(view)
		GraphViewModule.oscilloscopeViewService.hideOscilloscope(view)
		EditModule.commandManager.undo()
		assertTrue(GraphViewModule.oscilloscopeViewService.isOscilloscopeDisplayed(view.drawing))
	}

	@Test
	fun shouldAddSignalRow() {
		GraphViewModule.oscilloscopeViewService.displayOscilloscope(view)
		GraphViewModule.oscilloscopeViewService.addRow(view, getOscilloscopeView())
		assertEquals(1, getOscilloscopeView().rowsCount)
	}

	@Test
	fun shouldUndoAddSignalRow() {
		GraphViewModule.oscilloscopeViewService.displayOscilloscope(view)
		GraphViewModule.oscilloscopeViewService.addRow(view, getOscilloscopeView())
		EditModule.commandManager.undo()
		assertEquals(0, getOscilloscopeView().rowsCount)
	}

	@Test
	fun shouldRemoveSignalRow() {
		addRowAndDropProbeViewIntoGraphView()

		GraphViewModule.oscilloscopeViewService.removeRow(view, "P", getOscilloscopeView())

		assertEquals(0, getOscilloscopeView().rowsCount)
		assertEquals(0, view.drawing.getVerticeViews().filterIsInstance<OscilloscopeProbeVerticeView<*>>().size)
	}

	@Test
	fun shouldAddAndDropRowAfterRemovingRowWithDroppedProbe() {
		addRowAndDropProbeViewIntoGraphView()
		GraphViewModule.oscilloscopeViewService.removeRow(view, "P", getOscilloscopeView())

		addRowAndDropProbeViewIntoGraphView()

		assertDroppedProbeView()
	}

	@Test
	fun shouldDropProbeViewIntoGraphView() {
		addRowAndDropProbeViewIntoGraphView()

		assertDroppedProbeView()
		assertTrue(EditModule.commandManager.canUndo())
	}

	private fun addRowAndDropProbeViewIntoGraphView() {
		GraphViewModule.oscilloscopeViewService.displayOscilloscope(view)
		GraphViewModule.oscilloscopeViewService.addRow(view, getOscilloscopeView())
		EditModule.commandManager.reset()
		dropFirstRowProbeView()
	}

	private fun dropFirstRowProbeView() {
		editor.currentTool = editor.selectionTool
		driver.mouseMoveTo(-104, 45)
		driver.pressMouseAt(-104, 45)
		driver.dragMouseTo(50, -20)
		driver.releaseMouseAt(50, -20)
	}

	private fun assertDroppedProbeView() {
		assertEquals(1, view.drawing.getVerticeViews().filterIsInstance<OscilloscopeProbeVerticeView<*>>().size)
		val pvv = view.drawing.getVerticeViews().first { it is OscilloscopeProbeVerticeView<*> } as OscilloscopeProbeVerticeView<*>
		val vv = getTestVerticeView("P")
		assertNotNull(pvv)
		assertEquals("P", pvv.name) // Inherit defined Port name
		assertEquals("P", getOscilloscopeView().getRow(0).name)
		assertFalse(getOscilloscopeView().getRow(0).probeView.verticeViewPresent)
		assertSame(vv.model.getOutput<Boolean>().net, pvv.model.getInput<Boolean>().net)
	}

	@Test
	fun shouldUndoDropProbeViewIntoGraphView() {
		addRowAndDropProbeViewIntoGraphView()

		EditModule.commandManager.undo()

		assertNotDroppedPortView()
		assertFalse(EditModule.commandManager.canUndo())
	}

	private fun assertNotDroppedPortView() {
		val pvv = view.drawing.getVerticeViews().firstOrNull { it is OscilloscopeProbeVerticeView<*> } as OscilloscopeProbeVerticeView<*>?
		assertNull(pvv)
		assertTrue(getOscilloscopeView().getRow(0).probeView.verticeViewPresent)
		assertEquals("1", getOscilloscopeView().getRow(0).probeView.name) // Reset inherited Port name
		assertEquals("1", getOscilloscopeView().model.getInput<Boolean>().name)
	}

	@Test
	fun shouldRedoDragProbeViewIntoGraphView() {
		addRowAndDropProbeViewIntoGraphView()
		EditModule.commandManager.undo()

		EditModule.commandManager.redo()

		assertDroppedProbeView()
		assertTrue(EditModule.commandManager.canUndo())
	}

	@Test
	fun shouldSelectProbeView() {
		addRowAndDropProbeViewIntoGraphView()

		driver.mouseMoveTo(50, -20)
		driver.pressMouseAt(50, -20)
		driver.releaseMouseAt(50, -20)

		assertTrue(editor.view.selectionManager.isSelected(view.drawing.getVerticeViews().first { it is OscilloscopeProbeVerticeView<*> }))
	}

	@Test
	fun shouldMoveProbeView() {
		addRowAndDropProbeViewIntoGraphView()
		EditModule.commandManager.reset()

		moveProbeView()

		assertMovedProbedView()
	}

	private fun moveProbeView() {
		editor.currentTool = editor.selectionTool
		driver.mouseMoveTo(50, -20)
		driver.pressMouseAt(50, -20)
		driver.dragMouseTo(150, -20)
		driver.releaseMouseAt(150, -20)
	}

	private fun assertMovedProbedView() {
		assertEquals(1, view.drawing.getVerticeViews().filterIsInstance<OscilloscopeProbeVerticeView<*>>().size)
	}

	@Test
	fun shouldUndoMoveProbeView() {
		addRowAndDropProbeViewIntoGraphView()
		EditModule.commandManager.reset()
		moveProbeView()

		EditModule.commandManager.undo()

		assertDroppedProbeView()
	}

	@Test
	fun shouldRedoMoveProbeView() {
		addRowAndDropProbeViewIntoGraphView()
		EditModule.commandManager.reset()
		moveProbeView()
		EditModule.commandManager.undo()

		EditModule.commandManager.redo()

		assertMovedProbedView()
	}

	@Test
	fun shouldResetProbeViewWhenDeleting() {
		addRowAndDropProbeViewIntoGraphView()

		deleteProbeView()

		assertNotDroppedPortView()
		assertTrue(EditModule.commandManager.canUndo())
	}

	private fun deleteProbeView() {
		GraphViewModule.graphViewAppService.delete(listOf(
			view.drawing.getVerticeViews().filterIsInstance<OscilloscopeProbeVerticeView<*>>().first()),
			view)
	}

	@Test
	fun shouldUndoResetProbeViewWhenDeleting() {
		addRowAndDropProbeViewIntoGraphView()
		deleteProbeView()

		EditModule.commandManager.undo()

		assertDroppedProbeView()
	}

	@Test
	fun shouldRedoResetProbeViewWhenDeleting() {
		addRowAndDropProbeViewIntoGraphView()
		deleteProbeView()
		EditModule.commandManager.undo()

		EditModule.commandManager.redo()

		assertNotDroppedPortView()
	}

	@Test
	fun shouldDeleteOscilloscopeView() {
		addRowAndDropProbeViewIntoGraphView()

		deleteOscilloscopeView()

		assertNoOscilloscope()
	}

	private fun deleteOscilloscopeView() {
		GraphViewModule.graphViewAppService.delete(listOf(
			view.drawing.getVerticeViews().filterIsInstance<OscilloscopeView>().first()),
			view)
	}

	private fun assertNoOscilloscope() {
		assertEquals(0, view.drawing.getVerticeViews().filterIsInstance<OscilloscopeProbeVerticeView<*>>().size)
		assertEquals(0, view.drawing.getVerticeViews().filterIsInstance<OscilloscopeView>().size)
		assertEquals(0, builder.graphView.graph!!.elements.filterIsInstance<OscilloscopeProbeVertice<*>>().size)
		assertEquals(2, getTestVerticeView("P").model.getOutput<Boolean>().net!!.portsCount)
	}

	@Test
	fun shouldUndoDeleteOscilloscopeView() {
		addRowAndDropProbeViewIntoGraphView()
		deleteOscilloscopeView()

		EditModule.commandManager.undo()

		assertTrue(GraphViewModule.oscilloscopeViewService.isOscilloscopeDisplayed(view.drawing))
		assertDroppedProbeView()
	}

	@Test
	fun shouldRedoDeleteOscilloscopeView() {
		addRowAndDropProbeViewIntoGraphView()
		deleteOscilloscopeView()
		EditModule.commandManager.undo()

		EditModule.commandManager.redo()

		assertNoOscilloscope()
	}

	private fun getOscilloscopeView(): OscilloscopeView =
		view.drawing.getVerticeViews().first { it is OscilloscopeView } as OscilloscopeView

	private fun getTestVerticeView(name: String): VerticeView<*> =
		view.drawing.getVerticeViews().first { it is TestVerticeView && it.model.name == name }
}