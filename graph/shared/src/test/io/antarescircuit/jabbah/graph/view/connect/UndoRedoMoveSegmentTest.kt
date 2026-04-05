package io.antarescircuit.jabbah.graph.view.connect

import io.antarescircuit.jabbah.base.event.Modifier
import io.antarescircuit.jabbah.graph.view.AbstractGraphViewEditingTest
import io.antarescircuit.jabbah.graph.view.EdgeView
import io.antarescircuit.jabbah.graph.view.vertice.TestVerticeView
import kotlin.test.Test
import kotlin.test.assertEquals

import io.antarescircuit.jabbah.graph.view.net.edge.MoveSegmentCommand

/**
 * Regression test for GitHub issue #425, which occurred when replaying a snapshot
 * that contained a [MoveSegmentCommand].
 */
class UndoRedoMoveSegmentTest : AbstractGraphViewEditingTest(snapshotSize = 10) {

	private lateinit var vv1: TestVerticeView
	private lateinit var vv2: TestVerticeView
	private lateinit var vv3: TestVerticeView
	private lateinit var vv4: TestVerticeView
	private lateinit var ev12: EdgeView<Boolean>

	override fun setupCircuit() {
		vv1 = builder.addVerticeView(TestVerticeView.createEastOutputVerticeView("vv1", 0, 0))
		vv2 = builder.addVerticeView(TestVerticeView.createEastOutputVerticeView("vv2", 100, 0))
		ev12 = builder.connect(vv1, vv2)
		vv3 = builder.addVerticeView(TestVerticeView.createEastOutputVerticeView("vv3", 0, 100))
		vv4 = builder.addVerticeView(TestVerticeView.createEastOutputVerticeView("vv4", 100, 100))
	}

	@Test
	fun shouldReplayFromSnapshot() {
		editor.commandManager.reset()

		// Connect vv3/vv4
		driver.mouseMoveTo(20, 100)
		driver.pressMouseAt(20, 100)
		driver.dragMouseTo(80, 100)
		driver.releaseMouseAt(80, 100)

		// Move segment vv3/vv4
		driver.mouseMoveTo(50, 100)
		driver.pressMouseAt(50, 100)
		driver.dragMouseTo(50, 140)
		driver.dragMouseTo(50, 150)
		driver.releaseMouseAt(50, 150)

		// Start creating junction on vv1/vv2 and interrupt immediately by 'mouse release'
		// This will cancel the transaction. The bug in #425 threw a NPE in SourcingCommandManager.rollbackTransaction()
		// and finally in MoveSegmentCommand.getEdgeView()
		driver.mouseMoveTo(50, 0, Modifier.Alt.mask)
		driver.pressMouseAt(50, 0, Modifier.Alt.mask)
		driver.releaseMouseAt(50, 0, Modifier.Alt.mask)

		// First EdgeView still has moved segment
		assertEquals(6, view.drawing.getEdgeViews().first().polyline.pointsCount)

		// Second EdgeView wasn't split
		assertEquals(2, view.drawing.getEdgeViews().size)
		assertEquals(2, view.drawing.getEdgeViews().last().net!!.ports.size)
	}
}