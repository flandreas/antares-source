package io.antarescircuit.antares.model.analog

import io.antarescircuit.antares.model.AntaresGraphTypes.Analog
import io.antarescircuit.antares.model.AntaresGraphTypes.Digital
import io.antarescircuit.antares.model.inout.AbstractCircuitInOut
import io.antarescircuit.antares.model.inout.CircuitInOut
import io.antarescircuit.antares.model.signal.DigitalSignal
import io.antarescircuit.antares.view.analog.engine.AnalogCircuitAnalysis
import io.antarescircuit.antares.view.analog.engine.AnalogElement
import io.antarescircuit.antares.view.analog.engine.AnalogElementMixin
import io.antarescircuit.jabbah.base.logger
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.execution.SignalHandler
import io.antarescircuit.jabbah.execution.actor.ActorImpl
import io.antarescircuit.jabbah.graph.model.*
import io.antarescircuit.jabbah.graph.model.net.CombinedNet
import io.antarescircuit.jabbah.graph.model.net.NetCombiner
import io.antarescircuit.jabbah.graph.model.vertice.VerticeCalculator
import io.antarescircuit.jabbah.io.Storable
import io.antarescircuit.jabbah.io.StoreReader
import io.antarescircuit.jabbah.io.StoreWriter

class AnalogCircuitInOut(
	name: String? = null,
	portType: PortType = PortType.INPUT,
	private val analogElement: AnalogElementMixin = AnalogElementMixin(postCount = 1)
) : AbstractCircuitInOut<AnalogSignal>(
	port = AnalogPort(portType.reverse(), name),
	name = name,
	calculator = CALCULATOR
), AnalogVertice, AnalogElement by analogElement {

	companion object {
		private val LOG by logger(AnalogCircuitInOut::class)

		private val HIGH_VOLTAGE = AnalogSignal.HIGH_VOLTAGE
		private val LOW_VOLTAGE = AnalogSignal.ZERO_VOLTAGE

		private val CALCULATOR = Calculator()

		private class Calculator : VerticeCalculator<AnalogCircuitInOut> {
			override fun calculate(vertice: AnalogCircuitInOut, data: GraphActorData, signalHandler: SignalHandler) {
				vertice.requestAnalogGraphReanalization(signalHandler)
			}
		}
	}

	private val isInput: Boolean get() = getPort<AnalogSignal>().portType.isOutput

	/**
	 * The optional output resistance if this [AnalogCircuitInOut] is operating as output. If `null`, the
	 * output resistance is treated as infinite during simulation (open output).
	 * Non-open outputs can be used for educational purposes to see current flowing out of a circuit,
	 * i.e. a MOS NAND gate when one input is high and the other is low.
	 * Use [Long] so that the already existing 'LongOptionalPropertyEditor' on the JVM platform can be used.
	 */
	var outputResistance: Long? = null
		set(value) {
			if (field != value) {
				field = value
				stateChanged()
			}
		}

	init {
		analogElement.bindAnalogElement(this)
	}

	/** ---- [Storable] */

	override fun write(writer: StoreWriter) {
		super.write(writer)
		if (outputResistance != null) {
			writer.writeLong("outputResistance", outputResistance!!)
		}
	}

	override fun read(reader: StoreReader) {
		super.read(reader)
		outputResistance = if (reader.hasAttribute("outputResistance")) {
			reader.readLong("outputResistance")
		} else {
			null
		}
	}

	/** ---- [GraphPort] */

	override var signal: AnalogSignal? = LOW_VOLTAGE

	/** ---- [GraphInput] */

	override var startValue: AnalogSignal? = null

	override fun setIncomingSignal(signal: AnalogSignal?, signalHandler: SignalHandler, force: Boolean) {
		LOG.trace("Incoming voltage ${signal?.voltage} at input $name")
		this.signal = signal
		stateChanged(signalHandler)
		BaseModule.eventBus.post(AnalogCalculationRequest(this, signalHandler, true))
	}

	/** ---- [NetCombiner] */

	override val isNetCombiner: Boolean get() = false

	override fun requiresCombinedNets(signalHandler: SignalHandler): Boolean = false

	override fun <T : Any> createCombinedNetsFor(
		outputPort: OutputPort<T>,
		inputPort: InputPort<T>,
		signalHandler: SignalHandler
	): Collection<CombinedNet<T>> = emptyList()

	/** ---- [CircuitInOut] */

	override fun setSignalManually(signal: AnalogSignal, signalHandler: SignalHandler) {
		setIncomingSignal(signal, signalHandler)
	}

	override fun inputChanged(input: InputPort<*>, signalHandler: SignalHandler, force: Boolean) {
		if (portType.isOutput) {
			signal = input.net!!.signal as AnalogSignal?
			stateChanged(signalHandler)
		}
	}

	/** ---- [AnalogVertice] */

	override fun handleAnalogPortChanged(port: AnalogPort, signalHandler: SignalHandler) {
		if (portType.isOutput) {
			signal = getPort<AnalogSignal>().net!!.signal!!
			stateChanged(signalHandler)
			setOutgoingSignal(signal!!, signalHandler)
		}
	}

	/** ---- [AnalogElement] */

	override val voltageSourceCount: Int get() = if (isInput) 1 else 0

	override fun stamp(analysis: AnalogCircuitAnalysis) {
		if (isInput) {
			analysis.stampVoltageSource(0, analogElement.nodes[0], analogElement.voltageSource, signal?.voltage ?: AnalogSignal.ZERO_VOLTAGE.voltage)
		} else if (portType == PortType.OUTPUT) {
			if (!isToplevel || outputResistance == null) {
				analysis.stampResistor(analogElement.nodes[0], 0, 1e6)
			} else {
				analysis.stampResistor(analogElement.nodes[0], 0, outputResistance!!.toDouble())
			}
		}
	}

	override fun calculateCurrent() { }

	/** ---- [ActorImpl] */

	override fun executionStart(signalHandler: SignalHandler) {
		super.executionStart(signalHandler)
		signal = LOW_VOLTAGE
	}

	/** ---- [AnalogCircuitInOut] */

	private fun requestAnalogGraphReanalization(signalHandler: SignalHandler) {
		stateChanged(signalHandler, AbstractAnalogVertice.REQUEST_REANALYZE)
	}

	fun toggle(signalHandler: SignalHandler) {
		signal = if (signal == HIGH_VOLTAGE) LOW_VOLTAGE else HIGH_VOLTAGE
		requestActingAfter(signalHandler, propagationDelay.value, createActorData(null))
	}

	private fun setOutgoingSignal(signal: AnalogSignal, signalHandler: SignalHandler) {
		this.signal = signal
		if (portType.isOutput) {
			propagateToSubGraphOutputPort(signal, signalHandler)
		}
	}

	private fun propagateToSubGraphOutputPort(signal: AnalogSignal, signalHandler: SignalHandler) {
		val outgoingSignal = Digital.adaptTo<AnalogSignal, DigitalSignal>(Analog).convertOutgoingSignal(signal)!!
		(subGraphOutputPort as SubGraphOutputPort<DigitalSignal>?)?.propagateSignal(outgoingSignal, signalHandler)
		subGraphOutputPort?.flush(signalHandler, false)
	}
}