package io.antarescircuit.antares

import io.antarescircuit.antares.view.AbstractGraphViewEditingTest
import io.antarescircuit.antares.view.gate.LogicGateView
import io.antarescircuit.antares.view.inout.DigitalCircuitInOutView
import io.antarescircuit.jabbah.base.event.Modifier
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.edit.app.RotateCommand
import io.antarescircuit.jabbah.edit.module.EditModule
import io.antarescircuit.jabbah.graph.view.EdgeView
import io.antarescircuit.jabbah.graph.view.app.GraphViewAppServiceImpl
import io.antarescircuit.jabbah.graph.view.graph.GraphViewCopyPasteService
import junit.framework.TestCase.assertTrue
import kotlin.test.BeforeTest
import kotlin.test.Test

/**
 * Exploratory test to find cause of bug #627: Wire distortion when creating junction
 * at [EdgeView] corner. The layout of the [EdgeView] being split depended on the mouse location;
 * when the mouse location was slightly below the [EdgeView]'s corner, the layout algorithm
 * laid out the [EdgeView] endpoint with direction SOUTH, which lead to a distorted layout.
 */
class Bug627 : AbstractGraphViewEditingTest(snapshotSize = 5) {

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
		DigitalCircuitInOutView().apply {
			location = Point2D(-427.0,-210.0)
			EditModule.drawingAppService.add(this, editor.view)
		}
		LogicGateView.notGateView().apply {
			location = Point2D(-287.0,-70.0)
			EditModule.drawingAppService.add(this, editor.view)
		}

		val andGateView = EditModule.drawingAppService.add(
			LogicGateView.andGateView().apply {
				location = Point2D(-119.0,119.0)
			}, editor.view)

		// Rotate AND gate 3 times
		for (i in 0..2) {
			editor.commandManager.execute(RotateCommand(
				false,
				view,
				listOf(andGateView.id),
				andGateView.location
			))
		}

		// Move AND gate
		driver.moveMouseAndPressAt(-114, 76)
		driver.dragMouseAndReleaseAt(-179, 86)

		// Copy/Paste AND gate
		//editor.view.selectionManager.select(andGateView)
		EditModule.drawingAppService.copy(editor.view)
		EditModule.drawingAppService.paste(editor.view)

		// Move pasted AND gate
		driver.moveMouseAndPressAt(-158, 105)
		driver.dragMouseAndReleaseAt(-114, 84)

		// Select both AND gates
		driver.moveMouseAndPressAt(-256, 16)
		driver.dragMouseAndReleaseAt(-19, 168)

		// Copy selected AND gates and paste them
		EditModule.drawingAppService.copy(editor.view)
		EditModule.drawingAppService.paste(editor.view)

		// Move selected 2 AND gates
		driver.moveMouseAndPressAt(-96, 112)
		driver.dragMouseAndReleaseAt(98, 89)

		// Paste 2 AND gates, resulting in 6 AND gates
		EditModule.drawingAppService.paste(editor.view)

		// Connect DigitalCircuitInOutView with NotGateView
		driver.moveMouseAndPressAt(-427, -210)
		driver.dragMouseAndReleaseAt(-357, -70)

		// Connect NotGateView with AND gate 2
		driver.moveMouseAndPressAt(-287, -70)
		driver.dragMouseAndReleaseAt(-133, 56)

		// Junction from NotGateView net to AND gate 4
		driver.moveMouseAndPressAt(-133, -70, Modifier.Alt.mask)
		driver.dragMouseAndReleaseAt(84, 56, Modifier.Alt.mask)

		// Junction from NotGateView net to AND gate 6
		driver.moveMouseAndPressAt(84, -70, Modifier.Alt.mask)
		driver.dragMouseAndReleaseAt(301, 56, Modifier.Alt.mask)

		// Junction from CircuitInOutView net to AND gate 1
		driver.moveMouseAndPressAt(-392, -210, Modifier.Alt.mask)
		driver.dragMouseAndReleaseAt(-196, 56, Modifier.Alt.mask)

		// Junction from CircuitInOutView net to AND gate 3
		// The start connection point is at (-196, -210). Press the mouse slightly below, but still within
		// the sensitive area.
		driver.moveMouseAndPressAt(-196, -205, Modifier.Alt.mask)
		driver.dragMouseAndReleaseAt(21, 56, Modifier.Alt.mask)

		assertTrue(builder.graphView.getEdgeViews().none { it.polyline.pointsCount > 3 })

		println()
	}
}