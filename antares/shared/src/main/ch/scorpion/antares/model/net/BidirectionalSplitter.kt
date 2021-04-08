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
import ch.scorpion.jabbah.execution.actor.ActorData
import ch.scorpion.jabbah.graph.model.*
import ch.scorpion.jabbah.graph.model.net.CombinedNet
import ch.scorpion.jabbah.graph.model.net.NetCombiner
import ch.scorpion.jabbah.graph.model.net.SignalConverter
import ch.scorpion.jabbah.graph.model.net.SignalPropagationChain
import ch.scorpion.jabbah.graph.model.port.PortImpl
import ch.scorpion.jabbah.graph.model.vertice.VerticeCalculator
import ch.scorpion.jabbah.graph.model.vertice.CalculatingVertice

class BidirectionalSplitter(
	bitWidth: BitWidth = BitWidth.BW_8,
	branchCount: BranchCount = BranchCount.BC_4
) : AbstractSplitter(bitWidth, branchCount, CALCULATOR), NetCombiner<DigitalSignal> {

	companion object {

		private val LOG by logger(BidirectionalSplitter::class)

		private const val BASE_RESOURCE_KEY = "library.element.BidirectionalSplitter"
		private val TYPE = Translations.getString("$BASE_RESOURCE_KEY.name")
		private val TYPE_DESC = Translations.getOptionalString("$BASE_RESOURCE_KEY.desc")

		private val SIGNAL_SPLITTING_CACHE = mutableMapOf<SignalSplitterKey, SignalSplitter>()

		private val CALCULATOR = Calculator()

		private class Calculator : VerticeCalculator<AbstractSplitter> {
			override fun calculate(vertice: AbstractSplitter, data: GraphActorData, signalHandler: SignalHandler) {
				val changedPortId = data.changedPort!!.portId
				if (changedPortId == 1) {
					split(data.getSignal(1)!!, vertice, signalHandler)
				} else {
					combine(vertice, signalHandler)
				}
			}

			private fun split(signal: DigitalSignal, vertice: AbstractSplitter, signalHandler: SignalHandler) {
				for (portId in 2..vertice.portsCount) {
					val outputPort = vertice.getPort<DigitalSignal>(portId) as DigitalPort
					val key = SignalSplitterKey(vertice.bitWidth, vertice.narrowSideBitWidth, portId - 2)
					val outSignal = getSignalSplitter(key).convert(signal)
					outputPort.setOutgoingSignalBuffered(outSignal, signalHandler)
				}
			}

			private fun combine(vertice: AbstractSplitter, signalHandler: SignalHandler) {
				val words = mutableListOf<Word>()
				for (portId in 2..vertice.portsCount) {
					val signal = (vertice.getPort<DigitalPort>(portId) as DigitalPort).getIncomingSignal() as Word
					words.add(signal)
				}
				val output = Word.of(words)
				(vertice.getPort<DigitalSignal>(1) as DigitalPort).setOutgoingSignalBuffered(output, signalHandler)
			}
		}

		private fun getSignalSplitter(key: SignalSplitterKey): SignalSplitter =
			SIGNAL_SPLITTING_CACHE.getOrPut(key) { SignalSplitter(key) }

		private data class SignalSplitterKey(
			val wideSideBitWidth: BitWidth,
			val narrowSideBitWidth: BitWidth,
			val index: Int
		) {
			override fun toString(): String =
				"wide=${wideSideBitWidth.width},narrow=${narrowSideBitWidth.width},index=$index"
		}

		private data class SignalSplitter(private val key: SignalSplitterKey) : SignalConverter<DigitalSignal> {
			override fun convert(signal: DigitalSignal?): DigitalSignal? =
				signal?.getSubword(key.narrowSideBitWidth, key.index)

			override fun toString(): String = "SignalSplitter $key"
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

	/** ---- [CalculatingVertice] */

	override fun flush(signalHandler: SignalHandler, data: ActorData) {
		if ((data as GraphActorData).changedPort === wideSidePort) {
			flushNarrowSide(signalHandler, data)
		} else {
			flushWideSide(signalHandler, data)
		}
	}

	private fun flushNarrowSide(signalHandler: SignalHandler, data: ActorData) {
		narrowSidePorts.forEach {
			it.flush(signalHandler)
			(it as PortImpl<DigitalSignal>).syncIncomingSignalWithNegotiatedOutgoingSignal()
		}
	}

	private fun flushWideSide(signalHandler: SignalHandler, data: ActorData) {
		wideSidePort.flush(signalHandler)
		(wideSidePort as PortImpl<DigitalSignal>).syncIncomingSignalWithNegotiatedOutgoingSignal()
	}

	/** ---- [NetCombiner] interface */

	override fun getSignalPropagationChains(inputPort: InputPort<DigitalSignal>): Collection<SignalPropagationChain<DigitalSignal>> {
		return if (inputPort === wideSidePort) {
			getNarrowSignalPropagationChains()
		} else {
			getWideSignalPropagationChain(inputPort)
		}
	}

	private fun getWideSignalPropagationChain(inputPort: InputPort<DigitalSignal>): Collection<SignalPropagationChain<DigitalSignal>> {
		val chains = CombinedNet.fromOutputPort(wideSidePort).chains
		val converter = SignalCombiner(inputPort.portId)
		chains.forEach { it.extendHead(converter, inputPort, wideSidePort) }
		return chains
	}

	private fun getNarrowSignalPropagationChains(): Collection<SignalPropagationChain<DigitalSignal>> {
		val result = mutableListOf<SignalPropagationChain<DigitalSignal>>()
		for (portId in 2..portsCount) {
			val port = getOutput<DigitalSignal>(portId)
			val chains = CombinedNet.fromOutputPort(port).chains
			val key = SignalSplitterKey(bitWidth, narrowSideBitWidth, portId - 2)
			val splitter = getSignalSplitter(key)
			chains.forEach {
				it.extendHead(splitter, wideSidePort, port)
			}
			result.addAll(chains)
		}
		return result
	}

	private inner class SignalCombiner(
		private val signalPortId: Int
	) : SignalConverter<DigitalSignal> {

		override fun convert(signal: DigitalSignal?): DigitalSignal {
			val words = mutableListOf<Word>()
			for (portId in 2..portsCount) {
				if (portId == signalPortId) {
					words.add(signal as Word)
				} else {
					val s = (getPort<DigitalPort>(portId) as DigitalPort).getIncomingSignal() as Word
					words.add(s)
				}
			}
			return Word.of(words)
		}

		override fun toString(): String =
			"SignalCombiner for port $signalPortId in ${this@BidirectionalSplitter.id}"
	}
}