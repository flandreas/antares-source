package io.antarescircuit.jabbah.base.io

import kotlin.test.Test
import kotlin.test.assertEquals

class SeparatorTest {

	private val out = StringCodePrinter()

	@Test
	fun shouldSeparate() {
		val separator = Separator(out, ";\n")

		for (i in 0..2) {
			separator.check()
			out.print("Line $i")
		}

		assertEquals("""
			Line 0;
			Line 1;
			Line 2
		""".trimIndent(), out.toString())
	}
}