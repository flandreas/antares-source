package io.antarescircuit.antares.filebased

import io.antarescircuit.antares.AbstractCircuitTest
import io.antarescircuit.antares.model.signal.BitWidth
import io.antarescircuit.antares.model.signal.DigitalSignal
import io.antarescircuit.antares.model.signal.DigitalSignalFactory
import io.antarescircuit.antares.standardlibrary.AbstractStandardLibraryBasedCircuitTest
import io.antarescircuit.antares.view.addressable.ROMView
import io.antarescircuit.jabbah.base.UUID
import io.antarescircuit.jabbah.base.event.Button
import io.antarescircuit.jabbah.base.event.MouseEventImpl
import io.antarescircuit.jabbah.base.event.MouseEventType
import io.antarescircuit.jabbah.edit.DrawingViewMockBuilder
import io.antarescircuit.jabbah.execution.actor.ActorInteractionContext
import io.antarescircuit.jabbah.execution.actor.ActorView
import io.antarescircuit.jabbah.graph.library.FileLibraryPersistenceService
import io.antarescircuit.jabbah.graph.library.LibraryIdentification
import io.antarescircuit.jabbah.graph.library.LibraryModule
import io.antarescircuit.jabbah.graph.library.dictionary.FileLibraryDictionaryPersistenceService
import io.antarescircuit.jabbah.graph.library.dictionary.LibraryDictionaryService
import io.antarescircuit.jabbah.graph.project.ProjectManagementService
import io.antarescircuit.jabbah.graph.project.ProjectModule
import io.antarescircuit.jabbah.graph.view.GraphElementView
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.vertice.SubGraphVerticeView
import java.nio.file.Paths
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull

/** Runs the sum program in the Microcomputer (Tanenbaum) sample project. */
class TanenbaumTest : AbstractCircuitTest() {

	private lateinit var circuitView: GraphView

	override fun getCircuitView(): GraphView = circuitView

	override fun setup() {
		super.setup()
		AbstractStandardLibraryBasedCircuitTest.setupLibrary()
		val path = Paths.get("samples").toAbsolutePath().toString()
		ProjectModule.projectDictionaryService = LibraryDictionaryService(FileLibraryDictionaryPersistenceService({ path }, "projects"))
		ProjectModule.projectLibraryPersistenceService = FileLibraryPersistenceService({ path }, "projects")
		ProjectModule.projectManagementService = ProjectManagementService()
		val project = ProjectModule.projectManagementService.load(
			LibraryIdentification(UUID("d10cb564-42c2-4880-baf4-17c507b1526a"), null),
		)
		LibraryModule.libraryHolder.l = project
		circuitView = project.getMetaGraph(UUID("8eef51ac-7c2d-4e37-b7c1-732514265fba")).graph.graphView
	}

	@Test
	fun shouldCalculateSumOfZeroThroughThreeShallow() = checkSum(deep = false)

	@Test
	fun shouldCalculateSumOfZeroThroughThreeDeep() = checkSum(deep = true)

	private fun checkSum(deep: Boolean) {
		scheduler.isDeepExecution = deep
		scheduler.isSoftBreakpointsEnabled = false
		try {
			startSimulation()
			val rom = circuitView.getWithId(5) as ROMView
			assertEquals(0x7065UL, rom.model.read(2))
			// Use the exclusive upper bound 4, so the program sums 0..3.
			rom.model.setDataAt(2, 0x7004UL, scheduler)

			// Let the circuit initialize before pressing Run.
			timeService.setTimeNanos(20_000)
			scheduler.proceedTo(20_000, maxIteration = 100_000)
			assertFalse(scheduler.isInitializing)

			clickRun()
			// The clock keeps scheduling events after the program finishes, so use a fixed deadline.
			val deadline = 40_000_000L
			timeService.setTimeNanos(deadline)
			scheduler.proceedTo(deadline, maxIteration = 1_000_000)
			assertEquals(deadline, scheduler.executionTime)

			val pio = circuitView.getWithId(6) as SubGraphVerticeView<*>
			assertEquals(
				DigitalSignalFactory.of(BitWidth.BW_16, 6),
				pio.model.getOutput<DigitalSignal>(2).getOutgoingSignal(),
			)
			assertNoIssues()
		} finally {
			stopSimulation()
		}
	}

	private fun clickRun() {
		val view = DrawingViewMockBuilder().withDrawing(circuitView).build<GraphElementView<*>, GraphView>()
		fun contextFor(type: MouseEventType) = ActorInteractionContext(
			signalHandler = scheduler,
			view = view,
			mouseEvent = MouseEventImpl(type, x = 52, y = 308, button = Button.BUTTON1, clickCount = 1),
			x = 52.0,
			y = 308.0,
		)
		val context = contextFor(MouseEventType.PRESSED)
		val actor = assertNotNull(circuitView.getDrawableAt(52.0, 308.0) { it is ActorView } as? ActorView)
		val handler = actor.getActorInteractionHandler(context)
		val captured = handler.mousePressed(context)
		proceedToNanos(scheduler.executionTime + 1_000)
		(captured ?: handler).mouseReleased(contextFor(MouseEventType.RELEASED))
	}
}
