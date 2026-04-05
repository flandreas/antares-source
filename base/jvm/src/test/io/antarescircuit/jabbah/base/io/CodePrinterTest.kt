package io.antarescircuit.jabbah.base.io

import kotlin.test.Test
import kotlin.test.assertEquals

class CodePrinterTest {

	private val printer = StringCodePrinter()

	@Test
	fun shouldPrint() {
		val s = printer
			.println("begin")
			.inc()
			.println("val a: Int")
			.println("val b: Float")
			.println()
			.println("-- Hello!")
			.dec()
			.print("end")
			.toString()

		assertEquals("""
			begin
			  val a: Int
			  val b: Float
			
			  -- Hello!
			end
		""".trimIndent(), s)
	}
}