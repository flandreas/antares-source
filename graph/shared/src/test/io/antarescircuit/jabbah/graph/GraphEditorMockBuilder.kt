package io.antarescircuit.jabbah.graph

import io.antarescircuit.jabbah.edit.*
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.graph.GraphViewImpl
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock

/**
 * Builds test mocks for [Editor].
 */
class GraphEditorMockBuilder {

	val editor: Editor = mock(MockMode.autofill)
	private val view: DrawingView<GraphView> = mock(MockMode.autofill)

	init {
		withDrawing(GraphViewImpl())
		every { editor.view } returns view as DrawingView<Drawing<Component>>
	}

	fun withDrawingView(view: DrawingView<GraphView>): GraphEditorMockBuilder {
		every { editor.view } returns (view as DrawingView<Drawing<Component>>)
		return this
	}

	fun withDrawing(graphView: GraphView): GraphEditorMockBuilder {
		every { editor.drawing } returns (graphView as Drawing<Component>)
		every { view.drawing } returns graphView
		return this
	}

	fun withGrid(grid: Grid): GraphEditorMockBuilder {
		every { view.grid } returns grid
		return this
	}

	fun withGridDistance(distance: Double): GraphEditorMockBuilder {
		val grid: Grid = mock()
		every { grid.distance } returns distance
		every { view.grid } returns grid
		return this
	}

	fun withSelectionManager(): GraphEditorMockBuilder {
		every { view.selectionManager } returns mock()
		return this
	}

	fun build(): Editor = editor
}