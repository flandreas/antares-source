package io.antarescircuit.antares.filebased

import io.antarescircuit.jabbah.io.ElectricXmlReader
import io.antarescircuit.jabbah.io.ElectricXmlWriter
import io.antarescircuit.jabbah.io.Storable
import io.antarescircuit.jabbah.io.StoreXmlReader
import io.antarescircuit.jabbah.io.StoreXmlWriter
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Checks that storing the sample circuit and library files is stable across save/load round trips.
 */
class StoreRoundTripTest : AbstractFileBasedTest() {

	@Test
	fun shouldWriteIdenticalXmlAfterRoundTrip() {
		val files = File("samples/projects").walk()
			.filter { it.isFile && (it.extension == "cir" || it.name == "library.xml") }
			.sorted()
			.toList()
		assertTrue(files.isNotEmpty())

		for (file in files) {
			val saved = file.inputStream().use { roundTrip(it) }
			val savedAgain = roundTrip(saved.byteInputStream())

			assertEquals(saved, savedAgain, "round trip of ${file.path}")
		}
	}

	/** Loads a [Storable] from [input] and returns its stored XML. */
	private fun roundTrip(input: InputStream): String {
		val storable: Storable = StoreXmlReader(ElectricXmlReader(input)).readStorable()
		val output = ByteArrayOutputStream()
		StoreXmlWriter(ElectricXmlWriter(output)).writeStorable(storable)
		return output.toString(Charsets.UTF_8)
	}
}
