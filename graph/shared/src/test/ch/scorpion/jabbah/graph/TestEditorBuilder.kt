package ch.scorpion.jabbah.graph

import ch.scorpion.jabbah.edit.*
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.graph.GraphViewImpl
import io.mockk.every
import io.mockk.mockk

/**
 * Builds test mocks for [Editor].
 * TODO Copy/Paste of corresponding class in edit package. Wasn't able to reuse it with gradle.
 */
class TestEditorBuilder {

	val editor: Editor = mockk(relaxed = true)
	private val view: DrawingView<GraphView> = mockk(relaxed = true)

	init {
		withDrawing(GraphViewImpl())
		every { editor.view } returns view as DrawingView<Drawing<Component>>
	}

	fun withDrawing(graphView: GraphView): TestEditorBuilder {
		every { editor.drawing } returns (graphView as Drawing<Component>)
		every { view.drawing } returns graphView
		return this
	}

	fun withGrid(grid: Grid): TestEditorBuilder {
		every { view.grid } returns grid
		return this
	}

	fun withGridDistance(distance: Double): TestEditorBuilder {
		val grid: Grid = mockk()
		every { grid.distance } returns distance
		every { view.grid } returns grid
		return this
	}

	fun withSelectionManager(): TestEditorBuilder {
		every { view.selectionManager } returns mockk()
		return this
	}
}