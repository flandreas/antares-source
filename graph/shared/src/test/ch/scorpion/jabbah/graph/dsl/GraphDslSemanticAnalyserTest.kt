package ch.scorpion.jabbah.graph.dsl

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.dsl.*
import ch.scorpion.jabbah.graph.model.GraphModelTestRule
import ch.scorpion.jabbah.graph.model.graph.GraphImpl
import ch.scorpion.jabbah.graph.model.graph.GraphSymbolTable
import ch.scorpion.jabbah.graph.model.vertice.GraphInputImpl
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFailsWith

class GraphDslSemanticAnalyserTest {

	@BeforeTest
	fun setup() {
		GraphModelTestRule.configure()
		Translations.withAnyKey()
	}

	@Test
	fun shouldAllowInitBlockInGlobalScope() {
		val ast = GraphDslParser(DslLexer("""
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
		val ast = GraphDslParser(DslLexer("""
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
		val ast = GraphDslParser(DslLexer("""
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

	@Test
	fun shouldAllowStoreDeclarationInInitBlock() {
		val ast = GraphDslParser(DslLexer("""
			init {
				store a
			}
		""".trimIndent()), null).parse()

		val analyser = GraphDslSemanticAnalyser(null)
		analyser.analyse(ast)
	}

	@Test
	fun shouldUseStoreDeclarationFromInitBlock() {
		val ast = GraphDslParser(DslLexer("""
			init {
				store a = 0
			}
			var b = a
		""".trimIndent()), null).parse()

		val analyser = GraphDslSemanticAnalyser(null)
		analyser.analyse(ast)
	}

	@Test
	fun shouldNotAllowWritingInput() {
		val ast = GraphDslParser(DslLexer("""
			I = 1
		""".trimIndent()), null).parse()

		val graph = GraphImpl()
		graph.add(GraphInputImpl<Int>(name = "I"))
		val symbolTable = GraphSymbolTable(graph)
		val analyser = GraphDslSemanticAnalyser(symbolTable)

		assertFailsWith(SemanticError::class) {
			analyser.analyse(ast)
		}
	}
}