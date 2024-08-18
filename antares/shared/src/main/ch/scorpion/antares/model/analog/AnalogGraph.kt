package ch.scorpion.antares.model.analog

import ch.scorpion.antares.model.AntaresGraphTypes
import ch.scorpion.antares.view.analog.engine.AnalogElement
import ch.scorpion.antares.view.analog.AnalogGraphView
import ch.scorpion.antares.view.analog.engine.AnalogCircuitAnalysis
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.graph.GraphImpl
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

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
		writer.writeDouble("timeStep", timeStep)
	}
}