package io.antarescircuit.jabbah.base.io

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.nio.file.Files
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse

class ZipUtilTest {

	/** Contains [destDir] so that entries escaping [destDir] by `..` can be detected. */
	private val parentDir = Files.createTempDirectory(null)
	private val destDir = Files.createDirectory(parentDir.resolve("dest"))

	@Test
	fun shouldUnzipNestedEntries() {
		unzip(zipOf("lib/" to null, "lib/library.xml" to "content"))

		assertEquals("content", destDir.resolve("lib/library.xml").readText())
	}

	@Test
	fun shouldRejectFileEntryOutsideOfDestination() {
		assertFailsWith<IOException> { unzip(zipOf("../evil" to "content")) }

		assertFalse(parentDir.resolve("evil").exists())
	}

	@Test
	fun shouldRejectDirectoryEntryOutsideOfDestination() {
		assertFailsWith<IOException> { unzip(zipOf("../evil/" to null)) }

		assertFalse(parentDir.resolve("evil").exists())
	}

	private fun unzip(zip: ByteArray) {
		ZipInputStream(ByteArrayInputStream(zip)).use { ZipUtil.unzipFile(destDir, it) }
	}

	/** Creates a ZIP file with entries mapping names to their contents, or `null` for directories. */
	private fun zipOf(vararg entries: Pair<String, String?>): ByteArray {
		val output = ByteArrayOutputStream()
		ZipOutputStream(output).use { zip ->
			for ((name, content) in entries) {
				zip.putNextEntry(ZipEntry(name))
				content?.let { zip.write(it.toByteArray()) }
				zip.closeEntry()
			}
		}
		return output.toByteArray()
	}
}
