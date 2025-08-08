package ch.scorpion.antares.model.net

import ch.scorpion.antares.model.port.DigitalPort
import ch.scorpion.antares.model.port.DigitalPortImpl
import ch.scorpion.antares.model.signal.*
import ch.scorpion.antares.model.vertice.AdjustableBitWidth
import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.*
import ch.scorpion.jabbah.graph.model.vertice.CalculatingVertice
import ch.scorpion.jabbah.graph.model.vertice.VerticeCalculator
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

/**
 * Displays the value of a [DigitalSignal] within a circuit.
 */
class Probe(
	bitWidth: BitWidth = BitWidth.BW_1,
	hasOutput: Boolean = false,
	private val eventBus: EventBus = BaseModule.eventBus
) : CalculatingVertice(CALCULATOR), DigitalSignalSource {

	companion object {

		private val LOG by logger(Probe::class)

		private const val BASE_RESOURCE_KEY = "library.element.Probe"
		private val TYPE get() = Translations.getString("$BASE_RESOURCE_KEY.name")
		private val TYPE_DESC get() = Translations.getOptionalString("$BASE_RESOURCE_KEY.desc")

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
				if (fixedPointFractionSize != null) {
					fixedPointConfig = FixedPointConfig(0)
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

	var fixedPointFractionSize: Int?
		get() = fixedPointConfig?.fractionSize
		set(value) {
			fixedPointConfig = if (value == null) {
				null
			} else {
				require(value >= 0 && value <= bitWidth.width) { Translations.getString("element.property.fixedPointConfig.fractionSizeRange.error", bitWidth.width) }
				ensureFixedPointConfig().withFractionSize(value)
			}
		}

	var fixedPointSigned: Boolean?
		get() = fixedPointConfig?.signed
		set(value) {
			fixedPointConfig = if (value == null) {
				null
			} else {
				ensureFixedPointConfig().withSigned(value)
			}
		}

	init {
		addPort(DigitalPortImpl(PortType.INPUT, bitWidth = bitWidth, defaultBit = Bit.Undefined))
		this.hasOutput = hasOutput
	}

	/** ---- [GraphElement] */

	override fun graphParamsChanged(graph: Graph) {
		super.graphParamsChanged(graph)
		(bitWidth as? BitWidthExpression)?.let { it.evaluateIn(graph)?.let { bw -> bitWidth = bw } }
	}

	/** ---- [AdjustableBitWidth] */

	override fun adjustBitWidth(portId: Int, bitWidth: BitWidth): Boolean {
		this.bitWidth = bitWidth
		return true
	}

	/** ---- [DigitalSignalSource] interface */

	override var fixedPointConfig: FixedPointConfig? = null

	private fun ensureFixedPointConfig(): FixedPointConfig {
		if (fixedPointConfig == null) {
			fixedPointConfig = FixedPointConfig()
		}
		return fixedPointConfig!!
	}

	@Suppress("UNUSED_PARAMETER")
	override var signal: DigitalSignal?
		get() = getInput<DigitalSignal>().getIncomingSignal()
		set(value) {
			throw UnsupportedOperationException()
		}

	override fun executionInitialize(signalHandler: SignalHandler) {
		super.executionInitialize(signalHandler)
		getInput<DigitalSignal>().setIncomingSignal(DigitalSignalFactory.undefined(bitWidth), signalHandler)
		stateChanged(signalHandler)
	}

	override fun executionStart(signalHandler: SignalHandler) {
		super.executionStart(signalHandler)
		requestActingAfter(signalHandler, propagationDelay.value / 2, createActorData(null))
	}

	/** ---- [Storable] interface */

	override fun write(writer: StoreWriter) {
		super.write(writer)
		bitWidth.write("bitWidth", writer)
		writer.writeBoolean("hasOutput", hasOutput)
		if (isLogging) {
			writer.writeBoolean("logging", isLogging)
		}
		fixedPointConfig?.let { writer.writeStorable("fixedPoint", it) }
	}

	override fun read(reader: StoreReader) {
		super.read(reader)
		bitWidth = BitWidth.read("bitWidth", reader)
		hasOutput = reader.readBoolean("hasOutput")
		if (reader.hasAttribute("logging")) {
			isLogging = reader.readBoolean("logging")
		}
		if (reader.hasElement("fixedPoint")) {
			fixedPointConfig = reader.readStorable("fixedPoint")
		}
	}

	/** ---- [Probe] */

	private fun setSignal(signal: DigitalSignal, signalHandler: SignalHandler) {
		stateChanged()
		if (outputCount > 0) {
			getOutput<DigitalSignal>().setOutgoingSignalBuffered(signal, signalHandler)
		}
	}

	private fun sendLogEvent(data: GraphActorData, time: Long) {
		eventBus.post(LogEvent(
			source = this,
			name = StringUtils.orElse(name, "<Unknown>"),
			value = data.getSignal<DigitalSignal>(1)?.hexString ?: "<empty>",
			time = time))
	}
}