package io.antarescircuit.antares.model.analog

import io.antarescircuit.antares.view.analog.engine.AnalogElement
import io.antarescircuit.antares.view.analog.engine.AnalogElementMixin
import io.antarescircuit.antares.view.analog.engine.AnalogCircuitAnalysis
import io.antarescircuit.jabbah.base.LongValueImpl
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.execution.SignalHandler
import io.antarescircuit.jabbah.graph.model.GraphInput
import io.antarescircuit.jabbah.graph.model.vertice.AbstractVertice
import io.antarescircuit.jabbah.graph.model.vertice.CalculatingVertice
import io.antarescircuit.jabbah.graph.model.vertice.VerticeCalculator

abstract class AbstractAnalogVertice<T: CalculatingVertice>(
	calculator: VerticeCalculator<T>,
	val baseResourceKey: String,
	protected val analogElem: AnalogElementMixin = AnalogElementMixin()
) : CalculatingVertice(calculator), AnalogVertice, AnalogElement by analogElem {

	companion object {

		/** The 'reason' in [stateChanged] calls if the main property value has changed.*/
		const val MAIN_PROPERTY_STATE = "mainPropertyState"

		/**
		 * The 'reason' in [stateChanged] calls if an [AnalogVertice] request recalculation of the entire [AnalogGraph].
		 * Note that this explicitly skips analysis of the [AnalogGraph]. Used e.g. if active components like
		 * [Capacitor] have changed their voltage.
		 */
		const val REQUEST_RECALCULATE = "requestRecalculation"

		/**
		 * The 'reason' in [stateChanged] calls if an [AnalogVertice] request reanalyzing of the entire [AnalogGraph].
		 * Used if the structure of the [AnalogGraph] has changed during simulation, such as when opening a switch,
		 * or a signal at a [GraphInput] has arrived (which is treated as a new voltage source).
		 */
		const val REQUEST_REANALYZE = "requestReanalysis"
	}

	override val type: String get() = Translations.getString("${baseResourceKey}.name")

	override val typeDesc: String? get() = Translations.getOptionalString("${baseResourceKey}.desc")

	init {
		analogElem.bindAnalogElement(this)
		propagationDelay = LongValueImpl.ZERO
	}

	/** ---- [AnalogElement] */

	override val voltageSourceCount: Int get() = 0

	override fun calculateCurrent() {
		// empty
	}

	override fun stamp(analysis: AnalogCircuitAnalysis) {
		// empty
	}

	/** ---- [AbstractVertice] */

	override fun executionInitialize(signalHandler: SignalHandler) {
		super.executionInitialize(signalHandler)
		reset()
	}

	/** ---- [AbstractAnalogVertice] */

	override val storePropagationDelay: Boolean get() = false

	fun requestAnalogGraphRecalculation(signalHandler: SignalHandler) {
		stateChanged(signalHandler, REQUEST_RECALCULATE)
	}

	fun requestAnalogReanalization(signalHandler: SignalHandler) {
		stateChanged(signalHandler, REQUEST_REANALYZE)
	}
}