package ch.scorpion.antares.view.analog

import ch.scorpion.antares.model.analog.AnalogSignal
import ch.scorpion.jabbah.execution.SignalHandler

/**
 * Performs voltage/current calculation of a fixed, static circuit consisting of
 * a [BatteryView], a [ResistorView], a [AnalogSwitchView] and a [LightBulbView].
 *
 * This is a placeholder for developing current flow animation until the real
 * simulation calculation based on a linear equation system for the Kirchhoff's laws
 * has been implemented.
 */
object DummyAnalogCircuitCalculator {

	fun calculate(circuitView: AnalogGraphView, signalHandler: SignalHandler) {
		println("Performing dummy analog circuit calculation")

		val batteryView = circuitView.getWithId(4) as BatteryView
		val switchView = circuitView.getWithId(18) as AnalogSwitchView
		val lightBulbView = circuitView.getWithId(10) as LightBulbView
		//val resistorView = circuitView.getWithId(14) as ResistorView

		val evBattery = circuitView.getWithId(9) as AnalogEdgeView
		val evSwitch = circuitView.getWithId(11) as AnalogEdgeView
		val evLightBulb = circuitView.getWithId(15) as AnalogEdgeView
		val evResistor = circuitView.getWithId(16) as AnalogEdgeView

		val isSwitchOn = switchView.model.isOn
		val signalBehindSwitch = if (isSwitchOn) AnalogSignal(5f) else AnalogSignal(0f)

		evBattery.model.setSignal(AnalogSignal(5f), batteryView.model.getOutput(1), batteryView.model.getOutput(1), signalHandler, false)
		evSwitch.model.setSignal(signalBehindSwitch, switchView.model.getOutput(2), switchView.model.getOutput(2), signalHandler, false)
		evLightBulb.model.setSignal(signalBehindSwitch, lightBulbView.model.getOutput(2), lightBulbView.model.getOutput(2), signalHandler, false)
		evResistor.model.setSignal(AnalogSignal(0f), batteryView.model.getOutput(2), batteryView.model.getOutput(2), signalHandler, false)
	}
}