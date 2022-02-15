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

				// Set blind port to undefined
				vertice.getOutput<DigitalSignal>(getBlindPortId(vertice)).apply {
					setOutgoingSignalBuffered(DigitalSignalFactory.undefined(vertice.bitWidth), signalHandler)
					// Flushing in DoubleThrowSwitch.flush() could overwrite the value established
					// by resending the value of the NetTopologyChangeListener (see below)
					flush(signalHandler)
				}

				setOf(1, getOppositePortId(1, vertice.isOn))
					.map { vertice.getOutput<DigitalSignal>(it) }
					.forEach { output ->
						output.setOutgoingSignalBuffered(DigitalSignalFactory.undefined(vertice.bitWidth), signalHandler)

						// Make sure that re-propagation from origin OutputPort is not blocked at InputPort
						// of this DoubleThrowSwitch because incoming signal is already set
						(output as PortImpl<*>).syncIncomingSignalWithNegotiatedOutgoingSignal(always = true)
					}

				// Re-flush the dominant signals in the Nets of the switched Ports
				vertice.askNetTopologyChangeListenersForResend(signalHandler)
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

		private fun getBlindPortId(vertice: DoubleThrowSwitch): Int =
			if (vertice.isOn) 3 else 2

	}

	override val type: String get() = Translations.getString("${BASE_RESOURCE_KEY}.name")
	override val typeDesc: String? get() = Translations.getOptionalString("${BASE_RESOURCE_KEY}.desc")

	/** ---- [NetCombiner] interface */

	// TODO: Move this up to AbstractRealSwitch, which should work the same
	override fun requiresCombinedNets(signalHandler: SignalHandler): Boolean = false

	override fun <T : Any> createCombinedNetsFor(outputPort: OutputPort<T>, inputPort: InputPort<T>, signalHandler: SignalHandler): Collection<CombinedNet<T>> {
		return when (inputPort.portId) {
			1 -> {
				val opposite = getOutput<T>(getOppositePortId(1, isOn))
				CombinedNet.createFor(opposite, signalHandler).onEach {
					it.replaceAccessPort(opposite, outputPort)
				}
			}
			2, 3 -> if (isBlindInput(inputPort.portId, isOn)) {
				emptyList()
			} else {
				CombinedNet.createFor<T>(getOutput(1), signalHandler).onEach {
					it.replaceAccessPort(getOutput(1), outputPort)
				}
			}
			else -> throw IllegalArgumentException("Unsupported inputPort ID")
		}
	}

	/** ---- [CalculatingVertice] interface */

	override fun flush(signalHandler: SignalHandler, data: ActorData) {
		if ((data as GraphActorData).changedPort != null) {
			if (!isBlindInput(data.changedPort!!.portId, isOn)) {
				getOutput<DigitalSignal>(getOppositePortId(data.changedPort!!.portId, isOn)).flush(signalHandler)
			}
		}
	}
}