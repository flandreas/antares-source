package ch.scorpion.antares.model.gate

import ch.scorpion.antares.model.signal.Bit
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.DigitalSignalFactory
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.GraphActorData
import ch.scorpion.jabbah.graph.model.MultiSignalSource
import ch.scorpion.jabbah.graph.model.vertice.VerticeCalculator

abstract class AbstractLogicGateCalculator : VerticeCalculator<AbstractLogicGate> {

	fun calculateMultiBit(source: MultiSignalSource<DigitalSignal>, filter: (portId: Int) -> Boolean = { true }): DigitalSignal {
		val inputValues = (1..source.signalCount)
			.filter(filter)
			.map { effectiveGateInputValue(it, source) }

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
	DigitalSignalFactory.ofBits(input.bits.map { bit -> effectiveGateInputBit(bit) })

fun effectiveGateInputValue(portId: Int, source: MultiSignalSource<DigitalSignal>): DigitalSignal =
	effectiveGateInputWord(source.getSignal(portId))
