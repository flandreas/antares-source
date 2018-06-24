package ch.scorpion.jabbah.graph.repository

import ch.scorpion.jabbah.base.TestTranslationsBuilder
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.graph.GraphStorable
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.container.ContainerDrawing
import ch.scorpion.jabbah.graph.library.*
import ch.scorpion.jabbah.graph.project.ProjectModule
import ch.scorpion.jabbah.graph.view.GraphViewBuilder
import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import com.nhaarman.mockito_kotlin.mock
import org.hamcrest.CoreMatchers.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.ClassRule
import org.junit.Test

class RepositoryServiceImplTest {

	companion object {
		@ClassRule
		@JvmField
		val rule = GraphViewTestRule()
	}

	private val libraryPersistenceService = mock<LibraryPersistenceService>()
	private val libraryService: LibraryService = LibraryServiceImpl(libraryAccessor = { libraryBuilder.library }, persistenceService = libraryPersistenceService)
	private val libraryBuilder = TestLibraryBuilder(name = "Library", libraryService = libraryService)

	private val projectPersistenceService = mock<LibraryPersistenceService>()
	private val projectLibraryService: LibraryService = LibraryServiceImpl(libraryAccessor = { projectBuilder.library }, persistenceService = projectPersistenceService)
	private val projectBuilder = TestLibraryBuilder(name = "Project", libraryService = projectLibraryService)

	private val service = RepositoryServiceImpl(libraryService = libraryService, projectLibraryService = projectLibraryService)

	@Before
	fun setup() {
		TestTranslationsBuilder().withAnyKey()
		LibraryModule.libraryHolder.l = libraryBuilder.library
		ProjectModule.projectHolder.p = projectBuilder.library
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

		assertThat(project.getRecursively("Element"), `is`(nullValue()))
		assertThat(library.getRecursively("Element"), `is`(notNullValue()))
		assertThat((library.getRecursively("Directory") as LibraryDirectory).contains(library.getRecursively("Element") as LibraryItem), `is`(true))
	}

	@Test(expected = LibraryDependencyException::class)
	fun shouldNotMoveProjectDependenciesToLibrary() {
		projectBuilder.addContainerLibraryElement("ReferencedVertice")
		val referencedVertice = projectBuilder.library.get("ReferencedVertice") as ContainerLibraryElement
		val graphViewBuilder = GraphViewBuilder<Boolean>()
		graphViewBuilder.reference(referencedVertice.uuid)
		val referencingMetaGraph = MetaGraph(GraphStorable(graphViewBuilder.graphView), ContainerDrawing())
		referencingMetaGraph.graph.model!!.name = "ReferencingVertice"
		projectBuilder.addContainerLibraryElement(referencingMetaGraph)

		try {
			service.move(projectBuilder.library.get("ReferencingVertice") as ContainerLibraryElement, libraryBuilder.library as LibraryDirectory)
		} catch (e: LibraryDependencyException) {
			assertThat(projectBuilder.library.get("ReferencingVertice") as ContainerLibraryElement?, `is`(notNullValue()))
			assertThat(libraryBuilder.library.get("ReferencingVertice") as ContainerLibraryElement?, `is`(nullValue()))
			throw e
		}
	}

	@Test
	fun shouldUndoMove() {
		val project = projectBuilder
			.addDirectory("ProjectDirectory")
			.addContainerLibraryElement("Element")
			.library
		val library = libraryBuilder
			.addDirectory("Directory")
			.library
		service.move(project.getRecursively("Element") as ContainerLibraryElement, library.getRecursively("Directory") as LibraryDirectory)

		EditModule.commandManager.undo()

		assertThat(project.getRecursively("Element"), `is`(notNullValue()))
		assertThat((project.getRecursively("ProjectDirectory") as LibraryDirectory).contains(project.getRecursively("Element") as LibraryItem), `is`(true))
		assertThat(library.getRecursively("Element"), `is`(nullValue()))
	}

	@Test
	fun shouldRedoMove() {
		val project = projectBuilder
			.addDirectory("ProjectDirectory")
			.addContainerLibraryElement("Element")
			.library
		val library = libraryBuilder
			.addDirectory("Directory")
			.library
		service.move(project.getRecursively("Element") as ContainerLibraryElement, library.getRecursively("Directory") as LibraryDirectory)

		EditModule.commandManager.undo()
		EditModule.commandManager.redo()

		assertThat(project.getRecursively("Element"), `is`(nullValue()))
		assertThat(library.getRecursively("Element"), `is`(notNullValue()))
		assertThat((library.getRecursively("Directory") as LibraryDirectory).contains(library.getRecursively("Element") as LibraryItem), `is`(true))
	}
}