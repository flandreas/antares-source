package ch.scorpion.jabbah.graph.view.connect

import ch.scorpion.jabbah.base.event.Modifier
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.graph.health.GraphViewConsistencyCheck
import ch.scorpion.jabbah.graph.view.*
import ch.scorpion.jabbah.graph.view.vertice.TestVerticeView
import ch.scorpion.jabbah.io.StorableCloner
import kotlin.test.Test
import kotlin.test.assertEquals

/** Regression test for GitHub bug #218. */
class UndoSplitEdgeViewIntegrationTest: AbstractGraphViewEditingTest() {

	override fun setupCircuit() {
		builder.addVerticeView(TestVerticeView.createEastOutputVerticeView("v1", 100, 100))
		builder.addVerticeView(TestVerticeView.createEastOutputVerticeView("v2", 200, 100))
		builder.addVerticeView(TestVerticeView.createEastOutputVerticeView("v3", 100, 200))
		builder.addVerticeView(TestVerticeView.createEastOutputVerticeView("v4", 200, 200))
	}

	@Test
	fun test() {
		// Connect v1 and v2
		driver.mouseMoveTo(130, 100)
		driver.pressMouseAt(130, 100)
		driver.dragMouseTo(190, 100)
		driver.releaseMouseAt(190, 100)
		assertEquals(1, builder.graphView.getEdgeViews().size)

		// Split to v4
		driver.mouseMoveTo(150, 100, modifiers = Modifier.Alt.mask)
		driver.pressMouseAt(150, 100, modifiers = Modifier.Alt.mask)
		driver.dragMouseTo(190, 200)
		driver.releaseMouseAt(190, 200)
		assertEquals(3, builder.graphView.getEdgeViews().size)

		// Undo split
		StorableCloner.clone(builder.graphStorable)

		editor.commandManager.undo()
		assertEquals(1, builder.graphView.getEdgeViews().size)

		StorableCloner.clone(builder.graphStorable)

		// Connect v2 and v4
		driver.mouseMoveTo(130, 200)
		driver.pressMouseAt(130, 200)
		driver.dragMouseTo(190, 200)
		driver.releaseMouseAt(190, 200)
		assertEquals(2, builder.graphView.getEdgeViews().size)

		val v1 = builder.graphView.getDrawable { it is VerticeView<*> && it.model.name == "v1" } as VerticeView<*>
		service.move(listOf(v1), Point2D(-10, 0), editor, register = false, emptyList())

		GraphViewConsistencyCheck.execute(builder.graphView)
	}
}