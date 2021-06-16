package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.library.*
import ch.scorpion.jabbah.graph.library.dictionary.FileLibraryDictionaryPersistenceService
import ch.scorpion.jabbah.graph.library.dictionary.LibraryDictionaryService
import ch.scorpion.jabbah.graph.project.ProjectManagementService
import ch.scorpion.jabbah.graph.project.ProjectModule
import ch.scorpion.jabbah.graph.view.GraphViewBuilder
import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.graph.view.vertice.OpenSubGraphRequest
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeView
import ch.scorpion.jabbah.graph.view.vertice.TestVerticeView
import java.nio.file.Files
import kotlin.test.BeforeTest
import kotlin.test.Ignore
import kotlin.test.Test

class GrandUiIntegrationTest {

	companion object {
		init {
			GraphViewTestRule.configure()

			val tempDir = Files.createTempDirectory(null)

			val librariesDir = "${tempDir}/libraries"
			LibraryModule.userLibraryPersistenceService = FileLibraryPersistenceService(librariesDir)
			LibraryModule.libraryService = LibraryService()
			LibraryModule.userLibraryDictionaryService = LibraryDictionaryService(FileLibraryDictionaryPersistenceService(librariesDir))
			LibraryModule.systemLibraryDictionaryService = LibraryDictionaryService(FileLibraryDictionaryPersistenceService(librariesDir))
			LibraryModule.libraryFactory = EmptyLibraryFactory()
			LibraryModule.libraryManagementService = LibraryManagementService()

			val projectsDir = "${tempDir}/projects"
			ProjectModule.projectLibraryPersistenceService = FileLibraryPersistenceService(projectsDir)
			ProjectModule.projectLibraryService = { LibraryService(userLibraryPersister = ProjectModule.projectLibraryPersistenceService )}
			ProjectModule.projectDictionaryService = LibraryDictionaryService(FileLibraryDictionaryPersistenceService(projectsDir))
			ProjectModule.projectManagementService = ProjectManagementService()

			createUserLibrary()
		}

		private fun createUserLibrary() {
			val service = LibraryModule.libraryManagementService
			val library = service.create(LibraryProperties(name = TranslatableText("DummyLibrary")), null)
			LibraryModule.libraryHolder.l = library
		}
	}

	private val application = TestGraphApplication()

	@BeforeTest
	fun setup() {
		application.start()
	}

	@Ignore
	@Test
	fun shouldUseVirginSubGraphVerticeView() {
		createAndOpenNewProject()
		editGraphView()
		val componentUuid = save()
		createAndOpenNewCircuit()
		val subGraphVV = useComponentInNewCircuit(componentUuid)
		application.startSimulation()
		openSubGraphVerticeView(subGraphVV)
	}

	private fun createAndOpenNewProject() {
		val service = ProjectModule.projectManagementService
		val project = service.create(LibraryProperties(name = TranslatableText("Test")), LibraryModule.libraryHolder.library.uuid)
		service.open(project.uuid)
	}

	private fun editGraphView() {
		val builder = GraphViewBuilder<Boolean>((application.controller.data!!.content as MetaGraph).graph)
		val vv1 = builder.addVerticeView(TestVerticeView.createEastOutputVerticeView("vv1", 0, 0))
		val vv2 = builder.addVerticeView(TestVerticeView.createEastOutputVerticeView("vv2", 100, 0))
		val vv3 = builder.addVerticeView(TestVerticeView.createEastOutputVerticeView("vv3", 100, 100))
		val vv4 = builder.addVerticeView(TestVerticeView.createEastOutputVerticeView("vv4", 100, 200))
		val ev1 = builder.connect(vv1, vv2)
		val split1 = builder.split(ev1, 0, Point2D(50, 0), vv3)
		builder.split(split1.newEdgeView, 0, Point2D(50, 100), vv4)
	}

	private fun save(): UUID {
		application.controller.save()
		return (application.controller.data!!.content as MetaGraph).uuid
	}

	private fun createAndOpenNewCircuit() {
		val project = ProjectModule.projectHolder.project!!
		val metaGraph = MetaGraph.withName("Usage")
		val element = project.libraryService.addContainerLibraryElement(project, metaGraph, project)
		BaseModule.eventBus.post(OpenContainerLibraryElementRequest(element))
	}

	private fun useComponentInNewCircuit(componentUuid: UUID): SubGraphVerticeView<*> {
		val service = GraphViewModule.graphViewAppService
		val project = ProjectModule.projectHolder.project!!
		val element = project.getContainerLibraryElement(componentUuid)!!
		val editor = application.editor

		return service.addGraphElementViewFromLibrary(element, Point2D.ZERO, editor) as SubGraphVerticeView<*>
	}

	private fun openSubGraphVerticeView(vv: SubGraphVerticeView<*>) {
		BaseModule.eventBus.post(OpenSubGraphRequest(vv, newView = false, quickMode = true))
	}
}