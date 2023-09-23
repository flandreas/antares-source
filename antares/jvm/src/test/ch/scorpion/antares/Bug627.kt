package ch.scorpion.antares

import ch.scorpion.antares.view.AbstractGraphViewEditingTest
import ch.scorpion.antares.view.gate.LogicGateView
import ch.scorpion.antares.view.inout.DigitalCircuitInOutView
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.edit.command.SourcingCommandManager
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.graph.view.app.GraphViewAppServiceImpl
import ch.scorpion.jabbah.graph.view.graph.GraphViewCopyPasteService
import kotlin.test.BeforeTest
import kotlin.test.Test

class Bug627 : AbstractGraphViewEditingTest(snapshotSize = 5) {

	companion object {
		init {
			AntaresTestRule.configure()
		}
	}

	override fun setupCircuit() {
		// Cannot build circuit because CommandManager is not yet bound.
		editor.selectionTool.rubberBandHandler.delaySelectTimer = null
	}

	@BeforeTest
	fun setUp() {
		EditModule.drawingAppService = GraphViewAppServiceImpl(GraphViewCopyPasteService())
	}

	@Test
	fun test() {
		val inOutView = DigitalCircuitInOutView().apply { location = Point2D(-259.0,-189.0) }
		val notGateView = LogicGateView.notGateView().apply { location = Point2D(-140.0,-84.0) }
		val andGateView = LogicGateView.andGateView().apply {
			location = Point2D(-7.0,42.0)
			orientation = Direction.SOUTH
		}

		EditModule.drawingAppService.add(inOutView, editor.view)
		EditModule.drawingAppService.add(notGateView, editor.view)
		EditModule.drawingAppService.add(andGateView, editor.view)
		(editor.commandManager as SourcingCommandManager).addSnapshot()

		// Move AND gate
		driver.mouseMoveTo(2,3)
		driver.pressMouseAt(2,3)
		driver.dragMouseTo(-71,51)
		driver.releaseMouseAt(-71,51)

		// Copy/Paste AND gate
		editor.view.selectionManager.select(andGateView)
		EditModule.drawingAppService.copy(editor.view)
		EditModule.drawingAppService.paste(editor.view)

		// Move pasted AND gate
		driver.mouseMoveTo(-57,75)
		driver.pressMouseAt(-57,74)
		driver.dragMouseTo(-7,56)
		driver.releaseMouseAt(-7,56)

		// Select both AND gates
		driver.mouseMoveTo(-153, -19)
		driver.pressMouseAt(-153, -19)
		driver.dragMouseTo(81, 128)
		driver.releaseMouseAt(81, 128)

		// Copy selected AND gates
		EditModule.drawingAppService.copy(editor.view)
		EditModule.drawingAppService.paste(editor.view)

		// Move selected 2 AND gates
		driver.mouseMoveTo(-62, 76)
		driver.pressMouseAt(-62, 76)
		driver.dragMouseTo(90, 55)
		driver.releaseMouseAt(90, 55)

		// Paste 2 AND gates, resulting in 6 AND gates
		EditModule.drawingAppService.paste(editor.view)

		// Connect DigitalCircuitInOutView with NotGateView
		driver.mouseMoveTo(-259, -189)
		driver.pressMouseAt(-259, -189)
		driver.dragMouseTo(-210, -84)
		driver.releaseMouseAt(-210, -84)

		// Connect NotGateView with AND gate 2
		driver.mouseMoveTo(-140, -84)
		driver.pressMouseAt(-140, -84)
		driver.dragMouseTo(-91, 245)

		println()
	}

}