package ch.scorpion.jabbah.base.dsl

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.parser.TextLocation

/**
 * Registers external definitions in a [ScopedSymbolTable] of functions to be called
 * by DSL scripts.
 */
interface DslExternalFunctions {
	fun defineIn(symbolTable: SymbolTable)
}

fun anyParam(index: Int, params: List<Any>): Any {
	if (index >= params.size) {
		throw RuntimeError(TextLocation.UNDEFINED, Translations.getString("base.dsl.notEnoughParameters.msg"))
	}
	return params[index]
}

fun longParam(index: Int, params: List<Any>): Long {
	if (index >= params.size) {
		throw RuntimeError(TextLocation.UNDEFINED, Translations.getString("base.dsl.notEnoughParameters.msg"))
	}
	val param = params[index]
	if (param !is Long) {
		throw RuntimeError(TextLocation.UNDEFINED, Translations.getString("base.dsl.expectedNumberParameter.msg", index + 1))
	}
	return param
}

fun stringParam(index: Int, params: List<Any>): String {
	if (index >= params.size) {
		throw RuntimeError(TextLocation.UNDEFINED, Translations.getString("base.dsl.notEnoughParameters.msg"))
	}
	val param = params[index]
	if (param !is String) {
		throw RuntimeError(TextLocation.UNDEFINED, Translations.getString("base.dsl.expectedStringParameter.msg", index + 1))
	}
	return param
}