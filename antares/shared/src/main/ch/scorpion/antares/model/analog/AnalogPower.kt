package ch.scorpion.antares.model.analog

import ch.scorpion.antares.view.analog.engine.AnalogElement
import ch.scorpion.antares.view.analog.engine.AnalogElementMixin
import ch.scorpion.antares.view.analog.engine.AnalogCircuitAnalysis
import ch.scorpion.jabbah.graph.model.vertice.EmptyVerticeCalculator
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

class AnalogPower : AbstractAnalogVertice<AnalogPower>(
	EmptyVerticeCalculator,
	"library.element.AnalogPower",
	AnalogElementMixin(postCount = 1)
) {
	var voltage: Double = 5.0
		set(value) {
			if (value != field) {
				field = value
				stateChanged()
			}
		}

	init {
		addPort(AnalogPort())
	}

	/** ---- [Storable] */

	override fun write(writer: StoreWriter) {
		super.write(writer)
		writer.writeDouble("voltage", voltage)
	}

	override fun read(reader: StoreReader) {
		super.read(reader)
		voltage = reader.readDouble("voltage")
	}

	/** ---- [AnalogElement] */

	override val voltageSourceCount: Int get() = 1

	override fun stamp(analysis: AnalogCircuitAnalysis) {
		analysis.stampVoltageSource(0, analogElem.nodes[0], analogElem.voltageSource, voltage)
	}
}