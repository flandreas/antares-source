package ch.scorpion.antares.model.analog

import ch.scorpion.antares.model.AntaresGraphTypes.Analog
import ch.scorpion.antares.model.AntaresGraphTypes.Digital
import ch.scorpion.antares.model.inout.AbstractCircuitInOut
import ch.scorpion.antares.model.inout.CircuitInOut
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.view.analog.engine.AnalogElement
import ch.scorpion.antares.view.analog.engine.AnalogElementMixin
import ch.scorpion.antares.view.analog.AnalogGraphView
import ch.scorpion.antares.view.analog.engine.AnalogCircuitAnalysis
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.execution.actor.ActorImpl
import ch.scorpion.jabbah.graph.model.*
import ch.scorpion.jabbah.graph.model.net.CombinedNet
import ch.scorpion.jabbah.graph.model.net.NetCombiner
import ch.scorpion.jabbah.graph.model.vertice.VerticeCalculator
import ch.scorpion.jabbah.graph.view.GraphView

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

		private val HIGH_VOLTAGE = AnalogSignal.HIGH
		private val LOW_VOLTAGE = AnalogSignal.ZERO

		private val CALCULATOR = Calculator()

		private class Calculator : VerticeCalculator<AnalogCircuitInOut> {
			override fun calculate(vertice: AnalogCircuitInOut, data: GraphActorData, signalHandler: SignalHandler) {
				vertice.requestAnalogGraphRecalculation(signalHandler)
			}
		}
	}

	private val isInput: Boolean get() = getPort<AnalogSignal>().portType.isOutput

	init {
		analogElement.bindAnalogElement(this)
	}

	/** ---- [GraphPort] */

	override var signal: AnalogSignal? = LOW_VOLTAGE
		set(value) {
			if (signal != value) {
				field = value
				stateChanged()
			}
		}

	/** ---- [GraphInput] */

	override fun setIncomingSignal(signal: AnalogSignal?, signalHandler: SignalHandler, force: Boolean) {
		LOG.trace("Incoming voltage ${signal?.voltage} at input $name")
		this.signal = signal
		BaseModule.eventBus.post(AnalogCalculationRequest(this, signalHandler))
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
		}
	}

	/** ---- [AnalogVertice] */

	override fun handleAnalogPortChanged(port: AnalogPort, signalHandler: SignalHandler) {
		if (portType.isOutput) {
			val signal = getPort<AnalogSignal>().net!!.signal!!
			LOG.trace("Outgoing voltage ${signal.voltage} at output $name")
			setOutgoingSignal(signal, signalHandler)
		}
	}

	/** ---- [AnalogElement] */

	override val voltageSourceCount: Int get() = if (isInput) 1 else 0

	override fun stamp(analysis: AnalogCircuitAnalysis) {
		if (isInput) {
			analysis.stampVoltageSource(0, analogElement.nodes[0], analogElement.voltageSource, signal?.voltage ?: AnalogSignal.ZERO.voltage)
		}
	}

	override fun calculateCurrent() { }

	/** ---- [ActorImpl] */

	override fun executionStart(signalHandler: SignalHandler) {
		super.executionStart(signalHandler)
		signal = LOW_VOLTAGE
	}

	/** ---- [AnalogCircuitInOut] */

	fun requestAnalogGraphRecalculation(signalHandler: SignalHandler) {
		stateChanged(signalHandler, AbstractAnalogVertice.REQUEST_RECALCULATE)
	}

	fun toggle(signalHandler: SignalHandler) {
		signal = if (signal == HIGH_VOLTAGE) LOW_VOLTAGE else HIGH_VOLTAGE
		requestActingAfter(signalHandler, propagationDelay, createActorData(null))
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