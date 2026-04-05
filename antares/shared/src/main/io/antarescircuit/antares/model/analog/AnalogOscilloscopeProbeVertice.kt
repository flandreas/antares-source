package io.antarescircuit.antares.model.analog

import io.antarescircuit.antares.model.AntaresGraphTypes
import io.antarescircuit.antares.view.analog.engine.AnalogElement
import io.antarescircuit.antares.view.analog.engine.AnalogElementMixin
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.execution.SignalHandler
import io.antarescircuit.jabbah.graph.model.module.GraphModelModule
import io.antarescircuit.jabbah.graph.model.oscilloscope.OscilloscopeProbeVertice
import io.antarescircuit.jabbah.graph.model.port.PortFactory
import io.antarescircuit.jabbah.io.Storable
import io.antarescircuit.jabbah.io.StoreReader
import io.antarescircuit.jabbah.io.StoreWriter

data class AnalogOscilloscopeSignalTypeEvent(
	val source: AnalogOscilloscopeProbeVertice,
	val oldValue: AnalogOscilloscopeSignalType,
	val newValue: AnalogOscilloscopeSignalType
)

class AnalogOscilloscopeProbeVertice(
	portFactory: PortFactory = GraphModelModule.portFactory,
	private val analogElement: AnalogElementMixin = AnalogElementMixin()
) : OscilloscopeProbeVertice<AnalogSignal>(AntaresGraphTypes.Analog, portFactory),
	AnalogVertice,
	AnalogElement by analogElement
{

	var signalType: AnalogOscilloscopeSignalType = AnalogOscilloscopeSignalType.Voltage
		set(value) {
			if (field != value) {
				val oldValue = field
				field = value
				if (!isReading) {
					stateChanged()
					BaseModule.eventBus.post(AnalogOscilloscopeSignalTypeEvent(this, oldValue, field))
				}
			}
		}

	/** ---- [Storable] interface */

	override fun write(writer: StoreWriter) {
		super.write(writer)
		writer.writeString("signalType", signalType.customName)
	}

	override fun read(reader: StoreReader) {
		super.read(reader)
		if (reader.hasAttribute("signalType")) {
			signalType = AnalogOscilloscopeSignalType.withName(reader.readString("signalType"))
		}
	}

	/** ---- [AnalogVertice] */

	override fun handleAnalogPortChanged(port: AnalogPort, signalHandler: SignalHandler) {
		stateChanged(signalHandler)
	}
}