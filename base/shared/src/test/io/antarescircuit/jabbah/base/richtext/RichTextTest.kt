package io.antarescircuit.jabbah.base.richtext

import kotlin.test.Test
import kotlin.test.assertEquals

class RichTextTest {

	@Test
	fun shouldStripToPlainText() {
		assertEquals("JK MS-Flip-Flop PRE CLR", RichText.stripToPlainText("JK MS-Flip-Flop PRE\\/CLR"))
		assertEquals("Overline", RichText.stripToPlainText("!(Overline)"))
		assertEquals("Export Import", RichText.stripToPlainText("Export\\/Import"))
		assertEquals("Export Import", RichText.stripToPlainText("Export/Import"))
	}
}