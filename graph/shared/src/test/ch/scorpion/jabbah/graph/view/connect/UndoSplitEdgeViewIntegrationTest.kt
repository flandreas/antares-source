package ch.scorpion.jabbah.graph.view.connect

import ch.scorpion.jabbah.base.event.Modifier
import ch.scorpion.jabbah.base.geom.Dimension2D
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.Canvas
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.command.SourcingCommandManager
import ch.scorpion.jabbah.edit.editor.EditEditorModule
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.graph.view.EditorToolDriver
import ch.scorpion.jabbah.graph.view.GraphViewBuilder
import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import ch.scorpion.jabbah.graph.view.VerticeView
import ch.scorpion.jabbah.graph.view.graph.GraphViewImpl
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.graph.view.vertice.TestVerticeView
import ch.scorpion.jabbah.io.StorableCloner
import io.mockk.every
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals

/** Regression test for GitHub bug #218. */
class UndoSplitEdgeViewIntegrationTest {

	companion object {
		init {
			GraphViewTestRule.configure()
		}
	}

	private val builder: GraphViewBuilder<Boolean> = GraphViewBuilder {
		builder -> drawingView.drawing = builder.graphView as Drawing<Component>
	}
	private val drawingView = EditModule.drawingViewFactory.invoke(builder.graphView as Drawing<Component>)
	private val editor: Editor = EditEditorModule.createEditor(drawingView)
	private val driver = EditorToolDriver(editor)
	private val service = GraphViewModule.graphViewAppService



	init {
		BaseModule.properties.set(SourcingCommandManager.PROP_MAX_COMMAND_COUNT_PER_SNAPSHOT, 2)

		setupCircuit()

		val canvas = mockk<Canvas>(relaxed = true)
		every { canvas.dimension } returns Dimension2D(1000, 1000)
		every { canvas.devicePixelRatio } returns 1
		drawingView. canvas = canvas

		GraphViewImpl.inputEventHandler = null
		editor.commandManager.bindDataHolder(builder)
	}

	private fun setupCircuit() {
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
		println("--- Before undo")
		StorableCloner.clone(builder.graphStorable)

		editor.commandManager.undo()
		assertEquals(1, builder.graphView.getEdgeViews().size)

		println("--- After undo")
		StorableCloner.clone(builder.graphStorable)

		// Connect v2 and v4
		driver.mouseMoveTo(130, 200)
		driver.pressMouseAt(130, 200)
		driver.dragMouseTo(190, 200)
		driver.releaseMouseAt(190, 200)
		assertEquals(2, builder.graphView.getEdgeViews().size)

		val v1 = builder.graphView.getDrawable { it is VerticeView<*> && it.model.name == "v1" } as VerticeView<*>
		service.move(listOf(v1), Point2D(-10, 0), editor, register = false, emptyList())
	}
}