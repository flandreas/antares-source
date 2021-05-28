package ch.scorpion.jabbah.graph.view.net

import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.SelectionManager
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.graph.view.GraphViewBuilder
import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import ch.scorpion.jabbah.graph.view.NetView
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.graph.view.vertice.TestVerticeView
import io.mockk.every
import io.mockk.mockk
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/** An integration test for undoing deletion of [NetView]s.*/
class UndoDeleteNetViewsTest {

	companion object {
		init {
			GraphViewTestRule.configure()
		}
	}

	private lateinit var builder: GraphViewBuilder<Boolean>

	private val drawingView = mockk<DrawingView<Drawing<Component>>>()

	private val selectionManager = mockk<SelectionManager>(relaxed = true)

	@BeforeTest
	fun setup(){
		builder = GraphViewBuilder()
		EditModule.commandManager.bindDataHolder(builder)

		every { drawingView.drawing } returns builder.graphView as Drawing<Component>
		every { drawingView.selectionManager } returns selectionManager
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