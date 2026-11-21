package io.antarescircuit.antares.web

import io.antarescircuit.antares.model.analog.AnalogOscilloscopeSignalType
import io.antarescircuit.antares.model.input.SwitchConfiguration
import io.antarescircuit.antares.view.Handedness
import io.antarescircuit.antares.view.analog.ACVoltageSourceView
import io.antarescircuit.antares.view.analog.AnalogCircuitInOutView
import io.antarescircuit.antares.view.analog.AnalogDoubleThrowSwitchView
import io.antarescircuit.antares.view.analog.AnalogGraphView
import io.antarescircuit.antares.view.analog.AnalogLEDView
import io.antarescircuit.antares.view.analog.AnalogPowerView
import io.antarescircuit.antares.view.analog.AnalogRelayView
import io.antarescircuit.antares.view.analog.AnalogSwitchView
import io.antarescircuit.antares.view.analog.AnalogTransistorView
import io.antarescircuit.antares.view.analog.BatteryView
import io.antarescircuit.antares.view.analog.CapacitorView
import io.antarescircuit.antares.view.analog.CurrentSourceView
import io.antarescircuit.antares.view.analog.InductorView
import io.antarescircuit.antares.view.analog.LightBulbView
import io.antarescircuit.antares.view.analog.OpAmpView
import io.antarescircuit.antares.view.analog.ResistorView
import io.antarescircuit.antares.view.oscilloscope.AnalogOscilloscopeProbeVerticeView
import io.antarescircuit.jabbah.edit.properties.magnitude.SIUnit
import io.antarescircuit.jabbah.graph.model.PortType

/** `AnalogProperties.variable()` */
private fun <B : Any> WebPropertyBuilder<B>.variable(get: (B) -> Boolean, set: (B, Boolean) -> Unit) =
	bool("variable", "element.property.variable", get, set)

private fun <B : Any> WebPropertyBuilder<B>.closedOnStart(get: (B) -> Boolean, set: (B, Boolean) -> Unit) =
	bool("closedOnStart", "element.property.Switch.closedOnStart", get, set)

/**
 * Analog components and the analog circuit sheet. Orientation (AnalogComponentViewBeanInfo) is registered
 * generically for oriented vertices, and transistor type/symbol for all transistors.
 */
