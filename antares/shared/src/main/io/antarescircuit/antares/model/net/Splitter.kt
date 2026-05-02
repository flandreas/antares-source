package io.antarescircuit.antares.model.net

import io.antarescircuit.antares.model.Logic
import io.antarescircuit.antares.model.port.DigitalPort
import io.antarescircuit.antares.model.port.DigitalPortImpl
import io.antarescircuit.antares.model.signal.BitWidth
import io.antarescircuit.antares.model.signal.DigitalSignal
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.execution.SignalHandler
import io.antarescircuit.jabbah.graph.model.GraphActorData
import io.antarescircuit.jabbah.graph.model.Net
import io.antarescircuit.jabbah.graph.model.vertice.VerticeCalculator

/**
 * Splits a multi-bit [Net] into multiple [Net]s with smaller [BitWidth].
 */
class Splitter(
	bitWidth: BitWidth = BitWidth.BW_8,
	branchCount: BranchCount = BranchCount.BC_4
) : AbstractBranchCountSplitter(bitWidth, branchCount, CALCULATOR) {

	companion object {

		private const val BASE_RESOURCE_KEY = "library.element.Splitter"
		private val TYPE get() = Translations.getString("$BASE_RESOURCE_KEY.name")
		private val TYPE_DESC get() = Translations.getOptionalString("$BASE_RESOURCE_KEY.desc")

		private val CALCULATOR = Calculator()

		private class Calculator : VerticeCalculator<AbstractSplitter> {
			override fun calculate(vertice: AbstractSplitter, data: GraphActorData, signalHandler: SignalHandler) {
				val signal = data.getSignal<DigitalSignal>(1)
				vertice.split(signal!!, signalHandler)
			}
		}
	}

	override val type: String get() = TYPE

	override val typeDesc: String? get() = TYPE_DESC

	/** ---- [AbstractSplitter] */

	override fun createWideSidePort(): DigitalPort =
		DigitalPortImpl.createInput(Logic.POSITIVE, null, this.bitWidth)

	override fun createNarrowSidePort(index: Int): DigitalPort =
		DigitalPortImpl.createOutput(Logic.POSITIVE, index.toString(), narrowSideBitWidth, signalRepresentation)

	override val wideSidePort: DigitalPort get() = getInput<DigitalPort>() as DigitalPort

	// Tuning: Instead of getOutputs().map { it as DigitalPort }
	override val narrowSidePorts: List<DigitalPort> get() = getTypedOutputs()
}