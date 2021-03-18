package ch.scorpion.antares.model.net

import ch.scorpion.antares.model.Logic
import ch.scorpion.antares.model.port.DigitalPort
import ch.scorpion.antares.model.port.DigitalPortImpl
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.Word
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.GraphActorData
import ch.scorpion.jabbah.graph.model.Net
import ch.scorpion.jabbah.graph.model.vertice.VerticeCalculator

/**
 * Splits a multi-bit [Net] into multiple [Net]s with smaller [BitWidth].
 */
class Splitter(
	bitWidth: BitWidth = BitWidth.BW_8,
	branchCount: BranchCount = BranchCount.BC_4
) : AbstractSplitter(bitWidth, branchCount, CALCULATOR) {

	companion object {

		private const val BASE_RESOURCE_KEY = "library.element.Splitter"
		private val TYPE = Translations.getString("$BASE_RESOURCE_KEY.name")
		private val TYPE_DESC = Translations.getOptionalString("$BASE_RESOURCE_KEY.desc")

		private val CALCULATOR = Calculator()

		private class Calculator : VerticeCalculator<AbstractSplitter> {
			override fun calculate(vertice: AbstractSplitter, data: GraphActorData, signalHandler: SignalHandler) {
				val signal = data.getSignal<DigitalSignal>(1)
				for ((index, output) in vertice.getOutputs().withIndex()) {
					val digitalPort = output as DigitalPort
					if (signal == null) {
						digitalPort.setOutgoingSignalBuffered(Word.undefined(vertice.narrowSideBitWidth), signalHandler)
					} else {
						digitalPort.setOutgoingSignalBuffered(signal.getSubword(vertice.narrowSideBitWidth, index), signalHandler)
					}
				}
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

	override val narrowSidePorts: List<DigitalPort> get() = getOutputs().map { it as DigitalPort }
}