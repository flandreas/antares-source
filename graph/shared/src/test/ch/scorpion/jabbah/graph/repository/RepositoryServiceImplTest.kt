package ch.scorpion.jabbah.graph.repository

import ch.scorpion.jabbah.graph.GraphStorable
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.container.ContainerDrawing
import ch.scorpion.jabbah.graph.library.*
import ch.scorpion.jabbah.graph.project.Project
import ch.scorpion.jabbah.graph.project.ProjectImpl
import ch.scorpion.jabbah.graph.project.ProjectModule
import ch.scorpion.jabbah.graph.view.GraphViewBuilder
import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import io.mockk.mockk
import kotlin.test.*

class RepositoryServiceImplTest {

	companion object {
		init {
			GraphViewTestRule.configure()
		}
	}

	private val libraryPersistenceService = mockk<LibraryPersistenceService>(relaxed = true)
	private val libraryService: LibraryService = LibraryService(libraryAccessor = { libraryBuilder.library }, userLibraryPersister = libraryPersistenceService)
	private val libraryBuilder = LibraryBuilder(name = "Library", libraryService = libraryService)

	private val projectPersistenceService = mockk<LibraryPersistenceService>(relaxed = true)
	private val projectLibraryService: LibraryService = LibraryService(libraryAccessor = { projectBuilder.library }, userLibraryPersister = projectPersistenceService)
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

		service.move(project.getRecursively("Element") as ContainerLibraryElement, library.getRecursively("Directory") as LibraryDirectory)

		assertNull(project.getRecursively("Element"))
		assertNotNull(library.getRecursively("Element"))
		assertTrue((library.getRecursively("Directory") as LibraryDirectory).contains(library.getRecursively("Element") as LibraryItem))
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
			project.getRecursively("Element") as ContainerLibraryElement,
			project.getRecursively("ProjectDirectory") as LibraryDirectory,
			3)

		val directory = project.getRecursively("ProjectDirectory") as LibraryDirectory
		assertEquals(0, directory.indexOf(project.getRecursively("Element2") as ContainerLibraryElement))
		assertEquals(1, directory.indexOf(project.getRecursively("Element3") as ContainerLibraryElement))
		assertEquals(2, directory.indexOf(project.getRecursively("Element") as ContainerLibraryElement))
	}

	@Test
	fun shouldNotMoveProjectDependenciesToLibrary() {
		assertFailsWith<LibraryDependencyException> {
			projectBuilder.addContainerLibraryElement("ReferencedVertice")
			val referencedVertice = projectBuilder.library.get("ReferencedVertice") as ContainerLibraryElement
			val graphViewBuilder = GraphViewBuilder<Boolean>()
			graphViewBuilder.reference(referencedVertice.uuid)
			val referencingMetaGraph = MetaGraph(GraphStorable(graphViewBuilder.graphView), ContainerDrawing())
			referencingMetaGraph.graph.model!!.name.value = "ReferencingVertice"
			projectBuilder.addContainerLibraryElement(referencingMetaGraph)

			try {
				service.move(projectBuilder.library.get("ReferencingVertice") as ContainerLibraryElement, libraryBuilder.library as LibraryDirectory)
			} catch (e: LibraryDependencyException) {
				assertNotNull(projectBuilder.library.get("ReferencingVertice") as ContainerLibraryElement?)
				assertNull(libraryBuilder.library.get("ReferencingVertice") as ContainerLibraryElement?)
				throw e
			}
		}
	}
}