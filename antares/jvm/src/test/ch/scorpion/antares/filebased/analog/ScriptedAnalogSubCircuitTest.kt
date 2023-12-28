package ch.scorpion.antares.filebased.analog

import ch.scorpion.antares.view.input.SwitchView
import ch.scorpion.antares.view.output.LEDView
import ch.scorpion.jabbah.base.UUID
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlin.test.BeforeTest
import kotlin.test.Test

/**
 * Executes an analog circuit with an execution script within a digital circuit.
 *
 * Also ensures that signals, which are expressed in the analog script as voltages,
 * are first converted to digital signals before they are applied at the outside ports
 * of the sub circuit.
 */
class ScriptedAnalogSubCircuitTest : AbstractAnalogFileBasedTest() {

	private lateinit var switchView: SwitchView
	private lateinit var ledView: LEDView

	@BeforeTest
	fun openCircuit() {
		openCircuit(UUID("5a9cd188-59fd-4c63-90d2-c1678c1c7cbe"))
		switchView = openedCircuitView.getWithId(2) as SwitchView
		ledView = openedCircuitView.getWithId(3) as LEDView
	}

	@Test
	fun shouldBeHighOnStartup() {
		scheduler.isDeepExecution = false
		startSimulation()
		processUntilQueueIsEmpty()

		assertNoIssues()
		assertTrue(ledView.model.isOn)
	}

	@Test
	fun shouldBeLowWithHighInput() {
		scheduler.isDeepExecution = false
		startSimulation()
		processUntilQueueIsEmpty()
		switchView.model.toggle(scheduler)
		processUntilQueueIsEmpty()

		assertNoIssues()
		assertFalse(ledView.model.isOn)
	}
}