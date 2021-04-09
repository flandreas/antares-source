package ch.scorpion.antares.model.net

import ch.scorpion.antares.model.port.DigitalPort
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.DigitalSignalRepresentation
import ch.scorpion.antares.model.signal.Word
import ch.scorpion.jabbah.base.exception.IllegalArgumentException
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.execution.actor.ActorData
import ch.scorpion.jabbah.graph.model.GraphActorData
import ch.scorpion.jabbah.graph.model.InputPort
import ch.scorpion.jabbah.graph.model.net.CombinedNet
import ch.scorpion.jabbah.graph.model.net.NetCombiner
import ch.scorpion.jabbah.graph.model.net.SignalConverter
import ch.scorpion.jabbah.graph.model.net.SignalPropagationChain
import ch.scorpion.jabbah.graph.model.port.PortImpl
import ch.scorpion.jabbah.graph.model.vertice.CalculatingVertice
import ch.scorpion.jabbah.graph.model.vertice.VerticeCalculator
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

/**
 * Common base class for [Splitter] and [Concentrator].
 *
 * An [AbstractSplitter] has two sides: The "wide side" has one [DigitalPort] with a wide [BitWidth],
 * and the "narrow side" has multiple [DigitalPort]s each with a narrower [BitWidth] that depends on the
 * value of [branchCount]. For example, an [AbstractSplitter] with a wide side [BitWidth] of 8 and
 * a [branchCount] of 2 has 2 narrow side [DigitalPort]s each with 4 bits.
 */
