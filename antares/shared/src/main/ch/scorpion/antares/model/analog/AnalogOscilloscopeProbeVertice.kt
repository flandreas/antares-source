package ch.scorpion.antares.model.analog

import ch.scorpion.antares.model.AntaresGraphTypes
import ch.scorpion.antares.view.analog.engine.AnalogElement
import ch.scorpion.antares.view.analog.engine.AnalogElementMixin
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.module.GraphModelModule
import ch.scorpion.jabbah.graph.model.oscilloscope.OscilloscopeProbeVertice
import ch.scorpion.jabbah.graph.model.port.PortFactory
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

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