package io.antarescircuit.antares

import io.antarescircuit.antares.view.AbstractGraphViewEditingTest
import io.antarescircuit.antares.view.gate.LogicGateView
import io.antarescircuit.antares.view.inout.DigitalCircuitInOutView
import io.antarescircuit.jabbah.base.event.Modifier
import io.antarescircuit.jabbah.base.geom.Dimension2D
import io.antarescircuit.jabbah.base.geom.Direction
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.edit.command.SourcingCommandManager
import io.antarescircuit.jabbah.edit.module.EditModule
import io.antarescircuit.jabbah.graph.view.app.GraphViewAppServiceImpl
import io.antarescircuit.jabbah.graph.view.EdgeView
import io.antarescircuit.jabbah.graph.view.graph.GraphViewCopyPasteService
import junit.framework.TestCase.assertEquals
import kotlin.test.BeforeTest
import kotlin.test.Test

/**
 * Regression test for a bug that lead to corrupt circuits when undoing
 * splitting [EdgeView]s.
 */
class Bug648 : AbstractGraphViewEditingTest(snapshotSize = 5) {

	@BeforeTest
	fun setUp() {
		AntaresTestRule.configure()
		EditModule.drawingAppService = GraphViewAppServiceImpl(GraphViewCopyPasteService())
	}

	override fun setupCircuit() {
		// Cannot build circuit because CommandManager is not yet bound.
		editor.selectionTool.rubberBandHandler.delaySelectTimer = null
	}

	@Test
	fun test() {
		canvasBuilder.withDimension(Dimension2D(1000, 1000))

		val inOutView = DigitalCircuitInOutView().apply { location = Point2D(220, 255) }
		val notGateView = LogicGateView.notGateView().apply { location = Point2D(332, 353) }
		val andGateView = LogicGateView.andGateView().apply {
			location = Point2D(479, 465)
			orientation = Direction.SOUTH
		}

		EditModule.drawingAppService.add(inOutView, editor.view)
		EditModule.drawingAppService.add(notGateView, editor.view)
		EditModule.drawingAppService.add(andGateView, editor.view)
		(editor.commandManager as SourcingCommandManager).addSnapshot()

		// Move AND gate
		driver.mouseMoveTo(475,421)
		driver.pressMouseAt(475,421)
		driver.dragMouseTo(400,468)
		driver.releaseMouseAt(400,468)

		// Copy/Paste AND gate
		editor.view.selectionManager.select(andGateView)
		EditModule.drawingAppService.copy(editor.view)
		EditModule.drawingAppService.paste(editor.view)

		// Move pasted AND gate
		driver.mouseMoveTo(421,502)
		driver.pressMouseAt(421,502)
		driver.dragMouseTo(463,479)
		driver.releaseMouseAt(463,479)

		// Select both AND gates
		driver.mouseMoveTo(331,406)
		driver.pressMouseAt(331,406)
		driver.dragMouseTo(532,548)
		driver.releaseMouseAt(532,548)

		// Copy selected AND gates
		EditModule.drawingAppService.copy(editor.view)
		EditModule.drawingAppService.paste(editor.view)

		// Move selected 2 AND gates
		driver.mouseMoveTo(422,500)
		driver.pressMouseAt(422,500)
		driver.dragMouseTo(568,476)
		driver.releaseMouseAt(568,476)

		// Paste 2 AND gates, resulting in 6 AND gates
		EditModule.drawingAppService.paste(editor.view)


		// Connect DigitalCircuitInOutView with NotGateView
		driver.mouseMoveTo(220,255)
		driver.pressMouseAt(220,255)
		driver.dragMouseTo(262,353)
		driver.releaseMouseAt(262,353)

		// Connect NotGateView with AND gate 2
		driver.mouseMoveTo(332,353)
		driver.pressMouseAt(332,353)
		driver.dragMouseTo(451,444)
		driver.releaseMouseAt(451,444)

		// Junction from NotGateView net to AND gate 4
		driver.mouseMoveTo(451, 353, Modifier.Alt.mask)
		driver.pressMouseAt(451, 353, Modifier.Alt.mask)
		driver.dragMouseTo(619, 444)
		driver.releaseMouseAt(619, 444)

		// Junction from NotGateView net to AND gate 6
		// In manual test, this created a distorted EdgeView
		// Here, the error is created
		driver.mouseMoveTo(381, 353, Modifier.Alt.mask)
		driver.pressMouseAt(381, 353, Modifier.Alt.mask)
		driver.dragMouseTo(815, 444)
		driver.releaseMouseAt(815, 444)

		// Junction from CircuitInOutView net to AND gate 1
		driver.mouseMoveTo(241, 255, Modifier.Alt.mask)
		driver.pressMouseAt(241, 255, Modifier.Alt.mask)
		driver.dragMouseTo(388, 444)
		driver.releaseMouseAt(388, 444)

		// Junction from CircuitInOutView net to AND gate 3
		driver.mouseMoveTo(388, 255, Modifier.Alt.mask)
		driver.pressMouseAt(388, 255, Modifier.Alt.mask)
		driver.dragMouseTo(556, 444)
		driver.releaseMouseAt(556, 444)

		// Junction from CircuitInOutView net to AND gate 5
		driver.mouseMoveTo(556, 255, Modifier.Alt.mask)
		driver.pressMouseAt(556, 255, Modifier.Alt.mask)
		driver.dragMouseTo(724, 444)
		driver.releaseMouseAt(724, 444)

		assertEquals(5, builder.graphView.getNodeViews().size)
		assertEquals(12, builder.graphView.getEdgeViews().size)
		assertEquals(8, builder.graphView.getVerticeViews().size)

		editor.commandManager.undo()

		assertEquals(4, builder.graphView.getNodeViews().size)
		assertEquals(10, builder.graphView.getEdgeViews().size)
		assertEquals(8, builder.graphView.getVerticeViews().size)

		// Simulate saving: Produces IllegalStateException: Port already connected

		// This does NOT produce the error
		// StorableCloner.clone(builder.graphView)

		// This DID produce the error (done by the running App in the save process)
		editor.commandManager.reset()
	}
}