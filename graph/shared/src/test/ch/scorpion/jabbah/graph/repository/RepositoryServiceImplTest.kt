package ch.scorpion.jabbah.graph.repository

import ch.scorpion.jabbah.base.TestTranslationsBuilder
import ch.scorpion.jabbah.graph.library.*
import ch.scorpion.jabbah.graph.model.GraphModelTestRule
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
}