package io.antarescircuit.antares.model.analog

import io.antarescircuit.antares.model.AntaresGraphTypes
import io.antarescircuit.antares.view.analog.engine.AnalogElement
import io.antarescircuit.antares.view.analog.AnalogGraphView
import io.antarescircuit.antares.view.analog.engine.AnalogCircuitAnalysis
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.edit.model.text.TranslatableText
import io.antarescircuit.jabbah.execution.SignalHandler
import io.antarescircuit.jabbah.graph.model.graph.GraphImpl
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.io.Storable
import io.antarescircuit.jabbah.io.StoreReader
import io.antarescircuit.jabbah.io.StoreWriter

class AnalogGraph(
	name: TranslatableText = TranslatableText(Translations.getString("graph.name.unknown")),
	eventBus: EventBus = BaseModule.eventBus
) : GraphImpl(name, AntaresGraphTypes.Analog, eventBus) {

	var timeStep: Double = BaseModule.properties.getFloat(AnalogCircuitAnalysis.PROP_TIME_STEP).toDouble()
		set(value) {
			require(value >= AnalogCircuitAnalysis.MIN_TIME_STEP && value <= AnalogCircuitAnalysis.MAX_TIME_STEP) {
				"Must be between ${AnalogCircuitAnalysis.MIN_TIME_STEP} and ${AnalogCircuitAnalysis.MAX_TIME_STEP}"
			}
			field = value
		}

	val isNonLinear: Boolean get() = elements.filterIsInstance<AnalogElement>().any { it.isNonLinear }

	override fun executionStart(signalHandler: SignalHandler, graphView: GraphView?) {
		super.executionStart(signalHandler, graphView)
		(graphView as AnalogGraphView?)?.requestActing(signalHandler, true)
	}

	override fun checkDesign(signalHandler: SignalHandler, eventBus: EventBus): Boolean = true

	/** ---- [Storable] interface */

	override fun read(reader: StoreReader) {
		super.read(reader)
		if (reader.hasAttribute("timeStep")) {
			timeStep = reader.readDouble("timeStep")
		}
	}

	override fun write(writer: StoreWriter) {
		super.write(writer)
		writer.writePreciseDouble("timeStep", timeStep)
	}
}