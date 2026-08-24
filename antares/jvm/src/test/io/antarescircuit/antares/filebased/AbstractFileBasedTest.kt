package io.antarescircuit.antares.filebased

import io.antarescircuit.antares.AbstractCircuitTest
import io.antarescircuit.antares.AntaresApplication
import io.antarescircuit.jabbah.app.AbstractDesktopApplication
import io.antarescircuit.jabbah.app.CurrentApplicationVersion
import io.antarescircuit.jabbah.base.UUID
import io.antarescircuit.jabbah.execution.actor.ActorListener
import io.antarescircuit.jabbah.graph.library.*
import io.antarescircuit.jabbah.graph.library.dictionary.FileLibraryDictionaryPersistenceService
import io.antarescircuit.jabbah.graph.library.dictionary.LibraryDictionaryService
import io.antarescircuit.jabbah.graph.library.dictionary.ResourceLibraryDictionaryPersistenceService
import io.antarescircuit.jabbah.graph.model.module.GraphModelModule
import io.antarescircuit.jabbah.graph.model.nonvolatile.NonVolatileServiceJvm
import io.antarescircuit.jabbah.graph.project.ProjectManagementService
import io.antarescircuit.jabbah.graph.project.ProjectModule
import io.antarescircuit.jabbah.graph.view.GraphView
import dev.mokkery.MockMode
import dev.mokkery.mock
import java.nio.file.Paths

abstract class AbstractFileBasedTest : AbstractCircuitTest() {

	protected lateinit var openedCircuitView: GraphView

	protected val actorListener = mock<ActorListener>(MockMode.autofill)

	override fun setup() {
		super.setup()
		configure()
	}

	private fun configure() {
		CurrentApplicationVersion.codeVersion = AbstractDesktopApplication.readCodeVersion()
		CurrentApplicationVersion.dataVersion = AbstractDesktopApplication.readDataVersion()

		val path = Paths.get("jvm/rsc/test").toAbsolutePath()
		//AntaresTestRule.configure()
		LibraryModule.DEF_LIBRARY_UUID = AntaresApplication.DEF_LIBRARY_UUID

		LibraryModule.systemLibraryPersistenceService = ResourceLibraryPersistenceService()
		LibraryModule.systemLibraryDictionaryService = LibraryDictionaryService(ResourceLibraryDictionaryPersistenceService())
		LibraryModule.libraryManagementService = LibraryManagementService()

		ProjectModule.projectDictionaryService = LibraryDictionaryService(FileLibraryDictionaryPersistenceService({ path.toString() }, "projects"))
		ProjectModule.projectLibraryPersistenceService = FileLibraryPersistenceService({ path.toString() }, "projects")
		ProjectModule.projectManagementService = ProjectManagementService()

		GraphModelModule.nonVolatileService = NonVolatileServiceJvm({ path.toString() }, "nonVolatile")

		LibraryModule.libraryHolder.l = LibraryModule.libraryService.loadLibrary(
			LibraryIdentification(LibraryModule.DEF_LIBRARY_UUID, null), isSystem = true)
	}

	override fun getCircuitView(): GraphView = openedCircuitView

	protected fun openCircuit(uuid: UUID) {
		ProjectModule.projectManagementService.open(
			LibraryIdentification(UUID("e70cb564-42c2-4880-baf4-17c507b1526a"), null))
		val metaGraph = LibraryModule.libraryHolder.library.getMetaGraph(uuid)
		openedCircuitView = metaGraph.graph.graphView
	}

	protected fun processUntilQueueIsEmpty() {
		scheduler.proceedUntilQueueIsEmpty(timeService, actorListener)
	}
}