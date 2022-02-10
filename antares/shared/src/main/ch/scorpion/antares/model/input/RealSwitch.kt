package ch.scorpion.antares.model.input

import ch.scorpion.antares.model.port.DigitalPort
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.DigitalSignalFactory
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.execution.SignalHandler
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
					val outputPortId = if (data.changedPort!!.portId == 1) 2 else 1
					vertice.getOutput<DigitalSignal>(outputPortId).setOutgoingSignalBuffered(data.getSignal(data.changedPort!!.portId), signalHandler)
				}
			}

			override fun handleStateChanged(vertice: RealSwitch, signalHandler: SignalHandler) {
				val port1 = vertice.getPort<DigitalSignal>(1) as DigitalPort
				val port2 = vertice.getPort<DigitalSignal>(2) as DigitalPort
				port1.setOutgoingSignalBuffered(DigitalSignalFactory.undefined(vertice.bitWidth), signalHandler)
				port2.setOutgoingSignalBuffered(DigitalSignalFactory.undefined(vertice.bitWidth), signalHandler)

				if (vertice.isOn) {
					// Make sure that re-propagation from origin OutputPort is not blocked at InputPort
					// of this RealSwitch because incoming signal is already set
					(port1 as PortImpl<*>).syncIncomingSignalWithNegotiatedOutgoingSignal(always = true)
					(port2 as PortImpl<*>).syncIncomingSignalWithNegotiatedOutgoingSignal(always = true)
				}
			}
		}
	}

	override val type: String get() = Translations.getString("${BASE_RESOURCE_KEY}.name")
	override val typeDesc: String? get() = Translations.getOptionalString("${BASE_RESOURCE_KEY}.desc")

	/** ---- [NetCombiner] interface */

	override fun <T : Any> createCombinedNetsFor(outputPort: OutputPort<T>, inputPort: InputPort<T>, signalHandler: SignalHandler): Collection<CombinedNet<T>> {
		return if (isOn) {
			CombinedNet.createFor(outputPort, signalHandler)
		} else {
			emptyList()
		}
	}
}