package ch.scorpion.antares.dsl

import kotlin.test.*

class SymbolTableBuilderTest {

	@Test
	fun shouldBuildSymbolTable() {
		val ast = Parser("""
			var a = 5
			b = a
		""".trimIndent()).parse()

		val builder = SymbolTableBuilder()
		ast.accept(builder)

		val symbolTable = builder.build()

		assertEquals(3, symbolTable.size)
		assertIs<BuiltInTypeSymbol>(symbolTable.lookup("var"))
		assertIs<VariableSymbol>(symbolTable.lookup("a"))
		assertIs<VariableSymbol>(symbolTable.lookup("b"))
	}

	@Test
	fun shouldThrowNameErrorWithUndefinedVariable() {
		assertFailsWith(NameError::class) {
			val ast = Parser("""
			var a = 5
			b = c
		""".trimIndent()).parse()

			val builder = SymbolTableBuilder()
			ast.accept(builder)
		}
	}
}