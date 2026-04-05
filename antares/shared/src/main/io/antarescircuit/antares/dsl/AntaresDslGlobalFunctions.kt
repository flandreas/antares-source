package io.antarescircuit.antares.dsl

import io.antarescircuit.antares.model.gate.effectiveGateInputWord
import io.antarescircuit.antares.model.signal.BitOperation
import io.antarescircuit.antares.model.signal.DigitalSignal
import io.antarescircuit.antares.model.signal.DigitalSignalFactory
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.dsl.*
import io.antarescircuit.jabbah.base.parser.TextLocation
import io.antarescircuit.jabbah.execution.SignalHandler
import io.antarescircuit.jabbah.execution.actor.Actor
import io.antarescircuit.jabbah.graph.model.StoringGraphActorData
import io.antarescircuit.jabbah.graph.model.vertice.SubGraphFunctionContext

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
			define(ExternalFunctionSymbol("gated", 1, ::gatedImpl))
			define(ExternalFunctionSymbol("triggerAfter", 1, ::triggerAfterImpl))
		}
	}

	private fun bitsImpl(params: List<Any>, @Suppress("UNUSED_PARAMETER") context: Any? = null): Any {
		if (params.size < 3) {
			throw RuntimeError(TextLocation.UNDEFINED, Translations.getString("base.dsl.notEnoughParameters.msg"))
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
	 *
	 * Example: bits(31, 3, 2) = 3.
	 */
	private fun bits(signal: DigitalSignal, pos: Long, size: Long): Long =
		signal.bitsAt(pos.toInt(), size.toInt())?.toLong() ?: throw RuntimeError(TextLocation.UNDEFINED, "Error bits in function 'bits'")

	/**
	 * Extracts [size] bits at position [pos] from [signal], where bit positions start with 0.
	 *
	 * Example: bits(31, 3, 2) = 3.
	 */
	private fun bits(signal: Long, pos: Long, size: Long): Long =
		BitOperation.bits(signal.toULong(), pos.toInt(), size.toInt()).toLong()

	private fun gatedImpl(params: List<Any>, @Suppress("UNUSED_PARAMETER") context: Any? = null): Any {
		if (params.isEmpty()) {
			throw RuntimeError(TextLocation.UNDEFINED, Translations.getString("base.dsl.notEnoughParameters.msg"))
		}
		return gated(digitalSignalParam(0, params))
	}

	/**
	 * Treats [signal] like an input signal to a logic gate, thereby possibly converting
	 * undefined (floating) bits according to the current system preference
	 * "Undefined Gate Input Behaviour".
	 *
	 * Example: gated(I) returns 0 if I is 0x?8 and "Undefined Gate Input Behaviour"
	 * is "Read as O"
	 */
	private fun gated(signal: DigitalSignal): DigitalSignal = effectiveGateInputWord(signal)

	private fun triggerAfterImpl(params: List<Any>, context: Any?): Any {
		if (params.isEmpty()) {
			throw RuntimeError(TextLocation.UNDEFINED, Translations.getString("base.dsl.notEnoughParameters.msg"))
		}
		if (context !is SubGraphFunctionContext) {
			throw RuntimeError(TextLocation.UNDEFINED, "Program error: Unsupported context")
		}
		if (context.actor == null || context.signalHandler == null) {
			throw RuntimeError(TextLocation.UNDEFINED, "Program error: Incomplete context")
		}
		return triggerAfter(longParam(0, params), context.actor!!, context.signalHandler!!)
	}

	/**
	 * Recalculates a component after [delay] ns have passed in the simulator.
	 *
	 * Example: triggerAfter(200)
	 */
	private fun triggerAfter(delay: Long, actor: Actor, signalHandler: SignalHandler) {
		signalHandler.requestActingAfter(actor, delay, StoringGraphActorData(null, null))
	}
}

fun digitalSignalParam(index: Int, params: List<Any>): DigitalSignal {
	if (index >= params.size) {
		throw RuntimeError(TextLocation.UNDEFINED, Translations.getString("base.dsl.notEnoughParameters.msg"))
	}
	return when (val param = params[index]) {
		is DigitalSignal -> param
		is Long -> DigitalSignalFactory.ofMinimalBitWidth(param.toULong())
		else -> throw RuntimeError(TextLocation.UNDEFINED, Translations.getString("antares.dsl.expectedDigitalSignalParameter.msg", index + 1))
	}
}