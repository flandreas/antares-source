package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.edit.auth.EditAuthModule
import ch.scorpion.jabbah.edit.auth.User
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.model.module.GraphModelModule
import ch.scorpion.jabbah.graph.model.port.TestPortFactory
import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.graph.view.port.TestPortViewFactory
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.name
import kotlin.test.*


class FileLibraryWithUserPersistenceServiceTest {

	companion object {
		init {
			GraphViewTestRule.configure()
		}
	}

	private val directory = Files.createTempDirectory(null)
	private val persistenceService = FileLibraryPersistenceService(
		dataPath = directory.parent.toAbsolutePath().toString(),
		directoryName = directory.name,
		useOwner = true)

	@BeforeTest
	fun setup() {
		LibraryModule.userLibraryPersistenceService = persistenceService
		LibraryModule.libraryService = LibraryService()
		LibraryModule.libraryHolder.l = LibraryImpl(TranslatableText("test"), libraryService = LibraryModule.libraryService)
		GraphModelModule.portFactory = TestPortFactory()
		GraphViewModule.portViewFactory = TestPortViewFactory()
	}

	@Test
	fun shouldStoreMetaGraph() {
		val metaGraph = MetaGraph()

		persistenceService.storeMetaGraph(LibraryModule.libraryHolder.library, metaGraph)

		assertTrue(Files.exists(metaGraphPath(EditAuthModule.userHolder.user, metaGraph)))
	}

	private fun metaGraphPath(
		user: User,
		metaGraph: MetaGraph,
		libraryUUID: UUID = LibraryModule.libraryHolder.library.uuid
	): Path =
		FileSystems.getDefault().getPath(
			directory.parent.toAbsolutePath().toString(),
			user.identity.toString(),
			directory.name,
			libraryUUID.toString(),
			"${metaGraph.uuid}.cir"
		)
}