package ch.scorpion.jabbah.base.dsl

import kotlin.test.*

class SemanticAnalyserTest {

	@Test
	fun shouldBuildSymbolTable() {
		val ast = Parser("""
			var a = 5
			b = a
		""".trimIndent()).parse()

		val analyser = SemanticAnalyser(null)
		analyser.analyse(ast)

		val symbolTable = analyser.scope

		assertEquals(2 + Lexer.RESERVED_KEYWORDS.size, symbolTable.size)
		assertIs<BuiltInTypeSymbol>(symbolTable.lookup("var"))
		assertIs<VariableSymbol>(symbolTable.lookup("a"))
		assertIs<VariableSymbol>(symbolTable.lookup("b"))
	}

	@Test
	fun shouldThrowNameErrorWithUndefinedVariable() {
		assertFailsWith(SemanticError::class) {
			val ast = Parser("""
			var a = 5
			b = c
		""".trimIndent()).parse()

			val analyser = SemanticAnalyser(null)
			analyser.analyse(ast)
		}
	}

	@Test
	fun shouldNotAllowStoreDeclarationInInnerScope() {
		assertFailsWith(SemanticError::class) {
			val ast = Parser("""
			var a = 1
			if (a) {
				store b = 0
			}
		""".trimIndent()).parse()

			val analyser = SemanticAnalyser(null)
			assertFailsWith(SemanticError::class) {
				analyser.analyse(ast)
			}
		}
	}
}