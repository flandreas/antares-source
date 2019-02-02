package ch.scorpion.antares.view.net

import ch.scorpion.antares.AbstractCircuitTest
import ch.scorpion.antares.TestCircuitBuilder
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.Word
import ch.scorpion.antares.view.input.SwitchView
import ch.scorpion.jabbah.graph.view.GraphElementView
import ch.scorpion.jabbah.graph.view.GraphView
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test

/** Unit tests for [TunnelView].*/
class TunnelViewTest : AbstractCircuitTest() {

	private lateinit var builder: TestCircuitBuilder
	private lateinit var circuitView: GraphView<GraphElementView<*>>
	private lateinit var switchView: SwitchView
	private lateinit var sender: TunnelView
	private lateinit var receiver: TunnelView

	override fun getCircuitView(): GraphView<GraphElementView<*>> = circuitView

	@Before
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
		startSimulation()
		proceedToMillis(1100L)

		assertThat(sender.model!!.getInOrOutSignal() as Word, `is`(Word.of(false)))
	}

	@Test
	fun shouldForwardSignal() {
		startSimulation()

		switchView.model!!.toggle(scheduler)
		proceedToMillis(1100L)

		assertThat(sender.model!!.getInOrOutSignal() as Word, `is`(Word.of(true)))
		assertThat(receiver.model!!.getInOrOutSignal() as Word, `is`(Word.of(true)))
	}

	@Test
	fun shouldForwardSignalToMultipleReceivers() {
		val receiver2 = builder.addVerticeView(TunnelView("A"))

		startSimulation()
		switchView.model!!.toggle(scheduler)
		proceedToMillis(1100L)

		assertThat(receiver.model!!.getInOrOutSignal() as Word, `is`(Word.of(true)))
		assertThat(receiver2.model!!.getInOrOutSignal() as Word, `is`(Word.of(true)))
	}

	@Test
	fun shouldNotForwardSignalToOtherName() {
		val tunnelB = builder.addVerticeView(TunnelView("B"))

		startSimulation()
		switchView.model!!.toggle(scheduler)
		proceedToMillis(1100L)

		assertThat(receiver.model!!.getInOrOutSignal() as Word, `is`(Word.of(true)))
		assertThat(tunnelB.model!!.getInOrOutSignal() as Word, `is`(Word.undefined(BitWidth.BW_1)))

	}
}