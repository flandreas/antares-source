package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.TempFileLibraryTestRule
import ch.scorpion.jabbah.graph.library.LibraryModule
import ch.scorpion.jabbah.graph.library.LibraryProperties
import ch.scorpion.jabbah.graph.library.OpenContainerLibraryElementRequest
import ch.scorpion.jabbah.graph.project.Project
import ch.scorpion.jabbah.graph.project.ProjectModule
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.GraphViewBuilder
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.graph.view.vertice.OpenSubGraphRequest
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeView
import ch.scorpion.jabbah.graph.view.vertice.TestVerticeView
import kotlin.test.*

class GrandUiIntegrationTest {

	companion object {
		init {
			TempFileLibraryTestRule.configure()
		}
	}

	private lateinit var application: TestGraphApplication

	@BeforeTest
	fun beforeTest() {
		BaseModule.eventBus.clear()
	}

	@AfterTest
	fun afterTest() {
		application.stopSimulation()
	}

	@Test
	fun shouldUseVirginSubGraphVerticeView() {
		TempFileLibraryTestRule.createAndEstablishCurrentLibrary("Lib1")
		application = TestGraphApplication()
		GraphFrameMockBuilder(application.graphFrameController)
		application.start()

		createAndOpenNewProject("Test1")
		val graphView = editGraphView()
		val v1Id = graphView.getVerticeView("vv1")!!.model.id
		val metaGraphUuid = save()
		createAndOpenNewMetaGraph()
		val subGraphVV = useContainerLibraryElement(metaGraphUuid)
		application.startSimulation()
		openSubGraphVerticeView(subGraphVV, newView = false)

		val displayedGraphView = application.editor.view.drawing as GraphView
		assertSame(
			subGraphVV.model.getGraphIfPresent()!!.withId(v1Id),
			displayedGraphView.getVerticeView("vv1")!!.model
		)
	}

	@Test
	fun shouldDetachFromModelsWhenClosingSecondView() {
		TempFileLibraryTestRule.createAndEstablishCurrentLibrary("Lib2")
		application = TestGraphApplication()
		GraphFrameMockBuilder(application.graphFrameController)
		application.start()

		createAndOpenNewProject("Test2")
		val graphView = editGraphView()
		val v1Id = graphView.getVerticeView("vv1")!!.model.id
		val metaGraphUuid = save()
		createAndOpenNewMetaGraph()
		val subGraphVV = useContainerLibraryElement(metaGraphUuid)
		openSubGraphVerticeView(subGraphVV, newView = true)

		application.startSimulation()

		val desktop = application.graphFrameController.graphPanelViewController.desktopController

		assertTrue(subGraphVV.model.getGraphIfPresent()!!.elements.first { it.id == v1Id }.isBreakpoint)

		desktop.closeItem(desktop.additionalDesktopItems.first())

		assertEquals(0, desktop.additionalDesktopItems.size)
		assertFalse(subGraphVV.model.getGraphIfPresent()!!.elements.first { it.id == v1Id }.isBreakpoint)
	}

	private fun createAndOpenNewProject(name: String) {
		val service = ProjectModule.projectManagementService
		val project = service.invoke().create(LibraryProperties(name = TranslatableText(name)))
		service.invoke().open(project.identification)
	}

	private fun editGraphView(): GraphView {
		val builder = GraphViewBuilder<Boolean>((application.controller.data!!.content as MetaGraph).graph)
		val vv1 = builder.addVerticeView(TestVerticeView.createEastOutputVerticeView("vv1", 0, 0))
		val vv2 = builder.addVerticeView(TestVerticeView.createEastOutputVerticeView("vv2", 100, 0))
		val vv3 = builder.addVerticeView(TestVerticeView.createEastOutputVerticeView("vv3", 100, 100))
		val vv4 = builder.addVerticeView(TestVerticeView.createEastOutputVerticeView("vv4", 100, 200))
		val ev1 = builder.connect(vv1, vv2)
		val split1 = builder.split(ev1, 0, Point2D(50, 0), vv3)
		builder.split(split1.newEdgeView, 0, Point2D(50, 100), vv4)
		return builder.build()
	}

	private fun save(): UUID {
		application.controller.save()
		return (application.controller.data!!.content as MetaGraph).uuid
	}

	private fun createAndOpenNewMetaGraph() {
		val project = LibraryModule.libraryHolder.library as Project
		val metaGraph = MetaGraph.withName("Usage")
		val element = project.libraryService.addContainerLibraryElement(project, metaGraph, project)
		BaseModule.eventBus.post(OpenContainerLibraryElementRequest(element))
	}

	private fun useContainerLibraryElement(componentUuid: UUID): SubGraphVerticeView<*> {
		val service = GraphViewModule.graphViewAppService
		val project = LibraryModule.libraryHolder.library as Project
		val element = project.getContainerLibraryElement(componentUuid)!!
		val editor = application.editor

		return service.addGraphElementViewFromLibrary(element, Point2D.ZERO, editor) as SubGraphVerticeView<*>
	}

	private fun openSubGraphVerticeView(vv: SubGraphVerticeView<*>, newView: Boolean) {
		BaseModule.eventBus.post(OpenSubGraphRequest(vv, newView = newView, quickMode = true))
	}
}