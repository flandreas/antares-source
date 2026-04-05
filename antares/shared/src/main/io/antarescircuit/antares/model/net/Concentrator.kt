package io.antarescircuit.antares.model.net

import io.antarescircuit.antares.model.Logic
import io.antarescircuit.antares.model.port.DigitalPort
import io.antarescircuit.antares.model.port.DigitalPortImpl
import io.antarescircuit.antares.model.signal.BitWidth
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.execution.SignalHandler
import io.antarescircuit.jabbah.graph.model.GraphActorData
import io.antarescircuit.jabbah.graph.model.Net
import io.antarescircuit.jabbah.graph.model.vertice.VerticeCalculator

/**
 * Concentrates multiple [Net]s into one [Net] with a larger [BitWidth].
 */
class Concentrator(
	bitWidth: BitWidth = BitWidth.BW_8,
	branchCount: BranchCount = BranchCount.BC_4
) : AbstractBranchCountSplitter(bitWidth, branchCount, CALCULATOR) {

	companion object {

		private const val BASE_RESOURCE_KEY = "library.element.Concentrator"
		private val TYPE get() = Translations.getString("$BASE_RESOURCE_KEY.name")
		private val TYPE_DESC get() = Translations.getOptionalString("$BASE_RESOURCE_KEY.desc")

		private val CALCULATOR = Calculator()

		private class Calculator : VerticeCalculator<AbstractSplitter> {
			override fun calculate(vertice: AbstractSplitter, data: GraphActorData, signalHandler: SignalHandler) {
				vertice.concentrate(signalHandler)
			}
		}
	}

	override val type: String get() = TYPE

	override val typeDesc: String? get() = TYPE_DESC

	/** ---- [AbstractSplitter] */

	override fun createWideSidePort(): DigitalPort =
		DigitalPortImpl.createOutput(Logic.POSITIVE, null, bitWidth, signalRepresentation)

	override fun createNarrowSidePort(index: Int): DigitalPort =
		DigitalPortImpl.createInput(Logic.POSITIVE, index.toString(), narrowSideBitWidth)

	override val wideSidePort: DigitalPort get() = getOutput<DigitalPort>() as DigitalPort

	override val narrowSidePorts: List<DigitalPort> get() = getInputs().map { it as DigitalPort }
}