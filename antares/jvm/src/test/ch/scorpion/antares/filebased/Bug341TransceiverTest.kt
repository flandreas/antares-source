package ch.scorpion.antares.filebased

import ch.scorpion.antares.view.inout.DigitalCircuitInOutView
import ch.scorpion.antares.view.input.SwitchView
import ch.scorpion.jabbah.base.UUID
import kotlin.test.BeforeTest
import kotlin.test.Test

class Bug341TransceiverTest : AbstractFileBasedTest() {

	companion object {
		init {
			configure()
		}
	}

	private lateinit var inputView: DigitalCircuitInOutView
	private lateinit var outputView: DigitalCircuitInOutView
	private lateinit var enableSwitchView: SwitchView

	@BeforeTest
	fun openAndStartCircuit() {
		openCircuit(UUID("26489bca-3db9-4b3c-bae1-1f787d486217"))

		inputView = openedCircuitView.getWithId(2) as DigitalCircuitInOutView
		outputView = openedCircuitView.getWithId(4) as DigitalCircuitInOutView
		enableSwitchView = openedCircuitView.getWithId(7) as SwitchView

		startSimulation()
	}

	@Test
	fun shouldNotOscillateDisabled() {
		enableSwitchView.model.toggle(scheduler, openedCircuitView)
		processUntilQueueIsEmpty()
	}
}