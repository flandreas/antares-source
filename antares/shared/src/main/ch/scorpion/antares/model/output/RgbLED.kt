package ch.scorpion.antares.model.output

import ch.scorpion.antares.model.Logic
import ch.scorpion.antares.model.port.DigitalPortImpl
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.Word
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.execution.actor.Actor
import ch.scorpion.jabbah.graph.model.GraphActorData
import ch.scorpion.jabbah.graph.model.Vertice
import ch.scorpion.jabbah.graph.model.vertice.CalculatingVertice
import ch.scorpion.jabbah.graph.model.vertice.VerticeCalculator

/**
 * A light emitting [Vertice] whose RGB value is determined by the 24-bit input, where each of the three 8-bit values
 * represents the corresponding color value.
 .*/
class RgbLED() : CalculatingVertice("library.element.RgbLED", CALCULATOR) {

	companion object {
		private val DEFAULT_COLOR = Color(60, 0, 0)
		private val CALCULATOR = object : VerticeCalculator<RgbLED> {
			override fun calculate(vertice: RgbLED, data: GraphActorData, signalHandler: SignalHandler) {
				vertice.updateColor(data.getSignal<DigitalSignal>(1) as Word)
			}
		}
	}

	var color: Color = Color.BLACK
		private set(value) {
			if (field != value) {
				field = value
				stateChanged()
			}
		}

	init {
		addPort(DigitalPortImpl.createInput(Logic.POSITIVE, null, BitWidth.BW_24))
		propagationDelay = 0
	}

	/** ---- [Actor] */

	override fun executionStarted(signalHandler: SignalHandler) {
		super.executionStarted(signalHandler)
		color = DEFAULT_COLOR
	}

	override fun executionStopped(signalHandler: SignalHandler) {
		super.executionStopped(signalHandler)
		color = DEFAULT_COLOR
	}

	/** ---- [RgbLED] */

	/** Updates [color] according to the specified value.*/
	private fun updateColor(value: Word) {
		color = Color(
			value.getSubwordValue(BitWidth.BW_8, 0)!!.toInt(),
			value.getSubwordValue(BitWidth.BW_8, 1)!!.toInt(),
			value.getSubwordValue(BitWidth.BW_8, 2)!!.toInt()
		)
	}
}