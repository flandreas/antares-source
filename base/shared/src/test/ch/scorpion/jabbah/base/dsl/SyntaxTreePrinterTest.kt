package ch.scorpion.jabbah.base.dsl

import kotlin.test.Test
import kotlin.test.assertEquals

class SyntaxTreePrinterTest {

	@Test
	fun shouldPrintSyntaxTree() {
		val ast = DslParser("""
			var a = 5 * (10 - 7)
			var b = a
		""".trimIndent()).parse()

		val printer = SyntaxTreePrinter()
		ast.accept(printer)

		assertEquals("""
			Compound
			- var
			-- a
			-- *
			--- 5
			--- -
			---- 10
			---- 7
			- var
			-- b
			-- a
		""".trimIndent(), printer.result)
	}
}