internal fun WebPropertyRegistry.registerAnalogProperties() {
	// AnalogGraphViewBeanInfo
	define<AnalogGraphView> {
		double("timeStep", "antares.analog.timeStep", { it.timeStep }, { b, v -> b.timeStep = v }, min = 0.0, group = WebGroups.SIMULATION)
	}

	// AnalogCircuitInOutViewBeanInfo
	define<AnalogCircuitInOutView> { io ->
		description({ it.description }, { b, v -> b.description = v })
		if (io.model.portType == PortType.OUTPUT) {
			optionalLong("outputResistance", "element.property.outputResistance", { it.outputResistance }, { b, v -> b.outputResistance = v }, min = 0)
		}
	}

	define<ResistorView> {
		magnitude("resistance", "element.property.resistance", { it.resistance }, { b, v -> b.resistance = v }, SIUnit.Ohm)
		variable({ it.variable }, { b, v -> b.variable = v })
	}
	define<CapacitorView> {
		magnitude("capacitance", "element.property.capacitance", { it.capacitance }, { b, v -> b.capacitance = v }, SIUnit.Farad)
		variable({ it.variable }, { b, v -> b.variable = v })
	}
	define<InductorView> {
		magnitude("inductance", "element.property.inductance", { it.inductance }, { b, v -> b.inductance = v }, SIUnit.Henry)
		variable({ it.variable }, { b, v -> b.variable = v })
	}
	define<BatteryView> {
		magnitude("voltage", "element.property.voltage", { it.voltage }, { b, v -> b.voltage = v }, SIUnit.Volt)
	}
	define<AnalogPowerView> {
		magnitude("voltage", "element.property.voltage", { it.voltage }, { b, v -> b.voltage = v }, SIUnit.Volt)
	}
	define<ACVoltageSourceView> {
		magnitude("maxVoltage", "element.property.maxVoltage", { it.maxVoltage }, { b, v -> b.maxVoltage = v }, SIUnit.Volt)
		magnitude("frequency", "element.property.frequency", { it.frequency }, { b, v -> b.frequency = v }, SIUnit.Hertz)
	}
	define<CurrentSourceView> {
		magnitude("current", "element.property.current", { it.current }, { b, v -> b.current = v }, SIUnit.Ampere)
	}

	define<AnalogSwitchView> {
		closedOnStart({ it.closedOnStart }, { b, v -> b.closedOnStart = v })
	}
	define<AnalogDoubleThrowSwitchView> {
		closedOnStart({ it.closedOnStart }, { b, v -> b.closedOnStart = v })
	}

	// AnalogRelayViewBeanInfo
	define<AnalogRelayView> { relay ->
		enum(
			"switchConfiguration", "element.property.switchConfiguration", SwitchConfiguration.entries,
			{ it.switchConfiguration }, { b, v -> b.switchConfiguration = v }, editable = !relay.model.isConnected,
		)
		magnitude("inductance", "element.property.inductance", { it.inductance }, { b, v -> b.inductance = v }, SIUnit.Henry)
		magnitude("onCurrent", "element.property.relay.onCurrent", { it.onCurrent }, { b, v -> b.onCurrent = v }, SIUnit.Ampere)
		if (relay.switchConfiguration == SwitchConfiguration.SPST) {
			bool("normallyOn", "element.property.relay.normallyOn", { it.normallyOn }, { b, v -> b.normallyOn = v })
		}
	}

	// AnalogTransistorViewBeanInfo (type and symbol: see TransistorViewBeanInfo registration)
	define<AnalogTransistorView> {
		enum("handedness", "element.property.handedness", Handedness.entries, { it.handedness }, { b, v -> b.handedness = v }, group = WebGroups.APPEARANCE)
		double("gain", "element.property.transistorGain", { it.gain }, { b, v -> b.gain = v }, min = 0.0)
	}

	// OpAmpViewBeanInfo
	define<OpAmpView> {
		enum("handedness", "element.property.OpAmp.handedness", Handedness.entries, { it.handedness }, { b, v -> b.handedness = v }, group = WebGroups.APPEARANCE)
		magnitude("maxOutput", "element.property.OpAmp.maxOutput", { it.maxOutput }, { b, v -> b.maxOutput = v }, SIUnit.Volt)
		magnitude("minOutput", "element.property.OpAmp.minOutput", { it.minOutput }, { b, v -> b.minOutput = v }, SIUnit.Volt)
	}

	// AnalogLEDViewBeanInfo
	define<AnalogLEDView> { led ->
		optionalText("name", "edit.property.name", { it.name }, { b, v -> b.name = v })
		lightColor(baseKey = "element.property.LightColor", get = { it.lightColor }, set = { b, v -> b.lightColor = v!! }, current = led.lightColor)
		magnitude("minCurrent", "library.element.LightBulb.minCurrent", { it.minCurrent }, { b, v -> b.minCurrent = v }, SIUnit.Ampere)
		magnitude("maxCurrent", "library.element.LightBulb.maxCurrent", { it.maxCurrent }, { b, v -> b.maxCurrent = v }, SIUnit.Ampere)
	}

	// LightBulbViewBeanInfo
	define<LightBulbView> { bulb ->
		lightColor(baseKey = "element.property.LightColor", get = { it.lightColor }, set = { b, v -> b.lightColor = v!! }, current = bulb.lightColor)
		magnitude("resistance", "element.property.resistance", { it.resistance }, { b, v -> b.resistance = v }, SIUnit.Ohm)
		double("minCurrent", "library.element.LightBulb.minCurrent", { it.minCurrent }, { b, v -> b.minCurrent = v }, min = 0.0)
		double("maxCurrent", "library.element.LightBulb.maxCurrent", { it.maxCurrent }, { b, v -> b.maxCurrent = v }, min = 0.0)
	}

	// AnalogOscilloscopeProbeVerticeViewBeanInfo
	define<AnalogOscilloscopeProbeVerticeView> {
		enum("signalType", AnalogOscilloscopeSignalType.BASE_KEY, AnalogOscilloscopeSignalType.entries, { it.signalType }, { b, v -> b.signalType = v })
	}
}
