package ch.scorpion.antares.dsl

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.dsl.Node
import ch.scorpion.jabbah.base.dsl.SyntaxTreePrinter
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class AntaresParserTest {

    @BeforeTest
    fun setup() {
        Translations.withAnyKey()
    }

    @Test
    fun shouldParseLengthCast() {
        val parser = AntaresParser(AntaresLexer("A$16"), semanticAnalyser = null)
        assertAST(
            parser.parse(), """
			Compound
			- A$
			-- 16
		""".trimIndent()
        )
    }

    // Classes with asserts cannot yet be shared in test-util modules
    private fun assertAST(node: Node, ast: String) {
        val printer = SyntaxTreePrinter()
        node.accept(printer)

        assertEquals(ast, printer.result)
    }
}