package ch.scorpion.antares.model.net

import ch.scorpion.antares.model.port.DigitalPort
import ch.scorpion.antares.model.port.DigitalPortImpl
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.DigitalSignalSource
import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.exception.UnsupportedOperationException
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.GraphActorData
import ch.scorpion.jabbah.graph.model.LogEvent
import ch.scorpion.jabbah.graph.model.vertice.CalculatingVertice
import ch.scorpion.jabbah.graph.model.vertice.VerticeCalculator
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

/**
 * Displays the value of a [DigitalSignal] within a circuit.
 */
class Probe(
	hasOutput: Boolean = false,
	private val eventBus: EventBus = BaseModule.eventBus
) : CalculatingVertice(CALCULATOR), DigitalSignalSource {

	companion object {

		private val LOG by logger(Probe::class)

		private const val BASE_RESOURCE_KEY = "library.element.Probe"
		private val TYPE = Translations.getString("$BASE_RESOURCE_KEY.name")
		private val TYPE_DESC = Translations.getOptionalString("$BASE_RESOURCE_KEY.desc")

		private val CALCULATOR = Calculator()

		private class Calculator : VerticeCalculator<Probe> {
			override fun calculate(vertice: Probe, data: GraphActorData, signalHandler: SignalHandler) {
				if (vertice.isLogging) {
					LOG.info("${signalHandler.executionTime} Probe '${vertice.name}': ${data.getSignal<DigitalSignal>(1)}")
					vertice.sendLogEvent(data, signalHandler.executionTime)
				}
				vertice.setSignal(data.getSignal(1)!!, signalHandler)
			}
		}
	}

	override val type: String get() = TYPE
	override val typeDesc: String? get() = TYPE_DESC

	private fun sendLogEvent(data: GraphActorData, time: Long) {
		eventBus.post(LogEvent(
			source = this,
			name = StringUtils.orElse(name, "<Unknown>"),
			value = data.getSignal<DigitalSignal>(1)?.toHexString() ?: "<empty>",
			time = time))
	}

	/** Write a log message whenever the input changes */
	var isLogging: Boolean = false

	override var bitWidth: BitWidth
		get() = (getInput<DigitalSignal>() as DigitalPort).bitWidth
		set(value) {
			if (value != bitWidth) {
				(getInput<DigitalSignal>() as DigitalPort).bitWidth = value
				if (hasOutput) {
					(getOutput<DigitalSignal>() as DigitalPort).bitWidth = value
				}
				stateChanged()
			}
		}

	var hasOutput: Boolean
		get() = outputCount > 0
		set(value) {
			if (value == hasOutput) {
				return
			}
			if (value) {
				val output = DigitalPortImpl.createOutput()
				output.bitWidth = bitWidth
				addPort(output)
			} else {
				removePort(getOutput<DigitalSignal>())
			}
			stateChanged()
		}

	init {
		addPort(DigitalPortImpl.createInput())
		this.hasOutput = hasOutput
	}

	/** ---- [DigitalSignalSource] interface */

	@Suppress("UNUSED_PARAMETER")
	override var signal: DigitalSignal?
		get() = getInput<DigitalSignal>().getIncomingSignal()
		set(value) {
			throw UnsupportedOperationException()
		}

	override fun executionStarted(signalHandler: SignalHandler) {
		super.executionStarted(signalHandler)
		stateChanged(signalHandler)
	}

	/** ---- [Storable] interface */

	override fun write(writer: StoreWriter) {
		super.write(writer)
		writer.writeInt("bitWidth", bitWidth.width)
		writer.writeBoolean("hasOutput", hasOutput)
		if (isLogging) {
			writer.writeBoolean("logging", isLogging)
		}
	}

	override fun read(reader: StoreReader) {
		super.read(reader)
		bitWidth = BitWidth.of(reader.readInt("bitWidth"))
		hasOutput = reader.readBoolean("hasOutput")
		if (reader.hasAttribute("logging")) {
			isLogging = reader.readBoolean("logging")
		}
	}

	/** ---- [Probe] */

	private fun setSignal(signal: DigitalSignal, signalHandler: SignalHandler) {
		stateChanged()
		if (outputCount > 0) {
			getOutput<DigitalSignal>().setOutgoingSignalBuffered(signal, signalHandler)
		}
	}
}