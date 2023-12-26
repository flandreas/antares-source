package ch.scorpion.jabbah.graph.dsl

import ch.scorpion.jabbah.base.dsl.DslLexer
import kotlin.test.Test

class GraphDslParserTest {

	@Test
	fun shouldParseInitStatement() {
		val parser = GraphDslParser(DslLexer("""
			init {
				a = 0
			}
		""".trimIndent()), null)

		parser.parse()

		// SyntaxTreePrinter from base module not yet available in other modules
	}

	@Test
	fun shouldParsePropertyWithPortName() {
		val parser = GraphDslParser(DslLexer("""
			#1:OUT
		""".trimIndent()), null)

		parser.parse()

		// SyntaxTreePrinter from base module not yet available in other modules
	}

	@Test
	fun shouldParsePropertyWithPortId() {
		val parser = GraphDslParser(DslLexer("""
			#1:1
		""".trimIndent()), null)

		parser.parse()

		// SyntaxTreePrinter from base module not yet available in other modules
	}
}