package ch.scorpion.antares.filebased.analog

import ch.scorpion.antares.filebased.AbstractFileBasedTest
import ch.scorpion.antares.model.analog.AnalogSignal
import ch.scorpion.antares.view.analog.*
import ch.scorpion.antares.view.module.AntaresViewModule
import ch.scorpion.jabbah.base.UUID
import kotlin.test.BeforeTest
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals

class ParallelResistorsTest : AbstractFileBasedTest() {

	companion object {
		init {
			configure()
			AntaresViewModule.analogCircuitCalculator = KirchhoffAnalogCircuitCalculator
		}
	}

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
		resistor1OutEV = openedCircuitView.getWithId(13) as AnalogEdgeView
		batteryMinusEV = openedCircuitView.getWithId(12) as AnalogEdgeView

		startSimulation()
	}

	@Ignore // Logic not yet implemented
	@Test
	fun shouldCalculate() {
		switchView.model.toggle(scheduler, openedCircuitView)
		processUntilQueueIsEmpty()

		val voltage = AnalogSignal(5.0)
		val ground = AnalogSignal(0.0)
		val currentOn = 0.06
		val currentOn1 = 0.05
		val currentOn2 = 0.01

		assertEquals(voltage, batteryPlusEV.model.signal)
		assertEquals(currentOn, batteryPlusEV.current)
		assertEquals(currentOn, switchViewEV.current)
		assertEquals(currentOn1, resistor1InEV.current)
		assertEquals(currentOn1, resistor1OutEV.current)
		assertEquals(currentOn2, resistor2InEV.current)
		assertEquals(currentOn2, resistor2OutEV.current)

		assertEquals(voltage, switchViewEV.model.signal)
		assertEquals(ground, batteryMinusEV.model.signal)
	}
}