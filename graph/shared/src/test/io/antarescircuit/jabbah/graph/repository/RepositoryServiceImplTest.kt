package io.antarescircuit.jabbah.graph.repository

import io.antarescircuit.jabbah.edit.model.text.description.Name
import io.antarescircuit.jabbah.graph.GraphStorable
import io.antarescircuit.jabbah.graph.MetaGraph
import io.antarescircuit.jabbah.graph.container.ContainerDrawing
import io.antarescircuit.jabbah.graph.library.*
import io.antarescircuit.jabbah.graph.project.ProjectImpl
import io.antarescircuit.jabbah.graph.project.ProjectModule
import io.antarescircuit.jabbah.graph.view.GraphViewBuilder
import io.antarescircuit.jabbah.graph.view.GraphViewTestRule
import kotlin.test.*

class RepositoryServiceImplTest {

	private val libraryBuilder: LibraryBuilder

	private val projectBuilder: LibraryBuilder

	private val service: RepositoryService get() = RepositoryModule.repositoryService


	init {
		GraphViewTestRule.configure()
		LibraryModule.userLibraryPersistenceService = MemoryLibraryPersistenceService()
		libraryBuilder = LibraryBuilder(name = "Library")
		LibraryModule.libraryHolder.l = libraryBuilder.library

		ProjectModule.projectLibraryPersistenceService = MemoryLibraryPersistenceService()
		projectBuilder = LibraryBuilder(name = "Project", library = ProjectImpl("Project", ""))
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
	fun shouldMoveToAnotherDirectory() {
		val project = projectBuilder
			.addContainerLibraryElement("Element")
			.addDirectory("ProjectDirectory")
			.library

		service.move(
			project.getRecursively("Element") as ContainerLibraryElement,
			project.getRecursively("ProjectDirectory") as LibraryDirectory
		)

		val directory = project.getRecursively("ProjectDirectory") as LibraryDirectory
		val element = project.getRecursively("Element") as ContainerLibraryElement
		ProjectModule.projectLibraryService.loadMetaGraph(project, element)
		assertEquals(0, directory.indexOf(element))
		assertNotNull(ProjectModule.projectLibraryService.getMetaGraph(project, element))
	}

	@Test
	fun shouldNotMoveProjectDependenciesToLibrary() {
		assertFailsWith<LibraryDependencyException> {
			projectBuilder.addContainerLibraryElement("ReferencedVertice")
			val referencedVertice = projectBuilder.library.get("ReferencedVertice") as ContainerLibraryElement
			val graphViewBuilder = GraphViewBuilder<Boolean>()
			graphViewBuilder.reference(referencedVertice.uuid)
			val referencingMetaGraph = MetaGraph(GraphStorable(graphViewBuilder.graphView), ContainerDrawing())
			referencingMetaGraph.graph.model!!.name = Name("ReferencingVertice")
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