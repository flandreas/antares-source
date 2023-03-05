package ch.scorpion.antares.model.analog

import ch.scorpion.antares.view.analog.AnalogGraphView
import ch.scorpion.antares.view.module.AntaresViewModule
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.execution.actor.Actor
import ch.scorpion.jabbah.graph.model.GraphActorData
import ch.scorpion.jabbah.graph.model.vertice.VerticeCalculator
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

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
				if (data.graphView is AnalogGraphView) {
					AntaresViewModule.analogCircuitCalculator.calculate((data.graphView as AnalogGraphView).ensureAnalysis(), signalHandler)
				}
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
	private var resistanceBuffer: Double = 0.0

	/** ---- [Storable] interface */

	override fun read(reader: StoreReader) {
		super.read(reader)
		resistance = reader.readDouble("resistance")
		variable = reader.readBoolean("variable")
	}

	override fun write(writer: StoreWriter) {
		super.write(writer)
		writer.writeDouble("resistance", resistance)
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

	fun setState(resistance: Double, signalHandler: SignalHandler, graphView: GraphView) {
		this.resistance = resistance
		requestActingAfter(signalHandler, propagationDelay, createActorData(null, graphView = graphView))
	}
}