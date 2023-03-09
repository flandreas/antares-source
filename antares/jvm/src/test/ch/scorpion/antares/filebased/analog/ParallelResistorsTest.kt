package ch.scorpion.antares.filebased.analog

import ch.scorpion.antares.model.analog.AnalogNet
import ch.scorpion.antares.view.analog.AnalogEdgeView
import ch.scorpion.antares.view.analog.AnalogSwitchView
import ch.scorpion.antares.view.analog.BatteryView
import ch.scorpion.antares.view.analog.ResistorView
import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.base.math.near
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue

class ParallelResistorsTest : AbstractAnalogFileBasedTest() {

	private lateinit var batteryView: BatteryView
	private lateinit var switchView: AnalogSwitchView
	private lateinit var resistorView1: ResistorView
	private lateinit var resistorView2: ResistorView

	private lateinit var batteryPlusEV: AnalogEdgeView
	private lateinit var switchViewEV: AnalogEdgeView
	private lateinit var resistor1InEV: AnalogEdgeView
	private lateinit var resistor1OutEV: AnalogEdgeView
	private lateinit var resistor2InEV: AnalogEdgeView
	private lateinit var resistor2OutEV: AnalogEdgeView
	private lateinit var batteryMinusEV: AnalogEdgeView

	@BeforeTest
	fun openAndStartCircuit() {
		openCircuit(UUID("7833a5cd-1b34-47ab-823d-1c377420d498"))

		batteryView = openedCircuitView.getWithId(1) as BatteryView
		switchView = openedCircuitView.getWithId(3) as AnalogSwitchView
		resistorView1 = openedCircuitView.getWithId(2) as ResistorView
		resistorView2 = openedCircuitView.getWithId(4) as ResistorView

		batteryPlusEV = openedCircuitView.getWithId(5) as AnalogEdgeView
		switchViewEV = openedCircuitView.getWithId(6) as AnalogEdgeView
		resistor1InEV = openedCircuitView.getWithId(8) as AnalogEdgeView
		resistor1OutEV = openedCircuitView.getWithId(10) as AnalogEdgeView
		resistor2InEV = openedCircuitView.getWithId(9) as AnalogEdgeView
		resistor2OutEV = openedCircuitView.getWithId(13) as AnalogEdgeView
		batteryMinusEV = openedCircuitView.getWithId(12) as AnalogEdgeView

		startSimulation()
		processUntilQueueIsEmpty()
	}

	@Test
	fun shouldCalculate() {
		switchView.model.toggle(scheduler, openedCircuitView)
		processUntilQueueIsEmpty()

		val voltage = 5.0
		val ground = 0.0
		val currentOn = 0.06
		val currentOn1 = 0.05
		val currentOn2 = 0.01

		assertTrue((batteryPlusEV.model as AnalogNet).signal!!.voltage.near(voltage, 0.01))
		assertTrue(batteryPlusEV.current.near(currentOn, 0.01))
		assertTrue(switchViewEV.current.near(currentOn, 0.01))
		assertTrue(resistor1InEV.current.near(currentOn1, 0.01))
		assertTrue(resistor1OutEV.current.near(currentOn1, 0.01))
		assertTrue(resistor2InEV.current.near(currentOn2, 0.01))
		assertTrue(resistor2OutEV.current.near(-currentOn2, 0.01))

		assertTrue((switchViewEV.model as AnalogNet).signal!!.voltage.near(voltage, 0.01))
		assertTrue((batteryMinusEV.model as AnalogNet).signal!!.voltage.near(ground, 0.01))
	}
}