package ch.scorpion.jabbah.graph.library

import org.junit.ClassRule
import org.junit.Test
import java.nio.file.Files

class FileLibraryPersistenceServiceTest {

	companion object {
		@ClassRule
		@JvmField
		val rule = GraphLibraryTestRule()
	}

	private val service = FileLibraryPersistenceService(Files.createTempDirectory("libraries").toAbsolutePath().toString())

	@Test
	fun shouldDuplicateLibrary() {
		// TODO
	}
}