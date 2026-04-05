package io.antarescircuit.jabbah.graph.model.graph

import kotlin.test.Test
import kotlin.test.assertEquals

class GraphNameAbbreviatorTest {

	@Test
	fun shouldNotAbbreviateShortNames() {
		assertEquals("", GraphNameAbbreviator.abbreviate(""))
		assertEquals("A", GraphNameAbbreviator.abbreviate("A"))
		assertEquals("Bla", GraphNameAbbreviator.abbreviate("Bla"))
	}

	@Test
	fun shouldTrim() {
		assertEquals("Bla", GraphNameAbbreviator.abbreviate(" Bla "))
	}

	@Test
	fun shouldUseFirstLetterOfWordsAsUppercase() {
		assertEquals("HA", GraphNameAbbreviator.abbreviate("Half Adder"))
		assertEquals("MFC", GraphNameAbbreviator.abbreviate("My first circuit"))
		assertEquals("ACN", GraphNameAbbreviator.abbreviate("A circuit name with many words"))
	}

	@Test
	fun shouldUseFirstCharactersAsUppercase() {
		@Suppress("SpellCheckingInspection")
		assertEquals("THI", GraphNameAbbreviator.abbreviate("Thisisaverylongword"))
	}

	@Test
	fun shouldAbbreviateRichText() {
		assertEquals("RT", GraphNameAbbreviator.abbreviate("!(Rich Text)_(12)"))
	}
}