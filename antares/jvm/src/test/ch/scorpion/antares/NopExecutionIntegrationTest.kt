package ch.scorpion.antares

import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.DigitalSignalFactory
import ch.scorpion.antares.model.signal.Word
import ch.scorpion.antares.view.input.SwitchView
import ch.scorpion.antares.view.output.LEDView
import ch.scorpion.jabbah.execution.actor.ActorListener
import ch.scorpion.jabbah.graph.library.LibraryElement
import ch.scorpion.jabbah.graph.library.LibraryModule
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVertice
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVerticeRef
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeView
import io.mockk.mockk
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * An integration test for sending a signal through a "NOP" [SubGraphVertice].
 */
class NopExecutionIntegrationTest : AbstractJvmCircuitTest() {

	companion object {
		init {
			AntaresTestRule.configure()
		}
	}

	private lateinit var circuitView: GraphView
	private val actorListener = mockk<ActorListener>(relaxed = true)
	private val library get() = LibraryModule.libraryHolder.library

	private lateinit var subGraphVV: SubGraphVerticeView<out SubGraphVertice>
	private val switchView = SwitchView()
	private val ledView = LEDView()

	override fun getCircuitView(): GraphView = circuitView

	@BeforeTest
	fun setupCircuit() {
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