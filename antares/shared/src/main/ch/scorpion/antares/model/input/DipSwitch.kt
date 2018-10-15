package ch.scorpion.antares.model.input

import ch.scorpion.antares.model.Logic
import ch.scorpion.antares.model.port.DigitalPort
import ch.scorpion.antares.model.port.DigitalPortImpl
import ch.scorpion.antares.model.signal.Bit
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.Word
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.execution.actor.Actor
import ch.scorpion.jabbah.graph.model.GraphActorData
import ch.scorpion.jabbah.graph.model.GraphActorDataImpl
import ch.scorpion.jabbah.graph.model.vertice.CalculatingVertice
import ch.scorpion.jabbah.graph.model.vertice.VerticeCalculator
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreWriter

/**
 * Represents a DIP (Dual in-line package) switch with a configurable [BitWidth].
 * The switch settings can only be changed during execution, and they are NOT made
 * persistent.
 * @param bitWidth the initial [BitWidth] of this [DipSwitch]
 */
class DipSwitch(
	bitWidth: BitWidth = BitWidth.BW_4
) : CalculatingVertice("library.element.DipSwitch", CALCULATOR) {

	companion object {
		val CALCULATOR = object : VerticeCalculator<DipSwitch> {
			override fun calculate(vertice: DipSwitch, data: GraphActorData, signalHandler: SignalHandler) {
				val output = vertice.getOutput<DigitalSignal>()
				output.setOutgoingSignalBuffered(data.getSignal(1), signalHandler)
				vertice.stateChanged()
			}
		}
	}

	init {
		addPort(DigitalPortImpl.createOutput(Logic.POSITIVE, null, bitWidth))
		propagationDelay = 1000
	}

	var value: Word = Word.allOf(bitWidth, Bit.False)
		private set

	var bitWidth: BitWidth
		get() = getDigitalPort().bitWidth
		set(value) {
			if (value != bitWidth) {
				getDigitalPort().bitWidth = value
				this.value = Word.allOf(bitWidth, Bit.False)
				stateChanged()
			}
		}

	/** ---- [Actor] interface */

	override fun executionStarted(signalHandler: SignalHandler) {
		super.executionStarted(signalHandler)
		value = Word.allOf(bitWidth, Bit.False)
	}

	override fun executionStopped(signalHandler: SignalHandler) {
		super.executionStopped(signalHandler)
		value = Word.allOf(bitWidth, Bit.False)
		stateChanged(signalHandler)
	}

	/** ---- [Storable] */

	override fun read(reader: StoreReader) {
		super.read(reader)
		bitWidth = BitWidth.of(reader.readInt("bitWidth"))
	}

	override fun write(writer: StoreWriter) {
		super.write(writer)
		writer.writeInt("bitWidth", bitWidth.width)
	}

	/** ---- [DipSwitch] */

	fun setBit(index: Int, bit: Bit, signalHandler: SignalHandler) {
		value = value.withBit(index, bit)
		stateChanged(signalHandler)
		requestActingAfter(signalHandler, propagationDelay, GraphActorDataImpl(null, value))
	}

	private fun getDigitalPort(): DigitalPort {
		return getPort<DigitalPort>() as DigitalPort
	}
}