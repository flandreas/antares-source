package ch.scorpion.jabbah.graph.view.net

import ch.scorpion.jabbah.base.TestTranslationsBuilder
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.SelectionManager
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.graph.view.ConnectableView
import ch.scorpion.jabbah.graph.view.GraphViewBuilder
import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import ch.scorpion.jabbah.graph.view.NetView
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.graph.view.vertice.TestVerticeView
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.sameInstance
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.ClassRule
import org.junit.Test

/** An integration test for undoing deletion of [NetView]s.*/
class UndoDeleteNetViewsTest {

	companion object {
		@ClassRule
		@JvmField
		val rule = GraphViewTestRule()
	}

	private lateinit var builder: GraphViewBuilder<Boolean>

	private val drawingView = mock<DrawingView<Drawing<Component>>>()

	private val selectionManager = mock<SelectionManager>()

	@Before
	fun setup(){
		TestTranslationsBuilder().withAnyKey()
		builder = GraphViewBuilder()
		whenever(drawingView.drawing).thenReturn(builder.graphView as Drawing<Component>)
		whenever(drawingView.selectionManager).thenReturn(selectionManager)
	}

	@Test
	fun shouldUndoDeleteNetView() {
		val v1 = builder.addVerticeView(createVerticeView(0, 0, Direction.EAST))
		val v2 = builder.addVerticeView(createVerticeView(100, 0, Direction.WEST))
		val v3 = builder.addVerticeView(createVerticeView(100, 100, Direction.WEST))
		val v1v2 = builder.connect(v1, v2)
		val split = builder.split(v1v2, 0, Point2D(50, 0), v3)

		GraphViewModule.graphViewService.delete(builder.graphView.getDrawables().toList(), drawingView)
		EditModule.commandManager.undo()

		/*
		assertThat(v1v2.origin, `is`(sameInstance(v1 as ConnectableView)))
		assertThat(v1v2.destination, `is`(sameInstance(split.nodeView as ConnectableView)))
		assertThat(split.newEdgeView.destination, `is`(sameInstance(v2 as ConnectableView)))
		*/
		assertThat(builder.graphView.getEdgeViews().size, `is`(3))
	}

	private fun createVerticeView(x: Int, y: Int, dir: Direction): TestVerticeView {
		return TestVerticeView(loc = Point2D(x, y), inputDirection = dir, portViewLength = 20)
	}
}