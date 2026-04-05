package io.antarescircuit.antares.model.analog

import io.antarescircuit.antares.view.analog.engine.AnalogElement
import io.antarescircuit.antares.view.analog.engine.AnalogElementMixin
import io.antarescircuit.antares.view.analog.engine.AnalogCircuitAnalysis
import io.antarescircuit.jabbah.graph.model.vertice.EmptyVerticeCalculator
import io.antarescircuit.jabbah.io.Storable
import io.antarescircuit.jabbah.io.StoreReader
import io.antarescircuit.jabbah.io.StoreWriter

class AnalogPower : AbstractAnalogVertice<AnalogPower>(
	EmptyVerticeCalculator,
	"library.element.AnalogPower",
	AnalogElementMixin(postCount = 1)
) {
	var voltage: Double = 5.0
		set(value) {
			if (value != field) {
				field = value
				stateChanged(reason = MAIN_PROPERTY_STATE)
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