package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import org.apache.commons.io.FilenameUtils
import java.nio.file.Files
import kotlin.io.path.absolutePathString
import kotlin.io.path.name
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

/**
 * Integration tests of [LibraryService] using a [FileLibraryPersistenceService].
 */
class FileLibraryServiceTest {

	companion object {
		init {
			GraphViewTestRule.configure()
		}
	}

	private val directory = Files.createTempDirectory(null)
	private val libraryPersistenceService = FileLibraryPersistenceService({ directory.parent.absolutePathString() }, directory.name)
	private val service: LibraryService = LibraryService(userLibraryPersisterProvider = { libraryPersistenceService })
	private val libraryBuilder = LibraryBuilder(name = "Library", libraryService = service)
	private val library: Library get() = libraryBuilder.library

	init {
		LibraryModule.libraryHolder.l = library
	}

	@Test
	fun shouldDuplicateContainerLibraryElement() {
		libraryBuilder.addContainerLibraryElement("Element")
		val orig = library.get("Element") as ContainerLibraryElement

		val duplicate = service.duplicateContainerLibraryElement(library, orig, TranslatableText("NewName"))
		service.loadMetaGraph(library, orig)
		service.loadMetaGraph(library, duplicate)

		assertNotEquals(orig.uuid, duplicate.uuid)
		assertEquals("Element", orig.metaGraph!!.name)
		assertEquals("NewName", duplicate.metaGraph!!.name)
	}

	@Test
	fun shouldExportImportMetaGraphBundle() {
		libraryBuilder.addContainerLibraryElement("Element")
		val orig = library.get("Element") as ContainerLibraryElement
		libraryBuilder.addDirectory("Directory")

		val tempDir = Files.createTempDirectory(null)
		val tempFile = Files.createTempFile(tempDir, "Test", "zip")
		service.exportMetaGraphBundle(orig, LibraryModule.libraryHolder, tempFile.absolutePathString())

		val importResult = service.importMetaGraphBundle(
			tempFile.absolutePathString(),
			FilenameUtils.getBaseName(tempFile.absolutePathString()),
			libraryBuilder.peek(),
			replaceIfUuidExists = true,
			LibraryModule.libraryHolder)

		assertEquals(MetaGraphBundleImportResult.Success, importResult)
	}
}