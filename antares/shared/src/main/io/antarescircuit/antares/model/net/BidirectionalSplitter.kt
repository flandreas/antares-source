package io.antarescircuit.antares.model.net

import io.antarescircuit.antares.model.Logic
import io.antarescircuit.antares.model.port.DigitalPort
import io.antarescircuit.antares.model.port.DigitalPortImpl
import io.antarescircuit.antares.model.signal.BitWidth
import io.antarescircuit.antares.model.signal.DigitalSignal
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.execution.SignalHandler
import io.antarescircuit.jabbah.graph.model.Port
import io.antarescircuit.jabbah.graph.model.GraphActorData
import io.antarescircuit.jabbah.graph.model.PortType
import io.antarescircuit.jabbah.graph.model.vertice.VerticeCalculator

/**
 * An [AbstractSplitter] implementation whose [Port]s all all of type [PortType.INOUT], for which it can
 * act as a [Splitter] and a [Concentrator] at the same time.
 */
class BidirectionalSplitter(
	bitWidth: BitWidth = BitWidth.BW_8,
	branchCount: BranchCount = BranchCount.BC_4
) : AbstractBranchCountSplitter(bitWidth, branchCount, CALCULATOR) {

	companion object {

		private const val BASE_RESOURCE_KEY = "library.element.BidirectionalSplitter"
		private val TYPE get() = Translations.getString("$BASE_RESOURCE_KEY.name")
		private val TYPE_DESC get() = Translations.getOptionalString("$BASE_RESOURCE_KEY.desc")

		private val CALCULATOR = Calculator()

		private class Calculator : VerticeCalculator<AbstractSplitter> {
			override fun calculate(vertice: AbstractSplitter, data: GraphActorData, signalHandler: SignalHandler) {
				val changedPortId = data.changedPort!!.portId
				if (changedPortId == 1) {
					vertice.split(data.getSignal(1)!!, signalHandler)
				} else {
					vertice.concentrate(signalHandler)
				}
			}
		}
	}

	override val type: String get() = TYPE
	override val typeDesc: String? get() = TYPE_DESC

	override fun createWideSidePort(): DigitalPort =
		DigitalPortImpl(PortType.INOUT, null, Logic.POSITIVE, bitWidth = bitWidth, canBeUndefined = true, signalRepresentation = signalRepresentation)

	override fun createNarrowSidePort(index: Int): DigitalPort =
		DigitalPortImpl(PortType.INOUT, index.toString(), Logic.POSITIVE, bitWidth = narrowSideBitWidth, canBeUndefined = true, signalRepresentation = signalRepresentation)

	override val wideSidePort: DigitalPort get() = getPort<DigitalSignal>(1) as DigitalPort

	override val narrowSidePorts: List<DigitalPort> get() = getPorts()
		.filterIndexed { index, _ -> index > 0 }
		.map { it as DigitalPort }
}