package io.antarescircuit.jabbah.base.io

import java.io.IOException
import java.nio.file.Files
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AtomicFileWriterTest {

	private val directory = Files.createTempDirectory(null)
	private val target = directory.resolve("target.xml")

	@Test
	fun shouldReplaceTargetWithoutLeavingTemporaryFiles() {
		target.writeText("old")

		AtomicFileWriter.write(target) { it.write("new".toByteArray()) }

		assertEquals("new", target.readText())
		assertEquals(listOf(target), directory.listDirectoryEntries())
	}

	@Test
	fun shouldKeepTargetWhenWritingFails() {
		target.writeText("old")

		assertFailsWith<IOException> {
			AtomicFileWriter.write(target) {
				it.write("partial".toByteArray())
				throw IOException("disk full")
			}
		}

		assertEquals("old", target.readText())
		assertEquals(listOf(target), directory.listDirectoryEntries())
	}
}
