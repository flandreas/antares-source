package io.antarescircuit.jabbah.base.swing

import org.junit.Assert.*
import org.junit.Test
import java.io.File

class FileExtensionFilterTest {

	@Test
	fun shouldAcceptExtension() {
		val filter = FileExtensionFilter("cir", "test")
		assertTrue(filter.accept(File("test.cir")))
		assertFalse(filter.accept(File("test.bla")))
	}

	@Test
	fun shouldNotAcceptWrongExtension() {
		val filter = FileExtensionFilter("cir", "test")
		assertFalse(filter.accept(File("test.bla")))
	}
}