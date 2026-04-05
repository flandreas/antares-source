package io.antarescircuit.antares

import io.antarescircuit.antares.model.signal.DigitalSignal
import io.antarescircuit.antares.model.signal.DigitalSignalFactory
import io.antarescircuit.antares.view.input.SwitchView
import io.antarescircuit.antares.view.output.LEDView
import io.antarescircuit.jabbah.execution.actor.ActorListener
import io.antarescircuit.jabbah.graph.library.LibraryElement
import io.antarescircuit.jabbah.graph.library.LibraryModule
import io.antarescircuit.jabbah.graph.model.vertice.SubGraphVertice
import io.antarescircuit.jabbah.graph.model.vertice.SubGraphVerticeRef
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.vertice.SubGraphVerticeView
import dev.mokkery.MockMode
import dev.mokkery.mock
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * An integration test for sending a signal through a "NOP" [SubGraphVertice].
 */
class NopExecutionIntegrationTest : AbstractJvmCircuitTest() {

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

		TestLibraryBuilder().addNOP(library)
		subGraphVV = (library.get(TestLibraryBuilder.NOP) as LibraryElement).getNewInstance<SubGraphVerticeRef>()
			as SubGraphVerticeView<out SubGraphVertice>
		val builder = TestCircuitBuilder("test", styleProvider, eventBus)

		builder.addVerticeView(switchView)
		builder.addVerticeView(subGraphVV)
		builder.addVerticeView(ledView)

		builder.connect(switchView, subGraphVV)
		builder.connect(subGraphVV, ledView)

		circuitView = builder.build()
	}

	@Test
	fun outputShouldBeZeroAtStartup() {
		startSimulation()
		scheduler.proceedUntilQueueIsEmpty(timeService, actorListener)

		assertEquals(DigitalSignalFactory.of(false), subGraphVV.model.getOutput<DigitalSignal>().net?.signal)
	}
}