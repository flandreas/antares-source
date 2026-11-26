package io.antarescircuit.antares.filebased

import io.antarescircuit.antares.AbstractCircuitTest
import io.antarescircuit.antares.standardlibrary.AbstractStandardLibraryBasedCircuitTest
import io.antarescircuit.jabbah.base.IssueImpl
import io.antarescircuit.jabbah.base.UUID
import io.antarescircuit.jabbah.graph.library.FileLibraryPersistenceService
import io.antarescircuit.jabbah.graph.library.LibraryIdentification
import io.antarescircuit.jabbah.graph.library.LibraryModule
import io.antarescircuit.jabbah.graph.library.dictionary.FileLibraryDictionaryPersistenceService
import io.antarescircuit.jabbah.graph.library.dictionary.LibraryDictionaryService
import io.antarescircuit.jabbah.graph.project.ProjectManagementService
import io.antarescircuit.jabbah.graph.project.ProjectModule
import io.antarescircuit.jabbah.graph.view.GraphView
import java.nio.file.Paths
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** Tests start-up behavior of the "But Do It Know?" circuit*/
class ButHowStartupTest : AbstractCircuitTest() {
	private lateinit var circuitView: GraphView

	override fun getCircuitView(): GraphView = circuitView

	override fun setup() {
		super.setup()
		AbstractStandardLibraryBasedCircuitTest.setupLibrary()
		val path = Paths.get("samples").toAbsolutePath().toString()
		ProjectModule.projectDictionaryService = LibraryDictionaryService(FileLibraryDictionaryPersistenceService({ path }, "projects"))
		ProjectModule.projectLibraryPersistenceService = FileLibraryPersistenceService({ path }, "projects")
		ProjectModule.projectManagementService = ProjectManagementService()
		val project = ProjectModule.projectManagementService.load(LibraryIdentification(UUID("8d32ddb2-f1e7-4894-93e1-e5b51fbb5841"), null))
		LibraryModule.libraryHolder.l = project
		circuitView = project.getMetaGraph(UUID("2d76ec8a-4f01-453d-9af6-11ddd8895421")).graph.graphView
	}

	@Test
	fun shouldEndStartupAtConfiguredDurationDeep() = checkStartup(true)

	@Test
	fun shouldEndStartupAtConfiguredDurationScripted() = checkStartup(false)

	private fun checkStartup(deep: Boolean) {
		val issues = mutableListOf<IssueImpl>()
		eventBus.register(IssueImpl::class) { issues.add(it) }
		scheduler.isDeepExecution = deep
		scheduler.isSoftBreakpointsEnabled = false
		try {
			startSimulation()
			assertEquals(150L, scheduler.initializationDeadline)
			timeService.setTimeNanos(150)
			var waves = 0
			while (scheduler.isInitializing) {
				assertTrue(++waves < 10_000, "Startup did not finish at its deadline")
				scheduler.execute()
			}
			assertEquals(150L, scheduler.executionTime)
			assertFalse(scheduler.isQueueEmpty, "The gate-built oscillator must still be running")
			timeService.setTimeNanos(20_000)
			scheduler.proceedTo(20_000, maxIteration = 10_000)
			assertFalse(scheduler.isInitializing)
			assertTrue(scheduler.executionTime >= 20_000)
			assertTrue(issues.isEmpty(), issues.joinToString { "${it.name}: ${it.description}" })
		} finally {
			stopSimulation()
		}
	}
}
