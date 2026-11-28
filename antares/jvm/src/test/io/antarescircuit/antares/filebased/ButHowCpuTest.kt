package io.antarescircuit.antares.filebased

import io.antarescircuit.antares.AbstractCircuitTest
import io.antarescircuit.antares.model.signal.BitWidth
import io.antarescircuit.antares.model.signal.DigitalSignal
import io.antarescircuit.antares.model.signal.DigitalSignalFactory
import io.antarescircuit.antares.standardlibrary.AbstractStandardLibraryBasedCircuitTest
import io.antarescircuit.jabbah.base.UUID
import io.antarescircuit.jabbah.graph.library.FileLibraryPersistenceService
import io.antarescircuit.jabbah.graph.library.LibraryIdentification
import io.antarescircuit.jabbah.graph.library.LibraryModule
import io.antarescircuit.jabbah.graph.library.dictionary.FileLibraryDictionaryPersistenceService
import io.antarescircuit.jabbah.graph.library.dictionary.LibraryDictionaryService
import io.antarescircuit.jabbah.graph.model.vertice.SubGraphVerticeRef
import io.antarescircuit.jabbah.graph.project.ProjectManagementService
import io.antarescircuit.jabbah.graph.project.ProjectModule
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.vertice.SubGraphVerticeView
import java.nio.file.Paths
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

/** Runs the ROM counter program in the CPU of the "But How Do It Know?" sample project. */
class ButHowCpuTest : AbstractCircuitTest() {

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
			LibraryIdentification(UUID("8d32ddb2-f1e7-4894-93e1-e5b51fbb5841"), null),
		)
		LibraryModule.libraryHolder.l = project
		circuitView = project.getMetaGraph(UUID("2d76ec8a-4f01-453d-9af6-11ddd8895421")).graph.graphView
	}

	@Test
	fun shouldCountThroughFFAndWrapToZeroDeep() = checkCounter(deep = true)

	@Test
	fun shouldCountThroughFFAndWrapToZeroScripted() = checkCounter(deep = false)

	private fun checkCounter(deep: Boolean) {
		scheduler.isDeepExecution = deep
		scheduler.isSoftBreakpointsEnabled = false
		try {
			startSimulation()
			proceedToNanos(20_000)
			assertFalse(scheduler.isInitializing)
			val display = (circuitView.getWithId(144) as SubGraphVerticeView<*>).model
			val register = display.getGraph().elements.single { it.id == 15 } as SubGraphVerticeRef
			assertEquals(DigitalSignalFactory.of(BitWidth.BW_8, 0), register.getOutput<DigitalSignal>("o").getOutgoingSignal())

			// D is the shared CPU bus. Only sample data writes, excluding address selection and instruction fetches.
			val data = display.getInput<DigitalSignal>("D")
			val io = display.getInput<DigitalSignal>("I/O")
			val dataOrAddress = display.getInput<DigitalSignal>("D/A")
			val storeClock = display.getInput<DigitalSignal>("s I/O clk")
			fun isWritingDisplay(): Boolean =
				io.getIncomingSignal() == DigitalSignalFactory.of(true) &&
					dataOrAddress.getIncomingSignal() == DigitalSignalFactory.of(false) &&
					storeClock.getIncomingSignal() == DigitalSignalFactory.of(true)

			var wasWriting = isWritingDisplay()
			var writes = 0
			val deadline = 40_000_000L
			timeService.setTimeNanos(deadline)
			var steps = 0
			while (writes < 256 && scheduler.executionTime < deadline && steps < 6_000_000) {
				scheduler.execute()
				steps++
				val writing = isWritingDisplay()
				if (writing && !wasWriting) {
					// Let the display's register settle while the output instruction holds D stable.
					val settleTime = scheduler.executionTime + 500
					scheduler.proceedTo(settleTime)
					// The display starts at 0; the ROM program writes 1..0xFF, then 0.
					val expected = DigitalSignalFactory.of(BitWidth.BW_8, ((writes + 1) and 0xFF).toLong())
					assertEquals(expected, data.getIncomingSignal(), "Display.D at write ${writes + 1}")
					assertEquals(expected, register.getOutput<DigitalSignal>("o").getOutgoingSignal(), "Display register at write ${writes + 1}")
					writes++
				}
				wasWriting = writing
			}
			assertEquals(256, writes, "CPU did not count through 0xFF and wrap to 0 at ${scheduler.executionTime} ns after $steps steps")
			assertNoIssues()
		} finally {
			stopSimulation()
		}
	}
}
