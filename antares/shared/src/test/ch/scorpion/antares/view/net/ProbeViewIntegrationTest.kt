package ch.scorpion.antares.view.net

import ch.scorpion.antares.AbstractCircuitTest
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.Word
import ch.scorpion.antares.view.input.SwitchView
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.GraphViewBuilder
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ProbeViewIntegrationTest : AbstractCircuitTest() {

	private lateinit var builder: GraphViewBuilder<DigitalSignal>
	private lateinit var probeView1: ProbeView
	private lateinit var probeView2: ProbeView
	private lateinit var switchView: SwitchView

	override fun getCircuitView(): GraphView = builder.graphView

	@BeforeTest
	fun setupCircuit() {
		builder = GraphViewBuilder("test")
		probeView1 = builder.addVerticeView(ProbeView())
		probeView2 = builder.addVerticeView(ProbeView())
		switchView = builder.addVerticeView(SwitchView())
		builder.connect(switchView, probeView2)
	}

	@Test
	fun shouldBeUndefinedAtStartup() {
		startSimulation()
		proceedUntilQueueIsEmpty()

		assertEquals(Word.undefined(BitWidth.BW_1), probeView1.model.signal)
	}

	@Test
	fun shouldHaveIncomingSignalAtStartup() {
		startSimulation()
		proceedUntilQueueIsEmpty()

		assertEquals(Word.of(false), probeView2.model.signal)
	}
}