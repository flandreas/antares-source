package io.antarescircuit.antares.model.net

import io.antarescircuit.antares.model.port.DigitalPort
import io.antarescircuit.antares.model.signal.*
import io.antarescircuit.jabbah.base.LongValueImpl
import io.antarescircuit.jabbah.execution.SignalHandler
import io.antarescircuit.jabbah.execution.actor.ActorData
import io.antarescircuit.jabbah.graph.model.*
import io.antarescircuit.jabbah.graph.model.net.CombinedNet
import io.antarescircuit.jabbah.graph.model.net.NetCombiner
import io.antarescircuit.jabbah.graph.model.port.PortImpl
import io.antarescircuit.jabbah.graph.model.vertice.CalculatingVertice
import io.antarescircuit.jabbah.graph.model.vertice.VerticeCalculator
import io.antarescircuit.jabbah.io.Storable
import io.antarescircuit.jabbah.io.StoreReader
import io.antarescircuit.jabbah.io.StoreWriter

/**
 * Common base class for [Splitter] and [Concentrator].
 *
 * An [AbstractSplitter] has two sides: The "wide side" has one [DigitalPort] with a wide [BitWidth],
 * and the "narrow side" has multiple [DigitalPort]s each with a narrower [BitWidth].
 */
abstract class AbstractSplitter(
	calculator: VerticeCalculator<AbstractSplitter>
) : CalculatingVertice(calculator), NetCombiner, DigitalSignalRepresenter {

	abstract var bitWidth: BitWidth

	override var signalRepresentation = DigitalSignalRepresentation.getSystemDefault()
		set(value) {
			if (field != value) {
				field = value
				getOutputs().map { it as DigitalPort }.forEach { it.signalRepresentation = field }
			}
		}

	abstract val narrowSideBitWidth: BitWidth

	init {
		propagationDelay = LongValueImpl.ZERO
	}

	/** ---- [GraphElement] */

	override fun graphParamsChanged(graph: Graph) {
		super.graphParamsChanged(graph)
		(bitWidth as? BitWidthExpression)?.let { it.evaluateIn(graph)?.let { bw -> bitWidth = bw } }
	}

	/** ---- [Storable] */

	override fun write(writer: StoreWriter) {
		super.write(writer)
		bitWidth.write("bitWidth", writer)
		writer.writeString("representation", signalRepresentation.customName)
	}

	override fun read(reader: StoreReader) {
		super.read(reader)
		if (reader.hasAttribute("representation")) {
			// Legacy file support: in new files, 'representation' is always there
			signalRepresentation = DigitalSignalRepresentation.withName(reader.readString("representation"))
		}
	}

	override fun executionStart(signalHandler: SignalHandler) {
		super.executionStart(signalHandler)
		getOutputs().forEach { it.flush(signalHandler, force = false) }
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
			flushNarrowSide(signalHandler, data.force)
		} else {
			flushWideSide(signalHandler, data.force)
		}
	}

	private fun flushNarrowSide(signalHandler: SignalHandler, force: Boolean) {
		narrowSidePorts.forEach {
			it.flush(signalHandler, force)
			(it as PortImpl<DigitalSignal>).syncIncomingSignalWithNegotiatedOutgoingSignal()
		}
	}

	private fun flushWideSide(signalHandler: SignalHandler, force: Boolean) {
		wideSidePort.flush(signalHandler, force)
		(wideSidePort as PortImpl<DigitalSignal>).syncIncomingSignalWithNegotiatedOutgoingSignal()
	}

	/** ---- [AbstractSplitter] */

	abstract val wideSidePort: DigitalPort

	abstract val narrowSidePorts: List<DigitalPort>

	abstract fun split(signal: DigitalSignal, signalHandler: SignalHandler)

	abstract fun concentrate(signalHandler: SignalHandler)
}