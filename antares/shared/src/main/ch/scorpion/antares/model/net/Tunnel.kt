package ch.scorpion.antares.model.net

import ch.scorpion.antares.model.port.DigitalPort
import ch.scorpion.antares.model.port.DigitalPortImpl
import ch.scorpion.antares.model.signal.Bit
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.Word
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.execution.actor.Actor
import ch.scorpion.jabbah.graph.model.*
import ch.scorpion.jabbah.graph.model.vertice.AbstractVertice
import ch.scorpion.jabbah.graph.model.vertice.CalculatingVertice
import ch.scorpion.jabbah.graph.model.vertice.VerticeCalculator
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

/**
 * A [Tunnel] forwards a signal to other [Tunnel]s with the same name without the
 * need to explicitly connect them by a [Net].
 * [Tunnel]s with the same name are only connected within the same [Graph].
 * The owning [Graph] will be informed by [stateChanged()], which gets already
 * called by [AbstractVertice.inputChanged].
 */
class Tunnel(
	name: String? = null
) : CalculatingVertice(CALCULATOR) {

	companion object {

		private val LOG by logger(Tunnel::class)

		private const val BASE_RESOURCE_KEY = "library.element.Tunnel"
		private val TYPE get() = Translations.getString("$BASE_RESOURCE_KEY.name")
		private val TYPE_DESC get() = Translations.getOptionalString("$BASE_RESOURCE_KEY.desc")

		private val CALCULATOR = Calculator()

		private class Calculator : VerticeCalculator<Tunnel> {
			override fun calculate(vertice: Tunnel, data: GraphActorData, signalHandler: SignalHandler) {
				(vertice.getPort<DigitalSignal>() as DigitalPort).isOutputDominant = true
				LOG.trace("Calculate Tunnel ${vertice.id} with signal '${data.getSignal<DigitalSignal>(1)}'")
				vertice.getOutput<DigitalSignal>().setOutgoingSignalBuffered(data.getSignal(1), signalHandler)
			}
		}
	}

	init {
		this.name = name
		addPort(DigitalPortImpl.createInOut())
	}

	override val type: String get() = TYPE
	override val typeDesc: String? get() = TYPE_DESC

	var bitWidth: BitWidth
		get() = (getOutput<DigitalSignal>() as DigitalPort).bitWidth
		set(newValue) {
			if (newValue != bitWidth) {
				(getOutput<DigitalSignal>() as DigitalPort).bitWidth = newValue
				stateChanged()
			}
		}

	/** ---- [AbstractVertice] */

	override fun inputChanged(input: InputPort<*>, signalHandler: SignalHandler) {
		(getPort<DigitalSignal>() as DigitalPort).isOutputDominant = false
		stateChanged(signalHandler)
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

	/** ---- [Actor] */

	override fun executionStarted(signalHandler: SignalHandler) {
		super.executionStarted(signalHandler)
		val signal = Word.allOf(bitWidth, Bit.Undefined)
		requestActingAfter(signalHandler, propagationDelay, GraphActorDataImpl(null, signal))
	}

	/** ---- [Tunnel] */

	/**
	 * Called by the owning [Graph] after detection of a signal change from a [Tunnel]
	 * with the same name like this [Tunnel].
	 */
	fun setSignal(signal: DigitalSignal, signalHandler: SignalHandler) {
		if (signal != getOutput<DigitalSignal>().getOutgoingSignal()) {
			LOG.trace("Tunnel $id: setSignal '$signal'")
			requestActingAfter(signalHandler, propagationDelay, GraphActorDataImpl(null, signal))
		}
	}

	fun getIncomingSignal(): DigitalSignal {
		return getInput<DigitalSignal>().getIncomingSignal()!!
	}

	fun getOutgoingSignal(): DigitalSignal {
		return getOutput<DigitalSignal>().getOutgoingSignal()!!
	}

	fun getInOrOutSignal(): DigitalSignal {
		if ((getIncomingSignal() as Word).isAllOf(Bit.Undefined)) {
			return getOutgoingSignal()
		}
		return getIncomingSignal()
	}
}
