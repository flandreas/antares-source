package ch.scorpion.antares.model.output

import ch.scorpion.antares.model.port.DigitalPortImpl
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.view.style.AntaresTheme
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.style.Themes
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.edit.model.text.Translation
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.execution.actor.Actor
import ch.scorpion.jabbah.graph.model.GraphActorData
import ch.scorpion.jabbah.graph.model.PortType
import ch.scorpion.jabbah.graph.model.Vertice
import ch.scorpion.jabbah.graph.model.vertice.CalculatingVertice
import ch.scorpion.jabbah.graph.model.vertice.VerticeCalculator

/**
 * A light emitting [Vertice] whose RGB value is determined by the 24-bit input, where each of the three 8-bit values
 * represents the corresponding color value.
.*/
class RgbLED : CalculatingVertice(CALCULATOR) {

	companion object {
		private const val BASE_RESOURCE_KEY = "library.element.RgbLED"
		private val TYPE get() = Translations.getString("$BASE_RESOURCE_KEY.name")
		private val TYPE_DESC get() = Translations.getOptionalString("$BASE_RESOURCE_KEY.desc")

		private val DEFAULT_COLOR = Themes.get<AntaresTheme>().screen.backgroundColor
		private val DATA_PORT_DESC get() = TranslatableText(Translation.ofStaticKey("antares.rgbLed.dataPort.desc"))

		private val CALCULATOR = Calculator()

		private class Calculator : VerticeCalculator<RgbLED> {
			override fun calculate(vertice: RgbLED, data: GraphActorData, signalHandler: SignalHandler) {
				vertice.updateColor(data.getSignal(1)!!)
			}
		}
	}

	override val type: String get() = TYPE
	override val typeDesc: String? get() = TYPE_DESC

	var color: Color = Color.BLACK
		private set(value) {
			if (field != value) {
				field = value
				stateChanged()
			}
		}

	init {
		addPort(DigitalPortImpl(portType = PortType.INPUT, name = null, bitWidth = BitWidth.BW_24, description = DATA_PORT_DESC))
		propagationDelay = 0
	}

	/** ---- [Actor] */

	override fun executionInitialize(signalHandler: SignalHandler) {
		super.executionInitialize(signalHandler)
		color = Color.BLACK
	}

	override fun executionStopped(signalHandler: SignalHandler) {
		super.executionStopped(signalHandler)
		color = DEFAULT_COLOR
	}

	/** ---- [RgbLED] */

	/** Updates [color] according to the specified value.*/
	private fun updateColor(value: DigitalSignal) {
		color = Color(
			value.getSubwordValue(BitWidth.BW_8, 2)!!.toInt(),
			value.getSubwordValue(BitWidth.BW_8, 1)!!.toInt(),
			value.getSubwordValue(BitWidth.BW_8, 0)!!.toInt()
		)
	}
}