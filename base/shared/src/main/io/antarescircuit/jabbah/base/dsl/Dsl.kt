package io.antarescircuit.jabbah.base.dsl

import io.antarescircuit.jabbah.base.module.BaseModule

/**
 * Provides convenience facade methods for executing DSL scripts without
 * dealing with [DslParser] and [Interpreter] classes.
 */
object Dsl {

	fun execute(script: String, symbolTable: SymbolTable, memory: Memory): Any {
		val parser = BaseModule.parserFactory(
			script,
			BaseModule.semanticAnalyserFactory(symbolTable))
		val interpreter = BaseModule.interpreterFactory(parser.parse(), memory)
		return interpreter.interpret()
	}
}