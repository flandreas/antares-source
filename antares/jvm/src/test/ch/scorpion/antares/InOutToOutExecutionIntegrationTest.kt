package ch.scorpion.antares

import ch.scorpion.antares.model.inout.DigitalCircuitInOutImpl
import ch.scorpion.antares.model.input.Switch
import ch.scorpion.antares.model.signal.Bit
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.DigitalSignalFactory
import ch.scorpion.antares.view.input.SwitchView
import ch.scorpion.antares.view.output.LEDView
import ch.scorpion.jabbah.execution.actor.ActorListener
import ch.scorpion.jabbah.graph.library.LibraryElement
import ch.scorpion.jabbah.graph.library.LibraryModule
import ch.scorpion.jabbah.graph.model.PortType
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVertice
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVerticeRef
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeView
import dev.mokkery.MockMode
import dev.mokkery.mock
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * An integration test for sending a signal to a [DigitalCircuitInOutImpl] with [PortType.INOUT] in a sub-circuit.
 */
class InOutToOutExecutionIntegrationTest : AbstractJvmCircuitTest() {

	private lateinit var circuitView: GraphView
	private val actorListener = mock<ActorListener>(MockMode.autofill)
	private val library get() = LibraryModule.libraryHolder.library

	private lateinit var subGraphVV: SubGraphVerticeView<out SubGraphVertice>
	private lateinit var switchView: SwitchView
	private lateinit var ledView: LEDView

	override fun getCircuitView(): GraphView = circuitView

	override fun setup() {
		super.setup()
		switchView = SwitchView()
		ledView = LEDView()

		setupLibrary()

		TestLibraryBuilder().addInOutToOut(library)
		subGraphVV = (library.get(TestLibraryBuilder.INOUT_TO_OUT) as LibraryElement).getNewInstance<SubGraphVerticeRef>()
			as SubGraphVerticeView<out SubGraphVertice>
		val builder = TestCircuitBuilder("test", styleProvider, eventBus)

		builder.addVerticeView(subGraphVV)
		builder.addVerticeView(switchView)
		builder.connect(switchView, subGraphVV)

		builder.addVerticeView(ledView)
		builder.connect(subGraphVV, subGraphVV.model.getOutput("O"), ledView, ledView.model.getInput())

		circuitView = builder.build()
	}

	@Test
	fun shouldForwardToInOut() {
		startSimulation()
		scheduler.proceedUntilQueueIsEmpty(timeService, actorListener)
		assertEquals(DigitalSignalFactory.of(Bit.False), subGraphVV.model.getOutput<DigitalSignal>().getOutgoingSignal())
		val executionTime = scheduler.executionTime

		switchView.model.on(scheduler)
		proceedToNanos(executionTime + Switch.DEF_PROP_DELAY.value + 1)

		assertEquals(DigitalSignalFactory.of(true), subGraphVV.model.getOutput<DigitalSignal>().getOutgoingSignal())
	}
}