package ch.scorpion.antares.filebased

import ch.scorpion.antares.model.input.RealSwitch
import ch.scorpion.antares.model.net.DigitalNet
import ch.scorpion.antares.model.net.PullResistor
import ch.scorpion.antares.model.signal.DigitalSignalFactory
import ch.scorpion.jabbah.base.UUID
import kotlin.test.BeforeTest
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Regression test for GitHub bug issue #606 regarding [RealSwitch] and [PullResistor]
 * interaction and possible race condition during simulation start-up.
 *
 * Note: Bug occurs if simulation is started, [RealSwitch] is switched on, simulation is
 * stopped and the restarted. After restart, the [RealSwitch] is still on, which is maybe
 * another bug that might be fixed in the future.
 */
class Bug606 : AbstractFileBasedTest() {

	private lateinit var realSwitch: RealSwitch
	private lateinit var net: DigitalNet

	@BeforeTest
	fun openAndStartCircuit() {
		openCircuit(UUID("fc079ade-4c18-40b9-903e-cd731791cd23"))

		realSwitch = openedCircuitView.graph!!.withId(18) as RealSwitch
		net = openedCircuitView.graph!!.withId(17) as DigitalNet
	}

	@Ignore
	@Test
	fun shouldPullDownAfterSecondRestart() {
		startSimulation()
		processUntilQueueIsEmpty()
		realSwitch.toggle(scheduler)

		stopSimulation()
		startSimulation()
		processUntilQueueIsEmpty()

		assertEquals(DigitalSignalFactory.of(false), net.signal)
	}
}