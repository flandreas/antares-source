package ch.scorpion.antares.view.net

import ch.scorpion.antares.AbstractCircuitTest
import ch.scorpion.antares.TestCircuitBuilder
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.Word
import ch.scorpion.antares.view.input.SwitchView
import ch.scorpion.jabbah.graph.view.GraphView
import kotlin.test.BeforeTest
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals

/** Unit tests for [TunnelView].*/
class TunnelViewTest : AbstractCircuitTest() {

	private lateinit var builder: TestCircuitBuilder
	private lateinit var circuitView: GraphView
	private lateinit var switchView: SwitchView
	private lateinit var sender: TunnelView
	private lateinit var receiver: TunnelView

	override fun getCircuitView(): GraphView = circuitView

	@BeforeTest
	fun setupCircuit() {
		builder = TestCircuitBuilder("test", styleProvider, eventBus)
		sender = builder.addVerticeView(TunnelView("A"))
		receiver = builder.addVerticeView(TunnelView("A"))
		switchView = builder.addVerticeView(SwitchView())
		builder.connect(switchView, sender)
		circuitView = builder.build()
	}

	@Test
	fun shouldInitialize() {
		startSimulation(1100L)
		proceedToMillis(2200L)

		assertEquals(Word.of(false), sender.model.getIncomingSignal() as Word)
	}

	@Test
	fun shouldForwardSignal() {
		startSimulation(1100L)

		switchView.model.toggle(scheduler)
		proceedUntilQueueIsEmpty()

		assertEquals(Word.of(true), sender.model.getInOrOutSignal() as Word)
		assertEquals(Word.of(true), receiver.model.getInOrOutSignal() as Word)
	}

	@Test
	fun shouldForwardSignalToMultipleReceivers() {
		val receiver2 = builder.addVerticeView(TunnelView("A"))

		startSimulation(1100L)
		switchView.model.toggle(scheduler)
		proceedToMillis(2200L)

		assertEquals(Word.of(true), receiver.model.getInOrOutSignal() as Word)
		assertEquals(Word.of(true), receiver2.model.getInOrOutSignal() as Word)
	}

	@Test
	fun shouldNotForwardSignalToOtherName() {
		val tunnelB = builder.addVerticeView(TunnelView("B"))

		startSimulation(1100L)
		switchView.model.toggle(scheduler)
		proceedToMillis(2200L)

		assertEquals(Word.of(true), receiver.model.getInOrOutSignal() as Word)
		assertEquals(Word.undefined(BitWidth.BW_1), tunnelB.model.getInOrOutSignal() as Word)
	}
}