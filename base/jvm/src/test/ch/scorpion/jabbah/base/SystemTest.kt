package ch.scorpion.jabbah.base

import kotlin.test.Test
import kotlin.test.assertEquals

class SystemTest {

	@Test
	fun shouldBuildToolTipTextWithTitle() {
		val tooltip = System.buildToolTipText("Title", "Text", "Subtext")
		assertEquals("Title: Text\n\nSubtext", tooltip)
	}

	@Test
	fun shouldBuildToolTipTextWithoutTitle() {
		val tooltip = System.buildToolTipText(null, "Text", "Subtext")
		assertEquals("Text\n\nSubtext", tooltip)
	}
}