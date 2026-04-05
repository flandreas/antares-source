package io.antarescircuit.jabbah.graph.ui

import io.antarescircuit.jabbah.base.UUID
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.base.geom.Rotation
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.edit.auth.DesktopUser
import io.antarescircuit.jabbah.edit.auth.DesktopUserHolder
import io.antarescircuit.jabbah.edit.auth.EditAuthModule
import io.antarescircuit.jabbah.edit.model.text.TranslatableText
import io.antarescircuit.jabbah.graph.MetaGraph
import io.antarescircuit.jabbah.graph.TempFileLibraryTestRule
import io.antarescircuit.jabbah.graph.library.LibraryModule
import io.antarescircuit.jabbah.graph.library.LibraryProperties
import io.antarescircuit.jabbah.graph.library.OpenContainerLibraryElementRequest
import io.antarescircuit.jabbah.graph.model.GenericGraphType
import io.antarescircuit.jabbah.graph.project.Project
import io.antarescircuit.jabbah.graph.project.ProjectModule
import io.antarescircuit.jabbah.graph.ui.desktop.GraphDesktopViewItemElementDepthRef
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.GraphViewBuilder
import io.antarescircuit.jabbah.graph.view.module.GraphViewModule
import io.antarescircuit.jabbah.graph.view.vertice.OpenSubGraphRequest
import io.antarescircuit.jabbah.graph.view.vertice.SubGraphVerticeView
import io.antarescircuit.jabbah.graph.view.vertice.TestVerticeView
import kotlin.test.*

class GrandUiIntegrationTest {

	private lateinit var application: TestGraphApplication<GraphFrame>

	@BeforeTest
	fun beforeTest() {
		TempFileLibraryTestRule.configure()
		EditAuthModule.userHolder = DesktopUserHolder(DesktopUser.Companion.developer)
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
		application = TestGraphApplication<GraphFrame>()
		val graphFrameBuilder = GraphFrameMockBuilder(application.graphFrameController)
		application.start()

		createAndOpenNewProject("Test2")
		val graphView = editGraphView()
		val v1Id = graphView.getVerticeView("vv1")!!.model.id
		val metaGraphUuid = save()
		createAndOpenNewMetaGraph()
		val subGraphVV = useContainerLibraryElement(metaGraphUuid)
		graphFrameBuilder.graphPanelViewBuilder.graphEditViewBuilder.withCreatedElementRef(
			GraphDesktopViewItemElementDepthRef(subGraphVV.id, 0))
		openSubGraphVerticeView(subGraphVV, newView = true)

		application.startSimulation()

		val desktop = application.graphFrameController.graphPanelViewController.desktopController

		assertTrue(subGraphVV.model.getGraphIfPresent()!!.elements.first { it.id == v1Id }.isBreakpoint)

		val item = desktop.additionalDesktopItems.first()
		desktop.closeItem(item)

		assertFalse(subGraphVV.model.getGraphIfPresent()!!.elements.first { it.id == v1Id }.isBreakpoint)
	}

	private fun createAndOpenNewProject(name: String) {
		val service = ProjectModule.projectManagementService
		val project = service.create(LibraryProperties(name = TranslatableText(name)))
		service.open(project.identification)
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
		val metaGraph = MetaGraph.create(TranslatableText("Usage"), GenericGraphType)
		val element = project.libraryService.addContainerLibraryElement(project, metaGraph, project)
		BaseModule.eventBus.post(OpenContainerLibraryElementRequest(element))
	}

	private fun useContainerLibraryElement(componentUuid: UUID): SubGraphVerticeView<*> {
		val service = GraphViewModule.graphViewAppService
		val project = LibraryModule.libraryHolder.library as Project
		val element = project.getContainerLibraryElement(componentUuid)!!
		val editor = application.editor

		return service.addGraphElementViewFromLibrary(element, Point2D.ZERO, Rotation.R0, editor) as SubGraphVerticeView<*>
	}

	private fun openSubGraphVerticeView(vv: SubGraphVerticeView<*>, newView: Boolean) {
		BaseModule.eventBus.post(OpenSubGraphRequest(vv, newView = newView, quickMode = true))
	}
}