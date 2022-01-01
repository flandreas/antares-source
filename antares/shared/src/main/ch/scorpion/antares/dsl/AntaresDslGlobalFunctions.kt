package ch.scorpion.antares.dsl

import ch.scorpion.antares.model.signal.BitOperation
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.dsl.*

class AntaresDslGlobalFunctions : DslGlobalFunctions() {

	init {
		with(_reservedFunctionNames) {
			add("bits")
		}
	}

	override fun defineIn(symbolTable: SymbolTable) {
		super.defineIn(symbolTable)
		with(symbolTable) {
			define(ExternalFunctionSymbol("bits", 3, ::bitsImpl))
		}
	}

	private fun bitsImpl(params: List<Any>): Any {
		if (params.size < 3) {
			throw RuntimeError(CodeLocation.UNDEFINED, Translations.getString("base.dsl.notEnoughParameters.msg"))
		}
		return if (params[0] is DigitalSignal) {
			bits(
				digitalSignalParam(0, params),
				longParam(1, params),
				longParam(2, params)
			)
		} else {
			bits(
				longParam(0, params),
				longParam(1, params),
				longParam(2, params),
			)
		}
	}

	/**
	 * Extracts [size] bits at position [pos] from [signal], where bit positions start with 0.
	 * Example: bits(31, 3, 2) = 3.
	 */
	private fun bits(signal: DigitalSignal, pos: Long, size: Long): Long =
		signal.bitsAt(pos.toInt(), size.toInt())?.toLong() ?: throw RuntimeError(CodeLocation.UNDEFINED, "Error bits in function 'bits'")

	/**
	 * Extracts [size] bits at position [pos] from [signal], where bit positions start with 0.
	 * Example: bits(31, 3, 2) = 3.
	 */
	private fun bits(signal: Long, pos: Long, size: Long): Long =
		BitOperation.bits(signal.toULong(), pos.toInt(), size.toInt()).toLong()
}

fun digitalSignalParam(index: Int, params: List<Any>): DigitalSignal {
	if (index >= params.size) {
		throw RuntimeError(CodeLocation.UNDEFINED, Translations.getString("base.dsl.notEnoughParameters.msg"))
	}
	val param = params[index]
	if (param !is DigitalSignal) {
		throw RuntimeError(CodeLocation.UNDEFINED, Translations.getString("antares.dsl.expectedDigitalSignalParameter.msg", index + 1))
	}
	return param
}