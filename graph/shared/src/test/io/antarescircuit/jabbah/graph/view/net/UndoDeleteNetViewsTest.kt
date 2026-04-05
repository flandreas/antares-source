package io.antarescircuit.jabbah.graph.view.net

import io.antarescircuit.jabbah.base.geom.Direction
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.edit.Component
import io.antarescircuit.jabbah.edit.DrawingViewMockBuilder
import io.antarescircuit.jabbah.edit.SelectionManager
import io.antarescircuit.jabbah.edit.module.EditModule
import io.antarescircuit.jabbah.graph.view.GraphViewBuilder
import io.antarescircuit.jabbah.graph.view.GraphViewTestRule
import io.antarescircuit.jabbah.graph.view.NetView
import io.antarescircuit.jabbah.graph.view.module.GraphViewModule
import io.antarescircuit.jabbah.graph.view.vertice.TestVerticeView
import dev.mokkery.MockMode
import dev.mokkery.mock
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/** An integration test for undoing deletion of [NetView]s.*/
class UndoDeleteNetViewsTest {

	private lateinit var builder: GraphViewBuilder<Boolean>

	private val selectionManager = mock<SelectionManager>(MockMode.autofill)

	private val drawingView = DrawingViewMockBuilder()
		.withSelectionManager(selectionManager)
		.withDrawingAccessor { builder.build() }
		.build<Component>()

	@BeforeTest
	fun setup(){
		GraphViewTestRule.configure()
		builder = GraphViewBuilder()
		EditModule.commandManager.bindDataHolder(builder)
	}

	@Test
	fun shouldUndoDeleteNetView() {
		val v1 = builder.addVerticeView(createVerticeView(0, 0, Direction.EAST))
		val v2 = builder.addVerticeView(createVerticeView(100, 0, Direction.WEST))
		val v3 = builder.addVerticeView(createVerticeView(100, 100, Direction.WEST))
		val v1v2 = builder.connect(v1, v2)
		builder.split(v1v2, 0, Point2D(50, 0), v3)

		EditModule.commandManager.reset()

		GraphViewModule.graphViewAppService.delete(builder.graphView.drawables.toList(), drawingView)
		EditModule.commandManager.undo()

		val edgeViews = builder.graphView.getEdgeViews()
		assertEquals(3, edgeViews.size)
	}

	private fun createVerticeView(x: Int, y: Int, dir: Direction): TestVerticeView {
		return TestVerticeView(loc = Point2D(x, y), inputDirection = dir, portViewLength = 20)
	}
}