package ch.scorpion.antares.model.input

import ch.scorpion.antares.model.port.DigitalPort
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.DigitalSignalFactory
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.execution.actor.ActorData
import ch.scorpion.jabbah.graph.model.GraphActorData
import ch.scorpion.jabbah.graph.model.InputPort
import ch.scorpion.jabbah.graph.model.OutputPort
import ch.scorpion.jabbah.graph.model.net.CombinedNet
import ch.scorpion.jabbah.graph.model.net.NetCombiner
import ch.scorpion.jabbah.graph.model.port.PortImpl

/**
 * An interactive switch with two bi-directional [DigitalPort]s.
 */
class RealSwitch(
	bitWidth: BitWidth = BitWidth.BW_1
) : AbstractRealSwitch<RealSwitch>(CALCULATOR, portCount = 2, bitWidth) {

	companion object {

		private const val BASE_RESOURCE_KEY = "library.element.RealSwitch"
		private val CALCULATOR = Calculator()

		private class Calculator : AbstractRealSwitch.Companion.AbstractCalculator<RealSwitch>() {

			override fun handleInputChanged(data: GraphActorData, vertice: RealSwitch, signalHandler: SignalHandler) {
				if (vertice.isOn) {
					vertice.getOutput<DigitalSignal>(getOppositePortId(data.changedPort!!.portId))
						.setOutgoingSignalBuffered(data.getSignal(data.changedPort!!.portId), signalHandler)
				}
			}

			override fun handleStateChanged(data: GraphActorData, vertice: RealSwitch, signalHandler: SignalHandler) {
				val port1 = vertice.getPort<DigitalSignal>(1) as DigitalPort
				val port2 = vertice.getPort<DigitalSignal>(2) as DigitalPort
				port1.setOutgoingSignalBuffered(DigitalSignalFactory.undefined(vertice.bitWidth), signalHandler)
				port2.setOutgoingSignalBuffered(DigitalSignalFactory.undefined(vertice.bitWidth), signalHandler)

				if (vertice.isOn) {
					// Make sure that re-propagation from origin OutputPort is not blocked at InputPort
					// of this RealSwitch because incoming signal is already set
					(port1 as PortImpl<*>).syncIncomingSignalWithNegotiatedOutgoingSignal(always = true)
					(port2 as PortImpl<*>).syncIncomingSignalWithNegotiatedOutgoingSignal(always = true)
				} else {
					port1.flush(signalHandler, data.force)
					port2.flush(signalHandler, data.force)
				}

				// Re-flush the dominant signals in the Nets of the switched Ports
				vertice.askNetTopologyChangeListenersForResend(signalHandler)
			}
		}

		private fun getOppositePortId(portId: Int): Int =
			if (portId == 1) 2 else 1
	}

	override val type: String get() = Translations.getString("${BASE_RESOURCE_KEY}.name")
	override val typeDesc: String? get() = Translations.getOptionalString("${BASE_RESOURCE_KEY}.desc")

	/** ---- [NetCombiner] interface */

	override fun <T : Any> createCombinedNetsFor(outputPort: OutputPort<T>, inputPort: InputPort<T>, signalHandler: SignalHandler): Collection<CombinedNet<T>> =
		if (isOn) {
			val opposite = getOutput<T>(getOppositePortId(inputPort.portId))
			CombinedNet.createFor(opposite, signalHandler).onEach {
				it.replaceAccessPort(opposite, outputPort)
			}
		} else {
			emptyList()
		}

	override fun flush(signalHandler: SignalHandler, data: ActorData) {
		if ((data as GraphActorData).changedPort != null) {
			if (isOn) {
				getOutput<DigitalSignal>(getOppositePortId(data.changedPort!!.portId)).flush(signalHandler, data.force)
			}
		}
	}
}