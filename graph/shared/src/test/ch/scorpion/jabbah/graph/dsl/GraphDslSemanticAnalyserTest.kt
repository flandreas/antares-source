package ch.scorpion.jabbah.graph.dsl

import ch.scorpion.jabbah.base.dsl.Lexer
import ch.scorpion.jabbah.base.dsl.SemanticError
import ch.scorpion.jabbah.base.dsl.Symbol
import kotlin.test.Test
import kotlin.test.assertFailsWith

class GraphDslSemanticAnalyserTest {


	@Test
	fun shouldAllowInitBlockInGlobalScope() {
		val ast = GraphDslParser(Lexer("""
			init {
				B = 0
			}
		""".trimIndent()), null).parse()

		val analyser = GraphDslSemanticAnalyser(null)
		analyser.scope.define(Symbol("B"))

		analyser.analyse(ast)
	}

	@Test
	fun shouldNotAllowInitBlockInInnerScope() {
		val ast = GraphDslParser(Lexer("""
			if (A) {
				init {
					B = 0
				}
			}
		""".trimIndent()), null).parse()

		val analyser = GraphDslSemanticAnalyser(null)
		analyser.scope.define(Symbol("A"))

		assertFailsWith(SemanticError::class) {
			analyser.analyse(ast)
		}
	}

	@Test
	fun shouldAllowAtMostOneInitBlock() {
		val ast = GraphDslParser(Lexer("""
			init {
				B = 0
			}
			init {
				B = 0
			}
		""".trimIndent()), null).parse()

		val analyser = GraphDslSemanticAnalyser(null)
		analyser.scope.define(Symbol("B"))

		assertFailsWith(SemanticError::class) {
			analyser.analyse(ast)
		}
	}
}