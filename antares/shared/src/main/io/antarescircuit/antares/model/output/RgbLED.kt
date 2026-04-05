package io.antarescircuit.antares.model.output

import io.antarescircuit.antares.model.port.DigitalPortImpl
import io.antarescircuit.antares.model.signal.BitWidth
import io.antarescircuit.antares.model.signal.DigitalSignal
import io.antarescircuit.antares.view.style.AntaresTheme
import io.antarescircuit.jabbah.base.LongValueImpl
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.draw.graphics.Color
import io.antarescircuit.jabbah.draw.style.Themes
import io.antarescircuit.jabbah.edit.model.text.TranslatableText
import io.antarescircuit.jabbah.edit.model.text.Translation
import io.antarescircuit.jabbah.execution.SignalHandler
import io.antarescircuit.jabbah.execution.actor.Actor
import io.antarescircuit.jabbah.graph.model.GraphActorData
import io.antarescircuit.jabbah.graph.model.PortType
import io.antarescircuit.jabbah.graph.model.Vertice
import io.antarescircuit.jabbah.graph.model.vertice.CalculatingVertice
import io.antarescircuit.jabbah.graph.model.vertice.VerticeCalculator

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
		propagationDelay = LongValueImpl.ZERO
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
			value.getSubwordValue(BitWidth.BW_8, 2)?.toInt() ?: 0,
			value.getSubwordValue(BitWidth.BW_8, 1)?.toInt() ?: 0,
			value.getSubwordValue(BitWidth.BW_8, 0)?.toInt() ?: 0
		)
	}
}