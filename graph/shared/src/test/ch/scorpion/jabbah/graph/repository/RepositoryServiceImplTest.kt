package ch.scorpion.jabbah.graph.repository

import ch.scorpion.jabbah.edit.model.text.description.Name
import ch.scorpion.jabbah.graph.GraphStorable
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.container.ContainerDrawing
import ch.scorpion.jabbah.graph.library.*
import ch.scorpion.jabbah.graph.project.Project
import ch.scorpion.jabbah.graph.project.ProjectImpl
import ch.scorpion.jabbah.graph.project.ProjectModule
import ch.scorpion.jabbah.graph.view.GraphViewBuilder
import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import kotlin.test.*

class RepositoryServiceImplTest {

	companion object {
		init {
			GraphViewTestRule.configure()
		}
	}

	private val libraryPersistenceService = MemoryLibraryPersistenceService()
	private val libraryService: LibraryService = LibraryService(userLibraryPersister = libraryPersistenceService)
	private val libraryBuilder = LibraryBuilder(name = "Library", libraryService = libraryService)

	private val projectPersistenceService = MemoryLibraryPersistenceService()
	private val projectLibraryService: LibraryService = LibraryService(userLibraryPersister = projectPersistenceService)
	private val projectBuilder = LibraryBuilder(name = "Project", libraryService = projectLibraryService,
		library = ProjectImpl("Project", "", projectLibraryService))

	private val service = RepositoryServiceImpl(libraryService = libraryService, projectLibraryService = projectLibraryService)

	@BeforeTest
	fun setup() {
		LibraryModule.libraryHolder.l = libraryBuilder.library
		ProjectModule.projectHolder.p = projectBuilder.library as Project
	}

	@Test
	fun shouldMoveContainerLibraryElementFromProjectToLibrary() {
		val project = projectBuilder
			.addDirectory("ProjectDirectory")
			.addContainerLibraryElement("Element")
			.library
		val library = libraryBuilder
			.addDirectory("Directory")
			.library

		service.move(project.directory.getRecursively("Element") as ContainerLibraryElement, library.directory.getRecursively("Directory") as LibraryDirectory)

		assertNull(project.directory.getRecursively("Element"))
		assertNotNull(library.directory.getRecursively("Element"))
		assertTrue((library.directory.getRecursively("Directory") as LibraryDirectory).contains(library.directory.getRecursively("Element") as LibraryItem))
	}

	@Test
	fun shouldMoveWithinDirectory() {
		val project = projectBuilder
			.addDirectory("ProjectDirectory")
			.addContainerLibraryElement("Element")
			.addContainerLibraryElement("Element2")
			.addContainerLibraryElement("Element3")
			.library

		service.move(
			project.directory.getRecursively("Element") as ContainerLibraryElement,
			project.directory.getRecursively("ProjectDirectory") as LibraryDirectory,
			3)

		val directory = project.directory.getRecursively("ProjectDirectory") as LibraryDirectory
		assertEquals(0, directory.indexOf(project.directory.getRecursively("Element2") as ContainerLibraryElement))
		assertEquals(1, directory.indexOf(project.directory.getRecursively("Element3") as ContainerLibraryElement))
		assertEquals(2, directory.indexOf(project.directory.getRecursively("Element") as ContainerLibraryElement))
	}

	@Test
	fun shouldMoveToAnotherDirectory() {
		val project = projectBuilder
			.addContainerLibraryElement("Element")
			.addDirectory("ProjectDirectory")
			.library

		service.move(
			project.directory.getRecursively("Element") as ContainerLibraryElement,
			project.directory.getRecursively("ProjectDirectory") as LibraryDirectory
		)

		val directory = project.directory.getRecursively("ProjectDirectory") as LibraryDirectory
		val element = project.directory.getRecursively("Element") as ContainerLibraryElement
		projectLibraryService.loadMetaGraph(project, element)
		assertEquals(0, directory.indexOf(element))
		assertNotNull(projectLibraryService.getMetaGraph(project, element))
	}

	@Test
	fun shouldNotMoveProjectDependenciesToLibrary() {
		assertFailsWith<LibraryDependencyException> {
			projectBuilder.addContainerLibraryElement("ReferencedVertice")
			val referencedVertice = projectBuilder.library.directory.get("ReferencedVertice") as ContainerLibraryElement
			val graphViewBuilder = GraphViewBuilder<Boolean>()
			graphViewBuilder.reference(referencedVertice.uuid)
			val referencingMetaGraph = MetaGraph(GraphStorable(graphViewBuilder.graphView), ContainerDrawing())
			referencingMetaGraph.graph.model!!.name = Name("ReferencingVertice")
			projectBuilder.addContainerLibraryElement(referencingMetaGraph)

			try {
				service.move(projectBuilder.library.directory.get("ReferencingVertice") as ContainerLibraryElement, libraryBuilder.library.directory as LibraryDirectory)
			} catch (e: LibraryDependencyException) {
				assertNotNull(projectBuilder.library.directory.get("ReferencingVertice") as ContainerLibraryElement?)
				assertNull(libraryBuilder.library.directory.get("ReferencingVertice") as ContainerLibraryElement?)
				throw e
			}
		}
	}
}