package ch.scorpion.antares.model.input

import ch.scorpion.antares.model.port.DigitalPort
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.DigitalSignalFactory
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.execution.actor.ActorData
import ch.scorpion.jabbah.graph.model.vertice.CalculatingVertice
import ch.scorpion.jabbah.graph.model.GraphActorData
import ch.scorpion.jabbah.graph.model.InputPort
import ch.scorpion.jabbah.graph.model.OutputPort
import ch.scorpion.jabbah.graph.model.Port
import ch.scorpion.jabbah.graph.model.net.CombinedNet
import ch.scorpion.jabbah.graph.model.net.NetCombiner
import ch.scorpion.jabbah.graph.model.port.PortImpl

/**
 * An interactive switch with two bi-directional [DigitalPort]s.
 *
 * [Port] 1 is the single [Port] at one side, and [Ports][Port] 2 and 3 are the two on the other side.
 * Interprets property [AbstractSwitch.isOn] as `true` if [Port] 2 is active, and as `false`
 * if [Port] 3 is active.
 */
class DoubleThrowSwitch(
	bitWidth: BitWidth = BitWidth.BW_1
) : AbstractRealSwitch<DoubleThrowSwitch>(CALCULATOR, portCount = 3, bitWidth) {

	companion object {

		private const val BASE_RESOURCE_KEY = "library.element.DoubleThrowSwitch"
		private val CALCULATOR = Calculator()

		private class Calculator : AbstractRealSwitch.Companion.AbstractCalculator<DoubleThrowSwitch>() {

			override fun handleInputChanged(data: GraphActorData, vertice: DoubleThrowSwitch, signalHandler: SignalHandler) {
				if (!isBlindInput(data.changedPort!!.portId, vertice.isOn)) {
					vertice.getOutput<DigitalSignal>(getOppositePortId(data.changedPort!!.portId, vertice.isOn))
						.setOutgoingSignalBuffered(data.getSignal(data.changedPort!!.portId), signalHandler)
				}
			}

			override fun handleStateChanged(vertice: DoubleThrowSwitch, signalHandler: SignalHandler) {
				val port1 = vertice.getPort<DigitalSignal>(1) as DigitalPort
				val port2 = vertice.getPort<DigitalSignal>(2) as DigitalPort
				val port3 = vertice.getPort<DigitalSignal>(3) as DigitalPort

				port1.setOutgoingSignalBuffered(DigitalSignalFactory.undefined(vertice.bitWidth), signalHandler)
				port2.setOutgoingSignalBuffered(DigitalSignalFactory.undefined(vertice.bitWidth), signalHandler)
				port3.setOutgoingSignalBuffered(DigitalSignalFactory.undefined(vertice.bitWidth), signalHandler)

				// Make sure that re-propagation from origin OutputPort is not blocked at InputPort
				// of this DoubleThrowSwitch because incoming signal is already set
				if (vertice.isOn) {
					(port1 as PortImpl<*>).syncIncomingSignalWithNegotiatedOutgoingSignal(always = true)
					(port2 as PortImpl<*>).syncIncomingSignalWithNegotiatedOutgoingSignal(always = true)
				} else {
					(port1 as PortImpl<*>).syncIncomingSignalWithNegotiatedOutgoingSignal(always = true)
					(port3 as PortImpl<*>).syncIncomingSignalWithNegotiatedOutgoingSignal(always = true)
				}
			}
		}

		private fun getOppositePortId(portId: Int, isOn: Boolean): Int =
			when (portId) {
				1 -> if (isOn) 2 else 3
				2 -> 1
				3 -> 1
				else -> throw IllegalArgumentException("Unsupported portId")
			}

		private fun isBlindInput(portId: Int, isOn: Boolean): Boolean =
			portId == 2 && !isOn || portId == 3 && isOn

	}

	override val type: String get() = Translations.getString("${BASE_RESOURCE_KEY}.name")
	override val typeDesc: String? get() = Translations.getOptionalString("${BASE_RESOURCE_KEY}.desc")

	/** ---- [NetCombiner] interface */

	override fun <T : Any> createCombinedNetsFor(outputPort: OutputPort<T>, inputPort: InputPort<T>, signalHandler: SignalHandler): Collection<CombinedNet<T>> {
		return when (outputPort.portId) {
			1 -> if (inputPort.portId == getOppositePortId(1, isOn)) {
				CombinedNet.createFor(outputPort, signalHandler)
			} else {
				emptyList()
			}
			2, 3 -> if (inputPort.portId == 1) {
				CombinedNet.createFor(outputPort, signalHandler)
			} else {
				emptyList()
			}
			else -> throw IllegalArgumentException("Unsupported outputPort ID")
		}
	}

	/** ---- [CalculatingVertice] interface */

	override fun flush(signalHandler: SignalHandler, data: ActorData) {
		if ((data as GraphActorData).changedPort == null) {
			getOutput<DigitalSignal>(1).flush(signalHandler)
			if (isOn) {
				getOutput<DigitalSignal>(2).flush(signalHandler)
			} else {
				getOutput<DigitalSignal>(3).flush(signalHandler)
			}
			return
		}

		if (!isBlindInput(data.changedPort!!.portId, isOn)) {
			getOutput<DigitalSignal>(getOppositePortId(data.changedPort!!.portId, isOn)).flush(signalHandler)
		}
	}
}