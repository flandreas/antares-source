package ch.scorpion.antares.model.arithmetic

import ch.scorpion.antares.model.InputCount
import ch.scorpion.antares.model.Logic
import ch.scorpion.antares.model.Trigger
import ch.scorpion.antares.model.gate.AbstractDigitalGate
import ch.scorpion.antares.model.port.DigitalPort
import ch.scorpion.antares.model.port.DigitalPortImpl
import ch.scorpion.antares.model.signal.*
import ch.scorpion.jabbah.base.Math
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.GraphActorData
import ch.scorpion.jabbah.graph.model.InputPort
import ch.scorpion.jabbah.graph.model.OutputPort
import ch.scorpion.jabbah.graph.model.vertice.VerticeCalculator
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

/**
 * Produces a random value of a specifiable [BitWidth] when the trigger input value changes to 1.
 */
class Random() : AbstractDigitalGate(CALCULATOR, InputCount.ONE), DigitalSignalSource {

	companion object {
		private val CALCULATOR = object : VerticeCalculator<Random> {
			override fun calculate(vertice: Random, data: GraphActorData, signalHandler: SignalHandler) {
				if (data.getSignal<DigitalSignal>(1)!!.bitAt(0) == Bit.True) {
					vertice.getOutput<DigitalSignal>().setOutgoingSignalBuffered(
						Word.of(vertice.bitWidth, Math.randomInt(0, vertice.bitWidth.power() - 1).toLong()),
						signalHandler
					)
				}
			}
		}
	}

	/** [AbstractDigitalGate] */

	override val minInputCount: InputCount get() = InputCount.ONE

	override fun createInputPort(): InputPort<DigitalSignal> {
		return DigitalPortImpl.createInput(trigger = Trigger.EDGE, name = null, bitWidth = BitWidth.BW_1)
	}

	override fun createOutputPort(): OutputPort<DigitalSignal> {
		return DigitalPortImpl.createOutput(Logic.POSITIVE, null, BitWidth.BW_8)
	}

	/** [DigitalSignalSource] interface */

	override var bitWidth: BitWidth
		get() = (getOutput<DigitalSignal>() as DigitalPort).bitWidth
		set(value) {
			if (value != bitWidth) {
				(getOutput<DigitalSignal>() as DigitalPort).bitWidth = value
				stateChanged()
			}
		}

	override var signal: DigitalSignal?
		get() = getOutput<DigitalSignal>().getOutgoingSignal()
		set(value) { throw UnsupportedOperationException() }

	/** ---- [Storable] interface */

	override fun write(writer: StoreWriter) {
		super.write(writer)
		writer.writeInt("bitWidth", bitWidth.width)
	}

	override fun read(reader: StoreReader) {
		super.read(reader)
		bitWidth = BitWidth.of(reader.readInt("bitWidth"))
	}
}