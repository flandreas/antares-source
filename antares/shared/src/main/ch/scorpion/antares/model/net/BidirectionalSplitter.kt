package ch.scorpion.antares.model.net

import ch.scorpion.antares.model.Logic
import ch.scorpion.antares.model.port.DigitalPort
import ch.scorpion.antares.model.port.DigitalPortImpl
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.Word
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.GraphActorData
import ch.scorpion.jabbah.graph.model.NetCombiner
import ch.scorpion.jabbah.graph.model.OutputPort
import ch.scorpion.jabbah.graph.model.PortType
import ch.scorpion.jabbah.graph.model.net.CombinedNet
import ch.scorpion.jabbah.graph.model.vertice.VerticeCalculator

class BidirectionalSplitter(
	bitWidth: BitWidth = BitWidth.BW_8,
	branchCount: BranchCount = BranchCount.BC_4
) : AbstractSplitter(bitWidth, branchCount, CALCULATOR), NetCombiner {

	companion object {

		private val LOG by logger(BidirectionalSplitter::class)

		private const val BASE_RESOURCE_KEY = "library.element.BidirectionalSplitter"
		private val TYPE = Translations.getString("$BASE_RESOURCE_KEY.name")
		private val TYPE_DESC = Translations.getOptionalString("$BASE_RESOURCE_KEY.desc")

		private val CALCULATOR = Calculator()

		private class Calculator : VerticeCalculator<AbstractSplitter> {
			override fun calculate(vertice: AbstractSplitter, data: GraphActorData, signalHandler: SignalHandler) {
				val changedPortId = data.changedPort!!.portId
				LOG.info("changedPortId=$changedPortId")
				if (changedPortId == 1) {
					split(data.getSignal(1)!!, vertice, signalHandler)
				} else {
					combine(vertice, signalHandler)
				}
			}

			private fun split(signal: DigitalSignal?, vertice: AbstractSplitter, signalHandler: SignalHandler) {
				LOG.info("split")
				for (portId in 2..vertice.portsCount) {
					val outputPort = vertice.getPort<DigitalSignal>(portId) as DigitalPort
					if (signal == null) {
						LOG.info("-> port $portId: undefined")
						outputPort.setOutgoingSignalBuffered(Word.undefined(vertice.narrowSideBitWidth), signalHandler)
					} else {
						val outSignal = signal.getSubword(vertice.narrowSideBitWidth, portId - 2)
						LOG.info("-> port $portId: ${outSignal.toBinaryString()}")
						outputPort.setOutgoingSignalBuffered(outSignal, signalHandler)
					}
				}
			}

			private fun combine(vertice: AbstractSplitter, signalHandler: SignalHandler) {
				LOG.info("combine")
				val words = mutableListOf<Word>()
				for (portId in 2..vertice.portsCount) {
					val signal = (vertice.getPort<DigitalPort>(portId) as DigitalPort).getIncomingSignal() as Word
					LOG.info("-> port $portId: ${signal.toBinaryString()}")
					words.add(signal)
				}
				val output = Word.of(words)
				LOG.info("=> combination: ${output.toBinaryString()}")
				(vertice.getPort<DigitalSignal>(1) as DigitalPort).setOutgoingSignalBuffered(output, signalHandler)
			}
		}
	}

	override val type: String get() = TYPE
	override val typeDesc: String? get() = TYPE_DESC

	override fun createWideSidePort(): DigitalPort =
		DigitalPortImpl(PortType.INOUT, null, Logic.POSITIVE, bitWidth = bitWidth, canBeUndefined = true, signalRepresentation = signalRepresentation)

	override fun createNarrowSidePort(index: Int): DigitalPort =
		DigitalPortImpl(PortType.INOUT, null, Logic.POSITIVE, bitWidth = narrowSideBitWidth, canBeUndefined = true, signalRepresentation = signalRepresentation)

	override val wideSidePort: DigitalPort get() = getPort<DigitalSignal>(1) as DigitalPort

	override val narrowSidePorts: List<DigitalPort> get() = getPorts().filterIndexed { index, _ -> index > 0 }.map { it as DigitalPort }

	/** ---- [NetCombiner] interface */

	override fun getCombinedNetOutputPorts(outputPort: OutputPort<*>): Collection<OutputPort<*>> {
		return if (outputPort === wideSidePort) {
			var result = mutableListOf<OutputPort<*>>()
			narrowSidePorts.forEach {
				CombinedNet.fromNet(it.net, excluding = it).outputPorts.forEach { result.add(it) }
			}
			result
		} else {
			CombinedNet.fromOutputPort(wideSidePort).outputPorts
		}
	}
}