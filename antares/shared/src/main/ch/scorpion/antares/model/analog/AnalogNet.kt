package ch.scorpion.antares.model.analog

import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.Net
import ch.scorpion.jabbah.graph.model.OutputPort
import ch.scorpion.jabbah.graph.model.net.NetImpl

class AnalogNet : NetImpl<AnalogSignal>() {

	companion object {
		private val LOG by logger(AnalogNet::class)
	}

	override val signal: AnalogSignal?
		get() = super.signal ?: AnalogSignal.ZERO

	override fun cloneEmpty(): Net<AnalogSignal> = AnalogNet()

	override fun setSignal(
		signal: AnalogSignal?,
		origin: OutputPort<AnalogSignal>,
		immediatePort: OutputPort<AnalogSignal>,
		signalHandler: SignalHandler,
		force: Boolean
	) {
		// Don't call super.setSignal() to avoid requestActingAfter()
		setSignal(signal, signalHandler)
	}

	fun setSignal(signal: AnalogSignal?, signalHandler: SignalHandler) {
		signal?.let {
			LOG.trace("Set AnalogSignal ${it.voltage} on AnalogNet $id")
			updateSignal(it)
			ports.map { port -> port as AnalogPort }.forEach { port -> port.handleAnalogSignalChanged(it, signalHandler) }
		}
	}
}