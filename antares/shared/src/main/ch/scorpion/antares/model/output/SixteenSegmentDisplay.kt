package ch.scorpion.antares.model.output

import ch.scorpion.antares.model.port.DigitalPortImpl
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.edit.model.text.Translation
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.GraphActorData
import ch.scorpion.jabbah.graph.model.PortType
import ch.scorpion.jabbah.graph.model.vertice.VerticeCalculator

class SixteenSegmentDisplay : AbstractSegmentDisplay<SixteenSegmentDisplay>(CALCULATOR) {

	companion object {

		private const val BASE_RESOURCE_KEY = "library.element.SixteenSegmentDisplay"

		const val SEG_INPUT_NAME = "s"
		const val DP_INPUT_NAME = "p"

		private val PORT_TO_BIT = mapOf(
			"a1" to 0,
			"a2" to 1,
			"b" to 2,
			"c" to 3,
			"d1" to 4,
			"d2" to 5,
			"e" to 6,
			"f" to 7,
			"g1" to 8,
			"g2" to 9,
			"h" to 10,
			"i" to 11,
			"j" to 12,
			"k" to 13,
			"l" to 14,
			"m" to 15
		)

		private val CALCULATOR = Calculator()

		private class Calculator : VerticeCalculator<SixteenSegmentDisplay> {
			override fun calculate(vertice: SixteenSegmentDisplay, data: GraphActorData, signalHandler: SignalHandler) {
				vertice.stateChanged(signalHandler)
			}
		}
	}

	override val type: String by lazy { Translations.getString("$BASE_RESOURCE_KEY.name") }
	override val typeDesc: String? by lazy { Translations.getOptionalString("$BASE_RESOURCE_KEY.desc") }

	init {
		addPort(DigitalPortImpl(portType = PortType.INPUT, name = SEG_INPUT_NAME, bitWidth = BitWidth.BW_16, description = TranslatableText(Translation.ofStaticKey("antares.sixteenSegment.segmentsPort.desc"))))
		addPort(DigitalPortImpl(portType = PortType.INPUT, name = DP_INPUT_NAME, description =TranslatableText(Translation.ofStaticKey("antares.sixteenSegment.pointPort.desc"))))
	}

	override fun inputValueOf(bitName: String): Boolean =
		when (bitName) {
			DP_INPUT_NAME -> {
				getInput<DigitalSignal>(bitName).getIncomingSignal()!!.bitAt(0).isSet
			}
			else -> {
				val bitNr = PORT_TO_BIT[bitName] ?: throw IllegalArgumentException("unknown bitName $bitName")
				getInput<DigitalSignal>(SEG_INPUT_NAME).getIncomingSignal()!!.bitAt(bitNr).isSet
			}
		}
}