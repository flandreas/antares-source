package ch.scorpion.jabbah.graph.ui.graphpanel

import ch.scorpion.jabbah.app.ApplicationData
import ch.scorpion.jabbah.app.ApplicationDataEvent
import ch.scorpion.jabbah.app.Savable
import ch.scorpion.jabbah.base.event.EventBusImpl
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.draw.view.DrawViewModule
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.editor.EditorImpl
import ch.scorpion.jabbah.edit.view.DrawingViewImpl
import ch.scorpion.jabbah.graph.GraphStorable
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.SavableMockBuilder
import ch.scorpion.jabbah.graph.VirtualCanvas
import ch.scorpion.jabbah.graph.view.GraphViewBuilder
import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import ch.scorpion.jabbah.graph.view.graph.GraphViewImpl
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.graph.view.vertice.TestVerticeView
import io.mockk.mockk
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertTrue

class GraphPanelViewEditabilityTest {

	companion object {
		init {
			GraphViewTestRule.configure()
		}
	}

	private val eventBus = EventBusImpl()
	private val graphViewBuilder = GraphViewBuilder<Boolean>()
	private val canvas = VirtualCanvas { DrawingViewImpl(GraphViewImpl() as Drawing<Component>, canvas = it, eventBus = eventBus) }
	private val editor = EditorImpl(canvas.view as DrawingView<Drawing<Component>>)
	private val controller = GraphPanelViewController(editor, eventBus = eventBus)

	init {
		GraphViewModule.applicationModeHolder = controller
		GraphPanelViewMockBuilder(controller)
		DrawViewModule.viewManager.activeView = editor.view

		graphViewBuilder.addVerticeView(TestVerticeView(loc = Point2D(100, 100), width = 100, height = 100))
	}

	@Test
	@Ignore
	/** TODO Does not work yet due to inversion of AffineTransform in View seems to not work correctly.*/
	fun shouldBeEditableWithEditableSavable() {
		establishEditableData()
		assertTrue(controller.editor.active)
		assertTrue(canSelectComponent())
	}

	private fun canSelectComponent():Boolean {
		canvas.pressMouseAt(150, 150)
		return editor.view.content.selectionManager.selectionCount == 1
	}

	private fun establishEditableData() {
		val savable = SavableMockBuilder().editable().build()
		eventBus.post(ApplicationDataEvent(null, applicationData(savable)))
	}

	private fun applicationData(savable: Savable): ApplicationData {
		return ApplicationData(
			MetaGraph(
				graph = GraphStorable(graphViewBuilder.build()),
				containerDrawing = mockk(relaxed = true),
				eventBus = eventBus),
			savable)
	}
}