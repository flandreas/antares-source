package ch.scorpion.antares.filebased

import ch.scorpion.antares.AbstractCircuitTest
import ch.scorpion.antares.AntaresApplication
import ch.scorpion.jabbah.app.AbstractDesktopApplication
import ch.scorpion.jabbah.app.CurrentApplicationVersion
import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.execution.actor.ActorListener
import ch.scorpion.jabbah.graph.library.*
import ch.scorpion.jabbah.graph.library.dictionary.FileLibraryDictionaryPersistenceService
import ch.scorpion.jabbah.graph.library.dictionary.LibraryDictionaryService
import ch.scorpion.jabbah.graph.library.dictionary.ResourceLibraryDictionaryPersistenceService
import ch.scorpion.jabbah.graph.model.module.GraphModelModule
import ch.scorpion.jabbah.graph.model.nonvolatile.NonVolatileServiceJvm
import ch.scorpion.jabbah.graph.project.ProjectManagementService
import ch.scorpion.jabbah.graph.project.ProjectModule
import ch.scorpion.jabbah.graph.view.GraphView
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
		CurrentApplicationVersion.version = AbstractDesktopApplication.readVersion()

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