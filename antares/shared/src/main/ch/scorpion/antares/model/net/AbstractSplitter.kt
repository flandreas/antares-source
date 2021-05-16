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
import ch.scorpion.jabbah.graph.model.OutputPort
import ch.scorpion.jabbah.graph.model.net.CombinedNet
import ch.scorpion.jabbah.graph.model.net.NetCombiner
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

	override fun requiresCombinedNets(signalHandler: SignalHandler): Boolean = false

	override fun <T : Any> createCombinedNetsFor(outputPort: OutputPort<T>, inputPort: InputPort<T>, signalHandler: SignalHandler): Collection<CombinedNet<T>> {
		return if (inputPort === wideSidePort) {
			createNarrowCombinedNets(outputPort as OutputPort<DigitalSignal>, signalHandler) as Collection<CombinedNet<T>>
		} else {
			createWideCombinedNets(outputPort as OutputPort<DigitalSignal>, inputPort as InputPort<DigitalSignal>, signalHandler) as Collection<CombinedNet<T>>
		}
	}

	private fun createNarrowCombinedNets(originOutputPort: OutputPort<DigitalSignal>, signalHandler: SignalHandler): Collection<CombinedNet<DigitalSignal>> {
		val result = mutableListOf<CombinedNet<DigitalSignal>>()

		for (portId in 2..portsCount) {
			val port = getOutput<DigitalSignal>(portId)
			val combinedNetsForPort = CombinedNet.createFor(port, signalHandler)

			val splitIndex = portId - 2

			combinedNetsForPort.forEach { combinedNet ->
				combinedNet.accessOf(port)?.let {
					val thisAccess = it as DigitalCombinedNetAccess
					val baseBitIndex = splitIndex * narrowSideBitWidth.width + thisAccess.index * thisAccess.width.width
					combinedNet.replaceAccess(
						port,
						DigitalCombinedNetAccess(originOutputPort, it.width, baseBitIndex / it.width.width)
					)
				}
			}

			result.addAll(combinedNetsForPort)
		}
		return result
	}

	private fun createWideCombinedNets(originOutputPort: OutputPort<DigitalSignal>, inputPort: InputPort<DigitalSignal>, signalHandler: SignalHandler): Collection<CombinedNet<DigitalSignal>> {
		val result = mutableListOf<CombinedNet<DigitalSignal>>()
		val combinedNetsForPort = CombinedNet.createFor(wideSidePort, signalHandler)

		val splitWidth = narrowSideBitWidth
		val splitIndex = inputPort.portId - 2

		val narrowSideAccess = DigitalCombinedNetAccess(originOutputPort, splitWidth, splitIndex)

		combinedNetsForPort.forEach { combinedNet ->
			val wideSideAccess = combinedNet.accessOf(wideSidePort) as DigitalCombinedNetAccess?

			if (wideSideAccess != null) {
				if (narrowSideAccess.contains(wideSideAccess)) {
					val index = wideSideAccess.index * wideSideAccess.width.width - narrowSideAccess.index * narrowSideAccess.width.width
					combinedNet.replaceAccess(
						wideSidePort,
						DigitalCombinedNetAccess(originOutputPort, wideSideAccess.width, index))
					result.add(combinedNet)
				} else if (wideSideAccess.contains(narrowSideAccess)) {
					combinedNet.accesses
						.filter { it.port !== wideSidePort }
						.forEach { otherAccess ->

							// Split otherAccess and reduce its BitWidth to that of narrowSideAccess
							otherAccess as DigitalCombinedNetAccess
							val factor = otherAccess.width.width / narrowSideBitWidth.width

							val narrowInWideIndex = narrowSideAccess.index - wideSideAccess.index * factor
							val index = otherAccess.index * factor + narrowInWideIndex

							combinedNet.replaceAccess(
								otherAccess.port,
								DigitalCombinedNetAccess(otherAccess.port, narrowSideAccess.width, index)
							)

							combinedNet.replaceAccess(
								wideSidePort,
								DigitalCombinedNetAccess(originOutputPort, splitWidth, 0)
							)
						}
					result.add(combinedNet)
				}
			}
		}

		return result
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
			outputPort.setOutgoingSignalBuffered(signal.getSubword(narrowSideBitWidth, portId - 2), signalHandler)
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