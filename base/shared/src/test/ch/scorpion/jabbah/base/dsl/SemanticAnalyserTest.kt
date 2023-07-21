package ch.scorpion.jabbah.base.dsl

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.module.BaseModule
import kotlin.test.*

class SemanticAnalyserTest {

	@BeforeTest
	fun setup() {
		Translations.withAnyKey()
	}

	@Test
	fun shouldBuildSymbolTable() {
		val ast = DslParser("""
			var a = 5
			var b = a
		""".trimIndent()).parse()

		val analyser = SemanticAnalyser(null)
		analyser.analyse(ast)

		val symbolTable = analyser.scope

		assertEquals(2 + DslLexer.RESERVED_KEYWORDS.size + BaseModule.dslGlobalFunctions.reservedFunctionNames.size, symbolTable.symbolsCount)
		assertIs<BuiltInTypeSymbol>(symbolTable.lookup("var"))
		assertIs<VariableSymbol>(symbolTable.lookup("a"))
		assertIs<VariableSymbol>(symbolTable.lookup("b"))
	}

	@Test
	fun shouldThrowNameErrorWithUndefinedVariable() {
		assertFailsWith(SemanticError::class) {
			val ast = DslParser("""
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
			val ast = DslParser("""
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

	@Test
	fun shouldNotAllowUndefinedFunctionName() {
		assertFailsWith(SemanticError::class) {
			val symbolTable = ScopedSymbolTable("global", 1, null)
			symbolTable.define(ExternalFunctionSymbol("f", 1) {})
			val semanticAnalyser = SemanticAnalyser(symbolTable)

			DslParser(DslLexer("g(1)"), semanticAnalyser).parse()
		}
	}

	@Test
	fun shouldAllowDefinedFunctionName() {
		val symbolTable = ScopedSymbolTable("global", 1, null)
		symbolTable.define(ExternalFunctionSymbol("f", 1) {})
		val semanticAnalyser = SemanticAnalyser(symbolTable)

		DslParser(DslLexer("f(1)"), semanticAnalyser).parse()
	}

	@Test
	fun shouldRejectTooManyFunctionParams() {
		assertFailsWith(SemanticError::class) {
			val symbolTable = ScopedSymbolTable("global", 1, null)
			symbolTable.define(ExternalFunctionSymbol("f", 1) {})
			val semanticAnalyser = SemanticAnalyser(symbolTable)

			DslParser(DslLexer("f(1, 2)"), semanticAnalyser).parse()
		}
	}

	@Test
	fun shouldResolveMultipleFunctionCalls() {
		val symbolTable = ScopedSymbolTable("global", 1, null)
		symbolTable.define(ExternalFunctionSymbol("f", 1) {})
		val semanticAnalyser = SemanticAnalyser(symbolTable)

		val ast = DslParser(DslLexer("""
			if (1 == 1) {
				f(1)
			} else {
				f(2)
			}
		""".trimIndent()), semanticAnalyser).parse()

		assertEquals(2, filterNodes(ast) { it is FunctionCall}.size)
	}
}