package ch.scorpion.jabbah.graph.ui.graphpanel

import ch.scorpion.jabbah.app.ApplicationData
import ch.scorpion.jabbah.app.Savable
import ch.scorpion.jabbah.base.event.EventBusImpl
import ch.scorpion.jabbah.draw.view.DrawViewModule
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.edit.view.DrawingViewImpl
import ch.scorpion.jabbah.graph.*
import ch.scorpion.jabbah.graph.container.ContainerDrawing
import ch.scorpion.jabbah.graph.library.*
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVerticeRef
import ch.scorpion.jabbah.graph.ui.TestGraphApplication
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.GraphViewBuilder
import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import ch.scorpion.jabbah.graph.view.editor.GraphEditor
import ch.scorpion.jabbah.graph.view.graph.GraphViewImpl
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.graph.view.vertice.OpenSubGraphRequest
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeView
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * An integration test for testing editability of a [GraphView] in a [GraphPanelView]
 * under various conditions.
 */
class GraphPanelViewEditabilityTest {

	companion object {
		init {
			GraphViewTestRule.configure()

			LibraryModule.userLibraryPersistenceService = MemoryLibraryPersistenceService()
			LibraryModule.libraryService = LibraryService()
			LibraryModule.libraryHolder.l = LibraryImpl(TranslatableText("test"))
		}
	}

	private val eventBus = EventBusImpl()
	private val application = TestGraphApplication({ mockk() }, eventBus = eventBus)
	private val graphViewBuilder = GraphViewBuilder<Boolean>()
	private val vv = createSubGraphVerticeView()
	private val canvas = VirtualCanvas { DrawingViewImpl(GraphViewImpl() as Drawing<Component>, canvas = it, eventBus = eventBus) }
	private val editor = GraphEditor(canvas.view as DrawingView<Drawing<Component>>, eventBus)
	private val controller = GraphPanelViewController(editor, eventBus = eventBus)

	init {
		graphViewBuilder.addVerticeView(vv)
		application.controller.view = mockk(relaxed = true)

		GraphViewModule.applicationModeHolder = controller
		GraphPanelViewMockBuilder(controller)
		DrawViewModule.viewManager.activeView = editor.view
	}

	@Test
	fun shouldBeEditableWithEditableSavable() {
		establishEditableData()
		assertEditableUI()
	}

	@Test
	fun shouldNotBeEditableWithNonEditableSavable() {
		establishNonEditableData()
		assertNonEditableUI(canSelect = true)
	}

	@Test
	fun shouldNotBeEditableWhenExecuting() {
		establishEditableData()
		controller.setMode(ApplicationMode.EXECUTE)
		assertNonEditableUI(canSelect = false)
	}

	@Test
	fun shouldNotBeEditableAfterDescending() {
		establishEditableData()
		eventBus.post(OpenSubGraphRequest(vv, newView = false, quickMode = true))
		assertNonEditableUI(canSelect = false)
	}

	private fun assertEditableUI() {
		assertTrue(controller.editor.view.editable)
		assertTrue(controller.editor.active)
		assertTrue(canSelectComponent())
		assertTrue(canMoveComponent())
	}

	private fun assertNonEditableUI(canSelect: Boolean) {
		assertFalse(controller.editor.view.editable)
		assertEquals(canSelect, controller.editor.active)
		assertEquals(canSelect, canSelectComponent())
		assertFalse(canMoveComponent())
	}

	private fun canSelectComponent(): Boolean {
		val location = vv.boundingBox.center
		canvas.pressMouseAt(location.xInt, location.yInt)
		return editor.view.content.selectionManager.selectionCount == 1
	}

	private fun canMoveComponent(): Boolean {
		val location = vv.boundingBox.center
		canvas
			.pressMouseAt(location.xInt, location.yInt)
			.dragMouse(50, 0)
			.releaseMouse()
		return editor.commandManager.canUndo()
	}

	private fun establishEditableData() {
		application.controller.data = applicationData(SavableMockBuilder().editable().build())
	}

	private fun establishNonEditableData() {
		application.controller.data = applicationData(SavableMockBuilder().nonEditable().build())
	}

	private fun applicationData(savable: Savable): ApplicationData = ApplicationData(
		MetaGraph(
			graph = GraphStorable(graphViewBuilder.build()),
			containerDrawing = ContainerDrawing(),
			eventBus = eventBus),
		savable)

	private fun createSubGraphVerticeView(): SubGraphVerticeView<*> {
		val library = LibraryModule.libraryHolder.library
		TestLibraryBuilder().addInnerCustomComponent(library)
		return (library.get(TestLibraryBuilder.INNER_CUSTOM_COMP) as LibraryElement)
			.getNewInstance<SubGraphVerticeRef>() as SubGraphVerticeView<*>
	}
}