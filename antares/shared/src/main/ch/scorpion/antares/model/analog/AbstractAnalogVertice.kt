package ch.scorpion.antares.model.analog

import ch.scorpion.antares.view.analog.engine.AnalogElement
import ch.scorpion.antares.view.analog.engine.AnalogElementMixin
import ch.scorpion.antares.view.analog.engine.AnalogCircuitAnalysis
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.graph.model.vertice.CalculatingVertice
import ch.scorpion.jabbah.graph.model.vertice.VerticeCalculator

abstract class AbstractAnalogVertice<T: CalculatingVertice>(
	calculator: VerticeCalculator<T>,
	val baseResourceKey: String,
	protected val analogElem: AnalogElementMixin = AnalogElementMixin()
) : CalculatingVertice(calculator), AnalogVertice, AnalogElement by analogElem {

	companion object {
		const val MAIN_PROPERTY_STATE = "mainPropertyState"
	}

	override val type: String get() = Translations.getString("${baseResourceKey}.name")

	override val typeDesc: String? get() = Translations.getOptionalString("${baseResourceKey}.desc")

	init {
		analogElem.bindAnalogElement(this)
		propagationDelay = 0
	}

	override val voltageSourceCount: Int get() = 0

	override fun calculateCurrent() {
		// empty
	}

	override fun stamp(analysis: AnalogCircuitAnalysis) {
		// empty
	}
}