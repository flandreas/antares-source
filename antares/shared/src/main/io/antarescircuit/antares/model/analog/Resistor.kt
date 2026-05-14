package io.antarescircuit.antares.model.analog

import io.antarescircuit.antares.view.analog.AnalogGraphView
import io.antarescircuit.jabbah.edit.properties.magnitude.MagnitudeValue
import io.antarescircuit.jabbah.execution.SignalHandler
import io.antarescircuit.jabbah.execution.actor.Actor
import io.antarescircuit.jabbah.graph.model.GraphActorData
import io.antarescircuit.jabbah.graph.model.vertice.VerticeCalculator
import io.antarescircuit.jabbah.io.Storable
import io.antarescircuit.jabbah.io.StoreReader
import io.antarescircuit.jabbah.io.StoreWriter

class Resistor(
	resistance: Double = DEF_RESISTANCE
) : AbstractResistingAnalogVertice<Resistor>(resistance, CALCULATOR, "library.element.Resistor") {

	companion object {
		private const val DEF_RESISTANCE = 100.0
		private const val VARIABLE_STATE = "variable"

		private val CALCULATOR = Calculator()

		private class Calculator : VerticeCalculator<Resistor> {
			override fun calculate(vertice: Resistor, data: GraphActorData, signalHandler: SignalHandler) {
				// React to changes of variable resistance
				vertice.requestAnalogGraphRecalculation(signalHandler)
			}
		}
	}

	var variable: Boolean = false
		set(value) {
			if (field != value) {
				field = value
				stateChanged(reason = VARIABLE_STATE)
			}
		}

	/** Used for restoring [resistance] after the simulation has ended. */
	private lateinit var resistanceBuffer: MagnitudeValue

	/** ---- [Storable] interface */

	override fun read(reader: StoreReader) {
		super.read(reader)
		variable = reader.readBoolean("variable")
	}

	override fun write(writer: StoreWriter) {
		super.write(writer)
		writer.writeBoolean("variable", variable)
	}

	/** ---- [Actor] interface */

	override fun executionInitialize(signalHandler: SignalHandler) {
		super.executionInitialize(signalHandler)
		resistanceBuffer = resistance
	}

	override fun executionStopped(signalHandler: SignalHandler) {
		super.executionStopped(signalHandler)
		resistance = resistanceBuffer
	}

	/** ---- [Resistor] */

	fun setState(resistance: MagnitudeValue, signalHandler: SignalHandler, graphView: AnalogGraphView) {
		this.resistance = resistance
		graphView.requireAnalysis()
		requestActingAfter(signalHandler, propagationDelay.value, createActorData(null))
	}
}