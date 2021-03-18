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
 * Concentrates multiple [Net]s into one [Net] with a larger [BitWidth].
 */
class Concentrator(
	bitWidth: BitWidth = BitWidth.BW_8,
	branchCount: BranchCount = BranchCount.BC_4
) : AbstractSplitter(bitWidth, branchCount, CALCULATOR) {

	companion object {

		private const val BASE_RESOURCE_KEY = "library.element.Concentrator"
		private val TYPE = Translations.getString("$BASE_RESOURCE_KEY.name")
		private val TYPE_DESC = Translations.getOptionalString("$BASE_RESOURCE_KEY.desc")

		private val CALCULATOR = Calculator()

		private class Calculator : VerticeCalculator<AbstractSplitter> {
			override fun calculate(vertice: AbstractSplitter, data: GraphActorData, signalHandler: SignalHandler) {
				val words = mutableListOf<Word>()
				vertice.getInputs().forEach { words.add(data.getSignal(it.portId)!!) }
				vertice.getOutput<DigitalSignal>().setOutgoingSignalBuffered(Word.of(words), signalHandler)
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