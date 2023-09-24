package ch.scorpion.antares

import ch.scorpion.antares.view.AbstractGraphViewEditingTest
import ch.scorpion.antares.view.gate.LogicGateView
import ch.scorpion.antares.view.inout.DigitalCircuitInOutView
import ch.scorpion.jabbah.base.event.Modifier
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.edit.command.SourcingCommandManager
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.graph.view.app.GraphViewAppServiceImpl
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.graph.GraphViewCopyPasteService
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
		val inOutView = DigitalCircuitInOutView().apply { location = Point2D(-280.0,-245.0) }
		val notGateView = LogicGateView.notGateView().apply { location = Point2D(-168.0,-147.0) }
		val andGateView = LogicGateView.andGateView().apply {
			location = Point2D(-21.0,-35.0)
			orientation = Direction.SOUTH
		}

		EditModule.drawingAppService.add(inOutView, editor.view)
		EditModule.drawingAppService.add(notGateView, editor.view)
		EditModule.drawingAppService.add(andGateView, editor.view)
		(editor.commandManager as SourcingCommandManager).addSnapshot()

		// Move AND gate
		driver.mouseMoveTo(-25,-79)
		driver.pressMouseAt(-25,-79)
		driver.dragMouseTo(-100,-32)
		driver.releaseMouseAt(-100,-32)

		// Copy/Paste AND gate
		editor.view.selectionManager.select(andGateView)
		EditModule.drawingAppService.copy(editor.view)
		EditModule.drawingAppService.paste(editor.view)

		// Move pasted AND gate
		driver.mouseMoveTo(-79,2)
		driver.pressMouseAt(-79,2)
		driver.dragMouseTo(-37,-21)
		driver.releaseMouseAt(-37,-21)

		// Select both AND gates
		driver.mouseMoveTo(-169, -94)
		driver.pressMouseAt(-169, -94)
		driver.dragMouseTo(32, 48)
		driver.releaseMouseAt(32, 48)

		// Copy selected AND gates
		EditModule.drawingAppService.copy(editor.view)
		EditModule.drawingAppService.paste(editor.view)

		// Move selected 2 AND gates
		driver.mouseMoveTo(-78, 0)
		driver.pressMouseAt(-78, 0)
		driver.dragMouseTo(68, -24)
		driver.releaseMouseAt(68, -24)

		// Paste 2 AND gates, resulting in 6 AND gates
		EditModule.drawingAppService.paste(editor.view)


		// Connect DigitalCircuitInOutView with NotGateView
		driver.mouseMoveTo(-280, -245)
		driver.pressMouseAt(-280, -245)
		driver.dragMouseTo(-238, -147)
		driver.releaseMouseAt(-238, -147)

		// Connect NotGateView with AND gate 2
		driver.mouseMoveTo(-168, -147)
		driver.pressMouseAt(-168, -147)
		driver.dragMouseTo(-49, -56)
		driver.releaseMouseAt(-49, -56)

		// Junction from NotGateView net to AND gate 4
		driver.mouseMoveTo(-49, -147, Modifier.Alt.mask)
		driver.pressMouseAt(-49, -147, Modifier.Alt.mask)
		driver.dragMouseTo(119, -56)
		driver.releaseMouseAt(119, -56)

		// Junction from NotGateView net to AND gate 6
		// In manual test, this created a distorted EdgeView
		// Here, the error is created
		driver.mouseMoveTo(-119, -147, Modifier.Alt.mask)
		driver.pressMouseAt(-119, -147, Modifier.Alt.mask)
		driver.dragMouseTo(315, -56)
		driver.releaseMouseAt(315, -56)

		// Junction from CircuitInOutView net to AND gate 1
		driver.mouseMoveTo(-259, -245, Modifier.Alt.mask)
		driver.pressMouseAt(-259, -245, Modifier.Alt.mask)
		driver.dragMouseTo(-112, -56)
		driver.releaseMouseAt(-112, -56)

		// Junction from CircuitInOutView net to AND gate 3
		driver.mouseMoveTo(-112, -245, Modifier.Alt.mask)
		driver.pressMouseAt(-112, -245, Modifier.Alt.mask)
		driver.dragMouseTo(56, -56)
		driver.releaseMouseAt(56, -56)

		// Junction from CircuitInOutView net to AND gate 5
		driver.mouseMoveTo(56, -245, Modifier.Alt.mask)
		driver.pressMouseAt(56, -245, Modifier.Alt.mask)
		driver.dragMouseTo(224, -56)
		driver.releaseMouseAt(224, -56)

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