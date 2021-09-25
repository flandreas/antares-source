package ch.scorpion.jabbah.graph.dsl

import ch.scorpion.jabbah.base.dsl.CodeLocation
import ch.scorpion.jabbah.base.dsl.RuntimeError
import ch.scorpion.jabbah.base.dsl.ScopedSymbolTable

/**
 * Registers external definitions in a [ScopedSymbolTable] of functions to be called
 * by DSL scripts.
 */
interface DslExternalFunctions {
	fun defineIn(symbolTable: ScopedSymbolTable)
}

/**
 * Abstract base implementation of [DslExternalFunctions] that contains some reusable
 * methods for extracting typed parameters from general parameter [List]s.
 */
abstract class AbstractExternalFunctions : DslExternalFunctions {

	fun anyParam(index: Int, params: List<Any>): Any {
		if (index >= params.size) {
			throw RuntimeError(CodeLocation.UNDEFINED, "Not enough parameters")
		}
		return params[index]
	}

	fun longParam(index: Int, params: List<Any>): Long {
		if (index >= params.size) {
			throw RuntimeError(CodeLocation.UNDEFINED, "Not enough parameters")
		}
		val param = params[index]
		if (param !is Long) {
			throw RuntimeError(CodeLocation.UNDEFINED, "Expected number in parameter ${index + 1}")
		}
		return param
	}

	fun stringParam(index: Int, params: List<Any>): String {
		if (index >= params.size) {
			throw RuntimeError(CodeLocation.UNDEFINED, "Not enough parameters")
		}
		val param = params[index]
		if (param !is String) {
			throw RuntimeError(CodeLocation.UNDEFINED, "Expected string in parameter ${index + 1}")
		}
		return param
	}
}