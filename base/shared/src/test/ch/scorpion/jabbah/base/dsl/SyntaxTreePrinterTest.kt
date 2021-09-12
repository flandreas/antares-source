package ch.scorpion.jabbah.base.dsl

import kotlin.test.Test
import kotlin.test.assertEquals

class SyntaxTreePrinterTest {

	@Test
	fun shouldPrintSyntaxTree() {
		val ast = Parser("""
			a = 5 * (10 - 7)
			b = a
		""".trimIndent()).parse()

		val printer = SyntaxTreePrinter()
		ast.accept(printer)

		assertEquals("""
			Compound
			- =
			-- a
			-- *
			--- 5
			--- -
			---- 10
			---- 7
			- =
			-- b
			-- a
		""".trimIndent(), printer.result)
	}
}