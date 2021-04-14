package ch.scorpion.antares

import ch.scorpion.antares.model.gate.TriStateBufferGate
import ch.scorpion.antares.model.inout.CircuitInOut
import ch.scorpion.antares.model.input.Switch
import ch.scorpion.antares.model.signal.Bit
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.Word
import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.execution.actor.ActorListener
import ch.scorpion.jabbah.graph.library.*
import ch.scorpion.jabbah.graph.library.dictionary.FileLibraryDictionaryPersistenceService
import ch.scorpion.jabbah.graph.library.dictionary.LibraryDictionaryService
import ch.scorpion.jabbah.graph.library.dictionary.ResourceLibraryDictionaryPersistenceService
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVerticeRef
import ch.scorpion.jabbah.graph.project.ProjectManagementService
import ch.scorpion.jabbah.graph.project.ProjectModule
import ch.scorpion.jabbah.graph.view.GraphView
import io.mockk.mockk
import java.nio.file.Paths
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class BidirectionalSplitterWithSubGraphTest : AbstractCircuitTest() {

	companion object {
		init {
			val path = Paths.get("jvm/rsc/test/projects").toAbsolutePath().toString()
			AntaresTestRule.configure()

			LibraryModule.systemLibraryPersistenceService = ResourceLibraryPersistenceService()
			LibraryModule.systemLibraryDictionaryService = LibraryDictionaryService(ResourceLibraryDictionaryPersistenceService())
			LibraryModule.libraryManagementService = LibraryManagementService()
			LibraryModule.libraryService = LibraryService()

			ProjectModule.projectDictionaryService = LibraryDictionaryService(FileLibraryDictionaryPersistenceService(path))
			ProjectModule.projectLibraryPersistenceService = FileLibraryPersistenceService(path)
			ProjectModule.projectManagementService = ProjectManagementService()

			LibraryModule.libraryHolder.l = LibraryModule.libraryService.loadLibrary(AntaresApplication.DEF_LIBRARY_UUID, isSystem = true)
		}
	}

	private val actorListener = mockk<ActorListener>(relaxed = true)
	private lateinit var circuitView: GraphView

	private lateinit var dirSwitch: Switch
	private lateinit var inOutA0: CircuitInOut
	private lateinit var inOutA1: CircuitInOut
	private lateinit var inOutB: CircuitInOut

	override fun getCircuitView(): GraphView = circuitView

	@BeforeTest
	fun openAndStartCircuit() {
		ProjectModule.projectManagementService.open(UUID("e70cb564-42c2-4880-baf4-17c507b1526a"))
		val metaGraph = ProjectModule.projectHolder.p!!.getMetaGraph(UUID("4e24ce93-6521-4911-a8be-bce39ce6147a"))
		circuitView = metaGraph.graph.graphView

		startSimulation()
		scheduler.proceedUntilQueueIsEmpty(timeService, actorListener)

		dirSwitch = circuitView.graph!!.withId(8) as Switch
		inOutA0 = circuitView.graph!!.withId(3) as CircuitInOut
		inOutA1 = circuitView.graph!!.withId(12) as CircuitInOut
		inOutB = circuitView.graph!!.withId(4) as CircuitInOut
	}

	@AfterTest
	fun cleanup() {
		stopSimulation()
	}

	@Test
	fun checkLengthOfChainBit0() {
		val combinedNet = circuitView.graph!!.getGraphPort<DigitalSignal>("B")!!.getOutput<DigitalSignal>().combinedNet!!

		val busDriverA0 = circuitView.graph!!.withId(1) as SubGraphVerticeRef
		val triStateBufferA0 = busDriverA0.getGraphIfPresent()!!.withId(4) as TriStateBufferGate
		val chain = combinedNet.getChainTo(triStateBufferA0.getOutputPort())

		assertEquals(1, chain!!.convertersCount)
	}

	@Test
	fun checkLengthOfChainBit1() {
		val combinedNet = circuitView.graph!!.getGraphPort<DigitalSignal>("B")!!.getOutput<DigitalSignal>().combinedNet!!

		val busDriverA1 = circuitView.graph!!.withId(10) as SubGraphVerticeRef
		val triStateBufferA1 = busDriverA1.getGraphIfPresent()!!.withId(4) as TriStateBufferGate
		val chain = combinedNet.getChainTo(triStateBufferA1.getOutputPort())

		assertEquals(1, chain!!.convertersCount)
	}

	@Test
	fun shouldPropagateBit0() {
		dirSwitch.on(scheduler)
		scheduler.proceedUntilQueueIsEmpty(timeService, actorListener)

		inOutB.setIncomingSignal(Word(listOf(Bit.True, Bit.Undefined)), scheduler)
		scheduler.proceedUntilQueueIsEmpty(timeService, actorListener)

		assertEquals(Word.of(true), inOutA0.signal)
	}

	@Test
	fun shouldPropagateBit1() {
		dirSwitch.on(scheduler)
		scheduler.proceedUntilQueueIsEmpty(timeService, actorListener)

		val splitter = circuitView.graph!!.withId(2)

		inOutB.setIncomingSignal(Word(listOf(Bit.Undefined, Bit.True)), scheduler)
		scheduler.proceedUntilQueueIsEmpty(timeService, actorListener)

		assertEquals(Word.of(true), inOutA1.signal)
	}

}