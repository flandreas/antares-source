package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.graph.GraphQuota
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.model.module.GraphModelModule
import ch.scorpion.jabbah.graph.model.port.TestPortFactory
import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.graph.view.port.TestPortViewFactory
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.name
import kotlin.test.*

class FileLibraryPersistenceServiceTest {

	companion object {
		init {
			GraphViewTestRule.configure()
		}
	}

	private val directory = Files.createTempDirectory(null)
	private val persistenceService = FileLibraryPersistenceService(directory.parent.absolutePathString(), directory.name)

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

		assertTrue(Files.exists(metaGraphPath(metaGraph)))
	}

	@Test
	fun shouldLoadMetaGraph() {
		val origMetaGraph = MetaGraph()
		persistenceService.storeMetaGraph(LibraryModule.libraryHolder.library, origMetaGraph)

		val loadedMetaGraph = persistenceService.loadMetaGraph(LibraryModule.libraryHolder.library, origMetaGraph.uuid)

		assertEquals(origMetaGraph.uuid, loadedMetaGraph.uuid)
	}

	@Test
	fun shouldDeleteMetaGraph() {
		val origMetaGraph = MetaGraph()
		persistenceService.storeMetaGraph(LibraryModule.libraryHolder.library, origMetaGraph)

		persistenceService.deleteMetaGraph(LibraryModule.libraryHolder.library, origMetaGraph.uuid)

		assertFalse(Files.exists(metaGraphPath(origMetaGraph)))
	}

	@Test
	fun shouldStoreLibrary() {
		persistenceService.storeLibrary(LibraryModule.libraryHolder.library)

		assertTrue(Files.exists(libraryFilePath()))
	}

	@Test
	fun shouldLoadLibrary() {
		persistenceService.storeLibrary(LibraryModule.libraryHolder.library)

		val loadedLibrary = persistenceService.loadLibrary(LibraryModule.libraryHolder.library.identification)

		assertEquals(LibraryModule.libraryHolder.library.uuid, loadedLibrary.uuid)
	}

	@Test
	fun shouldDeleteLibrary() {
		val origMetaGraph = MetaGraph()
		persistenceService.storeMetaGraph(LibraryModule.libraryHolder.library, origMetaGraph)
		persistenceService.storeLibrary(LibraryModule.libraryHolder.library)

		persistenceService.deleteLibrary(LibraryModule.libraryHolder.library.identification)

		assertFalse(Files.exists(libraryDirPath()))
		assertFalse(Files.exists(metaGraphPath(origMetaGraph)))
	}

	@Test
	fun shouldImportLibraryWithArbitraryFilename() {
		val zipFile = Files.createTempFile(null, ".zip")
		val metaGraph = MetaGraph()
		persistenceService.storeMetaGraph(LibraryModule.libraryHolder.library, metaGraph)
		persistenceService.storeLibrary(LibraryModule.libraryHolder.library)

		persistenceService.exportLibrary(LibraryModule.libraryHolder.library.identification, zipFile.toAbsolutePath().toString())
		persistenceService.deleteLibrary(LibraryModule.libraryHolder.library.identification)
		persistenceService.importLibrary(zipFile.toAbsolutePath().toString(), 1, GraphQuota.UNLIMITED)

		assertTrue(Files.exists(libraryFilePath()))
		assertTrue(Files.exists(metaGraphPath(metaGraph)))
	}

	private fun libraryDirPath(libraryUUID: UUID = LibraryModule.libraryHolder.library.uuid): Path =
		FileSystems.getDefault().getPath(directory.toAbsolutePath().toString(), libraryUUID.toString())

	private fun libraryFilePath(libraryUUID: UUID = LibraryModule.libraryHolder.library.uuid): Path =
		FileSystems.getDefault().getPath(directory.toAbsolutePath().toString(), libraryUUID.toString(), "library.xml")

	private fun metaGraphPath(metaGraph: MetaGraph, libraryUUID: UUID = LibraryModule.libraryHolder.library.uuid): Path =
		FileSystems.getDefault().getPath(directory.toAbsolutePath().toString(), libraryUUID.toString(), "${metaGraph.uuid}.cir")
}