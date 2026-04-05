package io.antarescircuit.jabbah.base.dsl

import kotlin.math.ceil

open class DslGlobalFunctions : DslExternalFunctions {

	protected val _reservedFunctionNames = mutableListOf<String>()
	val reservedFunctionNames: List<String> get() = _reservedFunctionNames

	init {
		with(_reservedFunctionNames) {
			add("log2")
		}
	}

	override fun defineIn(symbolTable: SymbolTable) {
		with(symbolTable) {
			define(ExternalFunctionSymbol("log2", 1, ::log2Impl))
		}
	}

	private fun log2Impl(params: List<Any>, @Suppress("UNUSED_PARAMETER") context: Any? = null): Any =
		log2(longParam(0, params))

	/**
	 * Computes the binary logarithm (base 2) of a given value.
	 *
	 * The result is rounded up to the next bigger integer value in order to provide enough
	 * bits when used to calculate the number of necessary bits that represent a number
	 * whose number of bits is given by [value].
	 *
	 * Examples:
	 * - log2(8) = 3
	 * - log2(10) = 4
	 * - log2(16) = 4
	 */
	private fun log2(value: Long): Long =
		ceil(kotlin.math.log2(value.toDouble())).toLong()
}