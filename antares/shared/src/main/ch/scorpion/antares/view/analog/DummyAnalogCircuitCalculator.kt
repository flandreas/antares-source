package ch.scorpion.antares.view.analog

import ch.scorpion.antares.model.analog.AnalogSignal
import ch.scorpion.jabbah.base.math.SIGMA
import ch.scorpion.jabbah.execution.SignalHandler

/**
 * Performs voltage/current calculation of a fixed, static circuit consisting of
 * a [BatteryView], a [ResistorView], a [AnalogSwitchView] and a [LightBulbView].
 *
 * This is a placeholder for developing current flow animation until the real
 * simulation calculation based on a linear equation system for the Kirchhoff's laws
 * has been implemented.
 */
object DummyAnalogCircuitCalculator : AnalogCircuitCalculator {

	override fun calculate(circuitView: AnalogGraphView, signalHandler: SignalHandler) {
		println("Performing dummy analog circuit calculation")

		val batteryView = circuitView.getWithId(4) as BatteryView
		val switchView = circuitView.getWithId(18) as AnalogSwitchView
		val lightBulbView1 = circuitView.getWithId(10) as LightBulbView
		val lightBulbView2 = circuitView.getWithId(23) as LightBulbView
		val resistorView1 = circuitView.getWithId(14) as ResistorView
		val resistorView2 = circuitView.getWithId(22) as ResistorView

		val evBattery = circuitView.getWithId(9) as AnalogEdgeView
		val evSwitch = circuitView.getWithId(20) as AnalogEdgeView
		val evNodeA1 = circuitView.getWithId(25) as AnalogEdgeView
		val evNodeA2 = circuitView.getWithId(26) as AnalogEdgeView
		val evLightBulb1 = circuitView.getWithId(15) as AnalogEdgeView
		val evLightBulb2 = circuitView.getWithId(27) as AnalogEdgeView
		val evResistor1 = circuitView.getWithId(29) as AnalogEdgeView
		val evResistor2 = circuitView.getWithId(30) as AnalogEdgeView
		val evNodeB = circuitView.getWithId(33) as AnalogEdgeView
		val evNodeC = circuitView.getWithId(16) as AnalogEdgeView
		val evGround = circuitView.getWithId(34) as AnalogEdgeView

		val r1 = resistorView1.model.resistance
		val r2 = resistorView2.model.resistance
		val r = if (r1 <= SIGMA || r2 <= SIGMA) 0.0 else 1 / (1 / r1 + 1 / r2)
		val v = batteryView.voltage

		val isSwitchOn = switchView.model.isOn
		val signalBehindSwitch = if (isSwitchOn) AnalogSignal(5f) else AnalogSignal(0f)
		val signalAtNegativePole = AnalogSignal(0f)
		val current = if (isSwitchOn) v / r else 0.0
		val current1 = if (isSwitchOn) v / r1 else 0.0
		val current2 = if (isSwitchOn) v / r2 else 0.0

		evBattery.model.setSignal(AnalogSignal(5f), batteryView.model.getOutput(1), batteryView.model.getOutput(1), signalHandler, false)
		evBattery.current = current

		evSwitch.model.setSignal(signalBehindSwitch, switchView.model.getOutput(2), switchView.model.getOutput(2), signalHandler, false)
		evSwitch.current = current

		evNodeA1.current = current1
		evNodeA2.current = current2

		evLightBulb1.model.setSignal(signalBehindSwitch, lightBulbView1.model.getOutput(2), lightBulbView1.model.getOutput(2), signalHandler, false)
		evLightBulb1.current = current1

		evResistor1.model.setSignal(signalAtNegativePole, batteryView.model.getOutput(2), batteryView.model.getOutput(2), signalHandler, false)
		evResistor1.current = -current1

		evLightBulb2.model.setSignal(signalBehindSwitch, lightBulbView2.model.getOutput(2), lightBulbView2.model.getOutput(2), signalHandler, false)
		evLightBulb2.current = current2

		evResistor2.model.setSignal(signalAtNegativePole, lightBulbView2.model.getOutput(2), lightBulbView2.model.getOutput(2), signalHandler, false)
		evResistor2.current = -current2

		evNodeB.current = -current
		evNodeC.current = -current
		evGround.current = current

		circuitView.invalidate()
		circuitView.validate()
	}
}