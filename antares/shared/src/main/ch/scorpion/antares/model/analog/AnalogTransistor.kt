package ch.scorpion.antares.model.analog

import ch.scorpion.antares.model.net.TransistorIF
import ch.scorpion.antares.model.net.TransistorIF.Companion.DEFAULT_TRANSISTOR_TYPE
import ch.scorpion.antares.model.net.TransistorType
import ch.scorpion.antares.view.analog.AnalogCircuitBranch
import ch.scorpion.antares.view.analog.AnalogGraphView
import ch.scorpion.antares.view.analog.DynamicLinearEquationSystem
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.GraphActorData
import ch.scorpion.jabbah.graph.model.vertice.VerticeCalculator
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

class AnalogTransistor(
	transistorType: TransistorType = DEFAULT_TRANSISTOR_TYPE,
) : AbstractAnalogVertice<AnalogTransistor>(
	CALCULATOR,
	"library.element.AnalogTransistor"
), TransistorIF<AnalogSignal> {

	companion object {
		private val CALCULATOR = Calculator()

		private class Calculator : VerticeCalculator<AnalogTransistor> {
			override fun calculate(vertice: AnalogTransistor, data: GraphActorData, signalHandler: SignalHandler) {
				// TODO
			}
		}
	}

	override val type: String get() = super<TransistorIF>.type

	override val typeDesc: String? get() = super<TransistorIF>.typeDesc

	override var transistorType: TransistorType = transistorType
		set(value) {
			if (field != value) {
				field = value
				stateChanged()
			}
		}

	override val isOn: Boolean get() = false

	init {
		addPort(AnalogPort("S"))
		addPort(AnalogPort("G"))
		addPort(AnalogPort("D"))
	}

	/** ---- [Storable] interface */

	override fun write(writer: StoreWriter) {
		super<TransistorIF>.write(writer)
	}

	override fun read(reader: StoreReader) {
		super<TransistorIF>.read(reader)
	}

	override fun composeComponentConstituentEquation(
		circuitView: AnalogGraphView,
		voltageNodes: List<Int>,
		branches: List<AnalogCircuitBranch>,
		groundNodeNetId: Int,
		equationSystem: DynamicLinearEquationSystem
	) {
		// TODO
	}
}