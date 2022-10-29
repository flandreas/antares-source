package ch.scorpion.jabbah.base.dsl

import ch.scorpion.jabbah.base.module.BaseModule

/**
 * Provides convenience facade methods for executing DSL scripts without
 * dealing with [Parser] and [Interpreter] classes.
 */
object Dsl {

	fun execute(script: String, symbolTable: SymbolTable, memory: Memory): Any {
		val parser = BaseModule.parserFactory.create(
			script,
			BaseModule.semanticAnalyserFactory.create(symbolTable))
		val interpreter = BaseModule.interpreterFactory.invoke(parser.parse(), memory)
		return interpreter.interpret()
	}
}