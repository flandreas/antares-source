package ch.scorpion.antares

import ch.scorpion.antares.model.inout.CircuitInOutImpl
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.DigitalSignalFactory
import ch.scorpion.antares.model.signal.Word
import ch.scorpion.antares.view.input.SwitchView
import ch.scorpion.antares.view.output.LEDView
import ch.scorpion.jabbah.graph.library.LibraryElement
import ch.scorpion.jabbah.graph.library.LibraryModule
import ch.scorpion.jabbah.graph.model.PortType
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVertice
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVerticeRef
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeView
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * An integration test for sending a signal to a [CircuitInOutImpl] with two [PortType.INOUT] in a sub-circuit.
 */
class InOutToInOutExecutionIntegrationTest : AbstractJvmCircuitTest() {

	companion object {
		init {
			AntaresTestRule.configure()
		}
	}

	private lateinit var circuitView: GraphView
	private val library get() = LibraryModule.libraryHolder.library

	private lateinit var subGraphVV: SubGraphVerticeView<out SubGraphVertice>
	private val switchView = SwitchView()

	private val ledView = LEDView()

	override fun getCircuitView(): GraphView = circuitView

	@BeforeTest
	fun setupCircuit() {
		setupLibrary()

		TestLibraryBuilder().addInOutToInOut(library)
		subGraphVV = (library.get(TestLibraryBuilder.INOUT_TO_INOUT) as LibraryElement).getNewInstance<SubGraphVerticeRef>()
			as SubGraphVerticeView<out SubGraphVertice>
		val builder = TestCircuitBuilder("test", styleProvider, eventBus)

		builder.addVerticeView(switchView)
		builder.addVerticeView(subGraphVV)
		builder.connect(switchView, to = subGraphVV, toPort = subGraphVV.model.getInput("IO1"))

		builder.addVerticeView(ledView)
		builder.connect(subGraphVV, subGraphVV.model.getOutput("IO2"), ledView, ledView.model.getInput())

		circuitView = builder.build()
	}

	@Test
	fun shouldBeZeroAfterStartup() {
		startSimulation()
		proceedUntilQueueIsEmpty()

		assertEquals(DigitalSignalFactory.of(false), subGraphVV.model.getOutput<DigitalSignal>("IO2").net!!.signal)
	}

	@Test
	fun shouldForwardChangedSignal() {
		startSimulation()
		proceedUntilQueueIsEmpty()

		switchView.model.on(scheduler)
		proceedUntilQueueIsEmpty()

		assertEquals(DigitalSignalFactory.of(true), subGraphVV.model.getOutput<DigitalSignal>("IO2").net!!.signal)
		assertTrue(ledView.model.isOn)
	}
}