package io.antarescircuit.antares.model.gate

import io.antarescircuit.antares.model.signal.Bit
import io.antarescircuit.antares.model.signal.BitWidth
import io.antarescircuit.antares.model.signal.DigitalSignal
import io.antarescircuit.antares.model.signal.DigitalSignalFactory
import io.antarescircuit.jabbah.execution.SignalHandler
import io.antarescircuit.jabbah.graph.model.GraphActorData
import io.antarescircuit.jabbah.graph.model.MultiSignalSource
import io.antarescircuit.jabbah.graph.model.vertice.VerticeCalculator

abstract class AbstractLogicGateCalculator : VerticeCalculator<AbstractLogicGate> {

	fun calculateMultiBit(source: MultiSignalSource<DigitalSignal>, filter: (portId: Int) -> Boolean = { true }): DigitalSignal {
		val inputValues = (1..source.signalCount)
			.filter(filter)
			.map { effectiveGateInputValue(it, source) }

		calculateDefinedWord(inputValues)?.let { return it }

		val outputBits = mutableListOf<Bit>()
		for (bitIndex in 0 until inputValues.first().bitWidth.width) {
			outputBits.add(calculateBit(inputValues, bitIndex))
		}

		return DigitalSignalFactory.ofBits(outputBits)
	}

	fun calculateSingleBit(source: MultiSignalSource<DigitalSignal>, filter: (portId: Int) -> Boolean = { true }): DigitalSignal =
		DigitalSignalFactory.of(calculateBit(
			(1..source.signalCount)
				.filter(filter)
				.map { DigitalSignalFactory.of(effectiveGateInputBit(source.getSignal(it).bitAt(0))) },
			0))

	abstract fun calculateBit(input: Collection<DigitalSignal>, bitIndex: Int): Bit

	/**
	 * Calculates the output value bitwise from fully defined input values, or returns `null` if this
	 * gate has no word-level implementation. Must be equivalent to [calculateBit] for defined bits.
	 */
	open fun calculateWord(values: List<ULong>): ULong? = null

	/** The fast lane for multi-bit inputs without undefined or error bits: plain long arithmetic. */
	private fun calculateDefinedWord(inputs: List<DigitalSignal>): DigitalSignal? {
		val values = ArrayList<ULong>(inputs.size)
		for (input in inputs) {
			values.add(input.toLong() ?: return null)
		}
		return calculateWord(values)?.let { DigitalSignalFactory.of(inputs.first().bitWidth, it) }
	}

	/** The fast lane for BitWidth 1 inputs.*/
	abstract fun calculateBit(input: MultiSignalSource<DigitalSignal>): Bit

	override fun calculate(vertice: AbstractLogicGate, data: GraphActorData, signalHandler: SignalHandler) {
		if (vertice.bitWidth.width == BitWidth.BW_1.width) {
			vertice.getOutput<DigitalSignal>().setOutgoingSignalBuffered(DigitalSignalFactory.of(calculateBit(vertice)), signalHandler)
		} else {
			vertice.getOutput<DigitalSignal>().setOutgoingSignalBuffered(calculateMultiBit(vertice), signalHandler)
		}
	}
}

fun effectiveGateInputBit(bit: Bit): Bit =
	if (bit == Bit.Undefined) {
		CurrentUndefinedGateInputBehavior.value.definedBit
	} else {
		bit
	}

fun effectiveGateInputWord(input: DigitalSignal): DigitalSignal =
	if (!input.isPartiallyUndefined) {
		input
	} else {
		DigitalSignalFactory.ofBits(input.bits.map { bit -> effectiveGateInputBit(bit) })
	}

fun effectiveGateInputValue(portId: Int, source: MultiSignalSource<DigitalSignal>): DigitalSignal =
	effectiveGateInputWord(source.getSignal(portId))
