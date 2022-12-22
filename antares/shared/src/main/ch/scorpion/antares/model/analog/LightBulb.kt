package ch.scorpion.antares.model.analog

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.GraphActorData
import ch.scorpion.jabbah.graph.model.vertice.CalculatingVertice
import ch.scorpion.jabbah.graph.model.vertice.VerticeCalculator

class LightBulb : CalculatingVertice(CALCULATOR) {

	companion object {
		private const val BASE_RESOURCE_KEY = "library.element.LightBulb"

		private val CALCULATOR = Calculator()

		private class Calculator : VerticeCalculator<LightBulb> {
			override fun calculate(vertice: LightBulb, data: GraphActorData, signalHandler: SignalHandler) {
				// TODO
			}
		}
	}

	override val type: String get() = Translations.getString("$BASE_RESOURCE_KEY.name")

	override val typeDesc: String? get() = Translations.getOptionalString("$BASE_RESOURCE_KEY.desc")

	init {
		addPort(AnalogPort())
		addPort(AnalogPort())
		propagationDelay = 0
	}
}