abstract class AbstractSplitter(
	bitWidth: BitWidth,
	branchCount: BranchCount,
	calculator: VerticeCalculator<AbstractSplitter>
) : CalculatingVertice(calculator), NetCombiner {

	companion object {

		private val SIGNAL_SPLITTING_CACHE = mutableMapOf<SignalSplitterKey, SignalSplitter>()

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

	var bitWidth: BitWidth = bitWidth
		set(value) {
			if (field != value) {
				// calling setSplitting() would cause infinite recursion
				field = value
				branchCount =  BranchCount.forBitWidth(value).first()
				updatePorts()
			}
		}

	var branchCount: BranchCount = branchCount
		set(value) {
			if (field != value) {
				// calling setSplitting() would cause infinite recursion
				if (isSplittingSupported(bitWidth, value)) {
					field = value
					updatePorts()
				}
			}
		}

	var signalRepresentation = DigitalSignalRepresentation.BINARY
		set(value) {
			if (field != value) {
				field = value
				getOutputs().map { it as DigitalPort }.forEach { it.signalRepresentation = field }
			}
		}

	val supportedBranchCounts: List<BranchCount> get() = BranchCount.forBitWidth(bitWidth)

	val narrowSideBitWidth: BitWidth get() = BitWidth.of(bitWidth.width / branchCount.count)

	init {
		propagationDelay = 0
		if (isSplittingSupported(bitWidth, branchCount)) {
			setSplitting(bitWidth, branchCount)
		} else {
			throw IllegalArgumentException("Splitting with bitWidth $bitWidth and branchCount $branchCount not supported")
		}
	}

	/** ---- [Storable] */

	override fun write(writer: StoreWriter) {
		super.write(writer)
		writer.writeInt("bitWidth", bitWidth.width)
		writer.writeInt("branchCount", branchCount.count)
		writer.writeString("representation", signalRepresentation.customName)
	}

	override fun read(reader: StoreReader) {
		super.read(reader)
		setSplitting(BitWidth.of(reader.readInt("bitWidth")), BranchCount.withCount(reader.readInt("branchCount")))
		if (reader.hasAttribute("representation")) {
			// Legacy file support: in new files, 'representation' is always there
			signalRepresentation = DigitalSignalRepresentation.withName(reader.readString("representation"))
		}
	}

	override fun executionStarted(signalHandler: SignalHandler) {
		super.executionStarted(signalHandler)
		getOutputs().forEach { it.flush(signalHandler) }
	}

	/** ---- [NetCombiner] interface */

	override fun <T : Any> getSignalPropagationChains(inputPort: InputPort<T>, signalHandler: SignalHandler): Collection<SignalPropagationChain<T>> {
		return if (inputPort === wideSidePort) {
			getNarrowSignalPropagationChains(signalHandler)
		} else {
			getWideSignalPropagationChain(inputPort as InputPort<DigitalSignal>, signalHandler)
		} as Collection<SignalPropagationChain<T>>
	}

	private fun getWideSignalPropagationChain(inputPort: InputPort<DigitalSignal>, signalHandler: SignalHandler): Collection<SignalPropagationChain<DigitalSignal>> {
		val chains = CombinedNet.fromOutputPort(wideSidePort, signalHandler).chains
		val converter = SignalCombiner(inputPort.portId)
		chains.forEach { it.extendHead(converter, inputPort, wideSidePort) }
		return chains
	}

	private fun getNarrowSignalPropagationChains(signalHandler: SignalHandler): Collection<SignalPropagationChain<DigitalSignal>> {
		val result = mutableListOf<SignalPropagationChain<DigitalSignal>>()
		for (portId in 2..portsCount) {
			val port = getOutput<DigitalSignal>(portId)
			val chains = CombinedNet.fromOutputPort(port, signalHandler).chains
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
			"SignalCombiner for port $signalPortId in ${this@AbstractSplitter.id}"
	}

	/** ---- [CalculatingVertice] */

	override fun flush(signalHandler: SignalHandler, data: ActorData) {
		if ((data as GraphActorData).changedPort === wideSidePort) {
			flushNarrowSide(signalHandler)
		} else {
			flushWideSide(signalHandler)
		}
	}

	private fun flushNarrowSide(signalHandler: SignalHandler) {
		narrowSidePorts.forEach {
			it.flush(signalHandler)
			(it as PortImpl<DigitalSignal>).syncIncomingSignalWithNegotiatedOutgoingSignal()
		}
	}

	private fun flushWideSide(signalHandler: SignalHandler) {
		wideSidePort.flush(signalHandler)
		(wideSidePort as PortImpl<DigitalSignal>).syncIncomingSignalWithNegotiatedOutgoingSignal()
	}

	/** ---- [AbstractSplitter] */

	protected abstract fun createWideSidePort(): DigitalPort

	protected abstract fun createNarrowSidePort(index: Int): DigitalPort

	abstract val wideSidePort: DigitalPort

	abstract val narrowSidePorts: List<DigitalPort>

	fun split(signal: DigitalSignal, signalHandler: SignalHandler) {
		for (portId in 2..portsCount) {
			val outputPort = getPort<DigitalSignal>(portId) as DigitalPort
			val key = SignalSplitterKey(bitWidth, narrowSideBitWidth, portId - 2)
			val outSignal = getSignalSplitter(key).convert(signal)
			outputPort.setOutgoingSignalBuffered(outSignal, signalHandler)
		}
	}

	fun concentrate(signalHandler: SignalHandler) {
		val words = mutableListOf<Word>()
		for (portId in 2..portsCount) {
			val signal = (getPort<DigitalPort>(portId) as DigitalPort).getIncomingSignal() as Word
			words.add(signal)
		}
		val output = Word.of(words)
		(getPort<DigitalSignal>(1) as DigitalPort).setOutgoingSignalBuffered(output, signalHandler)
	}


	private fun isSplittingSupported(bitWidth: BitWidth, branchCount: BranchCount): Boolean {
		if (branchCount < BranchCount.BC_2 || branchCount.count > bitWidth.width) {
			return false
		}
		if (!BranchCount.forBitWidth(bitWidth).contains(branchCount)) {
			return false
		}
		return true
	}

	private fun setSplitting(bitWidth: BitWidth, branchCount: BranchCount) {
		if (!isSplittingSupported(bitWidth, branchCount)) {
			return
		}

		this.bitWidth = bitWidth
		this.branchCount = branchCount

		updatePorts()
	}

	private fun updatePorts() {
		clearPorts()
		addPort(createWideSidePort())

		for (index in 0 until bitWidth.width step narrowSideBitWidth.width) {
			addPort(createNarrowSidePort(index))
		}
	}
}