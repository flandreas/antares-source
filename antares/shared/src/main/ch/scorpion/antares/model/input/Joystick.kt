package ch.scorpion.antares.model.input

import ch.scorpion.antares.model.Logic
import ch.scorpion.antares.model.gate.AbstractDigitalGate
import ch.scorpion.antares.model.port.DigitalPort
import ch.scorpion.antares.model.port.DigitalPortImpl
import ch.scorpion.antares.model.signal.Bit
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.Word
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.execution.actor.Actor
import ch.scorpion.jabbah.graph.model.GraphActorData
import ch.scorpion.jabbah.graph.model.GraphActorDataImpl
import ch.scorpion.jabbah.graph.model.vertice.CalculatingVertice
import ch.scorpion.jabbah.graph.model.vertice.VerticeCalculator
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

class Joystick(bitWidth: BitWidth = BitWidth.BW_2) : CalculatingVertice(CALCULATOR) {

	companion object {
		private const val BASE_RESOURCE_KEY = "library.element.Joystick"
		private const val MIN_DISPLACEMENT = 0.5
		const val PORT_NAME_X = "X"
		const val PORT_NAME_Y = "Y"

		private val CALCULATOR = Calculator()

		private class Calculator : VerticeCalculator<Joystick> {
			override fun calculate(vertice: Joystick, data: GraphActorData, signalHandler: SignalHandler) {
				vertice.getOutput<DigitalSignal>(PORT_NAME_X).setOutgoingSignalBuffered(vertice.signalX, signalHandler)
				vertice.getOutput<DigitalSignal>(PORT_NAME_Y).setOutgoingSignalBuffered(vertice.signalY, signalHandler)
			}
		}
	}

	override val type: String get() = Translations.getString("$BASE_RESOURCE_KEY.name")
	override val typeDesc: String get() = Translations.getString("$BASE_RESOURCE_KEY.desc")

	/**
	 * The position of the knob that determines the x and y output signals.
	 * Neutral position is at (0,0). Has to be normalized to be in range -1 .. +1 for both coordinates.
	 */
	var knobPosition: Point2D = Point2D.ZERO

	var bitWidth: BitWidth
		get() = (getOutput<DigitalSignal>() as DigitalPort).bitWidth
		set(value) {
			if (value != bitWidth) {
				(getOutput<DigitalSignal>(PORT_NAME_X) as DigitalPort).bitWidth = value
				(getOutput<DigitalSignal>(PORT_NAME_Y) as DigitalPort).bitWidth = value
				stateChanged()
			}
		}

	init {
		propagationDelay = AbstractDigitalGate.DEFAULT_PROPAGATION_DELAY
		addPort(DigitalPortImpl.createOutput(Logic.POSITIVE, PORT_NAME_X, bitWidth))
		addPort(DigitalPortImpl.createOutput(Logic.POSITIVE, PORT_NAME_Y, bitWidth))
	}

	fun setKnobPosition(position: Point2D, signalHandler: SignalHandler) {
		knobPosition = position
		requestActingAfter(signalHandler, propagationDelay, GraphActorDataImpl(null, null))
	}

	/** ---- [Storable] interface */

	override fun write(writer: StoreWriter) {
		super.write(writer)
		writer.writeInt("bitWidth", bitWidth.width)
	}

	override fun read(reader: StoreReader) {
		super.read(reader)
		bitWidth = BitWidth.of(reader.readInt("bitWidth"))
	}

	/** ---- [Actor] interface */

	override fun executionInitialize(signalHandler: SignalHandler) {
		super.executionInitialize(signalHandler)
		knobPosition = Point2D.ZERO
		getOutput<DigitalSignal>("X").setOutgoingSignalBuffered(signalX, signalHandler)
		getOutput<DigitalSignal>("Y").setOutgoingSignalBuffered(signalY, signalHandler)
	}

	override fun executionStart(signalHandler: SignalHandler) {
		super.executionStart(signalHandler)
		requestActingAfter(signalHandler, propagationDelay / 2, createActorData(null))
	}

	override fun executionStopped(signalHandler: SignalHandler) {
		super.executionStopped(signalHandler)
		knobPosition = Point2D.ZERO
	}

	private val signalX: DigitalSignal get() {
		return when {
			knobPosition.x < -MIN_DISPLACEMENT -> Word.of(bitWidth, 1)
			knobPosition.x > MIN_DISPLACEMENT -> Word.allOf(bitWidth, Bit.True)
			else -> Word.of(bitWidth, 0).flip(bitWidth.width - 1)
		}
	}

	private val signalY: DigitalSignal get() {
		return when {
			knobPosition.y < -MIN_DISPLACEMENT -> Word.of(bitWidth, 1)
			knobPosition.y > MIN_DISPLACEMENT -> Word.allOf(bitWidth, Bit.True)
			else -> Word.of(bitWidth, 0).flip(bitWidth.width - 1)
		}
	}
}