package io.antarescircuit.antares

import io.antarescircuit.antares.model.inout.DigitalCircuitInOutImpl
import io.antarescircuit.antares.model.signal.DigitalSignal
import io.antarescircuit.antares.model.signal.DigitalSignalFactory
import io.antarescircuit.antares.view.input.SwitchView
import io.antarescircuit.antares.view.output.LEDView
import io.antarescircuit.jabbah.graph.library.LibraryElement
import io.antarescircuit.jabbah.graph.library.LibraryModule
import io.antarescircuit.jabbah.graph.model.PortType
import io.antarescircuit.jabbah.graph.model.vertice.SubGraphVertice
import io.antarescircuit.jabbah.graph.model.vertice.SubGraphVerticeRef
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.vertice.SubGraphVerticeView
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * An integration test for sending a signal to a [DigitalCircuitInOutImpl] with two [PortType.INOUT] in a sub-circuit.
 */
class InOutToInOutExecutionIntegrationTest : AbstractJvmCircuitTest() {

	private lateinit var circuitView: GraphView
	private val library get() = LibraryModule.libraryHolder.library

	private lateinit var subGraphVV: SubGraphVerticeView<out SubGraphVertice>
	private lateinit var switchView: SwitchView
	private lateinit var ledView: LEDView

	override fun getCircuitView(): GraphView = circuitView

	@BeforeTest
	override fun setup() {
		super.setup()
		switchView = SwitchView()
		ledView = LEDView()

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