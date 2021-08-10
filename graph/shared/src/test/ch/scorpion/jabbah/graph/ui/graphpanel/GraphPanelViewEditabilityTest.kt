package ch.scorpion.jabbah.graph.ui.graphpanel

import ch.scorpion.jabbah.app.ApplicationData
import ch.scorpion.jabbah.app.Savable
import ch.scorpion.jabbah.base.event.EventBusImpl
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.view.DrawViewModule
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.graph.GraphStorable
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.SavableMockBuilder
import ch.scorpion.jabbah.graph.TestLibraryBuilder
import ch.scorpion.jabbah.graph.app.ApplicationMode
import ch.scorpion.jabbah.graph.container.ContainerDrawing
import ch.scorpion.jabbah.graph.library.*
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVerticeRef
import ch.scorpion.jabbah.graph.ui.GraphFrameMockBuilder
import ch.scorpion.jabbah.graph.ui.TestGraphApplication
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.GraphViewBuilder
import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import ch.scorpion.jabbah.graph.view.vertice.OpenSubGraphRequest
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeView
import ch.scorpion.jabbah.graph.view.vertice.TestVerticeView
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

	private val application: TestGraphApplication
	private val controller: GraphPanelViewController

	init {
		BaseModule.eventBus = EventBusImpl()

		application = TestGraphApplication()
		application.controller.view = mockk(relaxed = true)
		controller = application.graphFrameController.graphPanelViewController

		GraphFrameMockBuilder(application.graphFrameController)
		DrawViewModule.viewManager.activeView = application.editor.view
	}

	private fun findSubGraphVerticeView(): SubGraphVerticeView<*> =
		application.editor.drawing.getDrawable { it is SubGraphVerticeView<*> } as SubGraphVerticeView<*>

	private fun findTestVerticeView(): TestVerticeView =
		application.editor.drawing.getDrawable { it is TestVerticeView } as TestVerticeView

	@Test
	fun shouldBeEditableWithEditableSavable() {
		establishEditableData()
		assertEditableUI(findSubGraphVerticeView())
	}

	@Test
	fun shouldNotBeEditableWithNonEditableSavable() {
		establishNonEditableData()
		assertNonEditableUI(canSelect = true, findSubGraphVerticeView())
	}

	@Test
	fun shouldNotBeEditableWhenExecuting() {
		establishEditableData()
		controller.applicationModeHolder.setMode(ApplicationMode.EXECUTE)
		assertNonEditableUI(canSelect = false, findSubGraphVerticeView())
	}

	@Test
	fun shouldNotBeEditableAfterDescending() {
		establishEditableData()
		BaseModule.eventBus.post(OpenSubGraphRequest(findSubGraphVerticeView(), newView = false, quickMode = true))
		assertNonEditableUI(canSelect = true, findTestVerticeView())
	}

	private fun assertEditableUI(component: Component) {
		assertTrue(controller.editor.view.editable)
		assertTrue(controller.editor.active)
		assertTrue(canSelectComponent(component))
		assertTrue(canMoveComponent(component))
	}

	private fun assertNonEditableUI(canSelect: Boolean, component: Component) {
		assertFalse(controller.editor.view.editable)
		assertEquals(canSelect, controller.editor.active)
		assertEquals(canSelect, canSelectComponent(component))
		assertFalse(canMoveComponent(component))
	}

	private fun canSelectComponent(component: Component): Boolean {
		val location = component.boundingBox.center
		application.canvas.pressMouseAt(location.xInt, location.yInt)
		return application.editor.view.content.selectionManager.selectionCount == 1
	}

	private fun canMoveComponent(component: Component): Boolean {
		val location = component.boundingBox.center
		application.canvas
			.pressMouseAt(location.xInt, location.yInt)
			.dragMouse(50, 0)
			.releaseMouse()
		return application.editor.commandManager.canUndo()
	}

	private fun establishEditableData() {
		application.controller.data = applicationData(SavableMockBuilder().editable().build())
	}

	private fun establishNonEditableData() {
		application.controller.data = applicationData(SavableMockBuilder().nonEditable().build())
	}

	private fun applicationData(savable: Savable): ApplicationData {
		val graphViewBuilder = GraphViewBuilder<Boolean>()
		val vv = createSubGraphVerticeView()
		graphViewBuilder.addVerticeView(vv)
		return ApplicationData(
			MetaGraph(
				graph = GraphStorable(graphViewBuilder.build()),
				containerDrawing = ContainerDrawing()),
			savable)
	}

	private fun createSubGraphVerticeView(): SubGraphVerticeView<*> {
		val library = LibraryModule.libraryHolder.library
		TestLibraryBuilder().addInnerCustomComponent(library)
		return (library.get(TestLibraryBuilder.INNER_CUSTOM_COMP) as LibraryElement)
			.getNewInstance<SubGraphVerticeRef>() as SubGraphVerticeView<*>
	}
}