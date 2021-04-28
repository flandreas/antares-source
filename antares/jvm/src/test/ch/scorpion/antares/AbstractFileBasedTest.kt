package ch.scorpion.antares

import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.execution.actor.ActorListener
import ch.scorpion.jabbah.graph.library.*
import ch.scorpion.jabbah.graph.library.dictionary.FileLibraryDictionaryPersistenceService
import ch.scorpion.jabbah.graph.library.dictionary.LibraryDictionaryService
import ch.scorpion.jabbah.graph.library.dictionary.ResourceLibraryDictionaryPersistenceService
import ch.scorpion.jabbah.graph.project.ProjectManagementService
import ch.scorpion.jabbah.graph.project.ProjectModule
import ch.scorpion.jabbah.graph.view.GraphView
import io.mockk.mockk
import java.nio.file.Paths

abstract class AbstractFileBasedTest : AbstractCircuitTest() {

	companion object {

		@JvmStatic
		protected fun configure() {
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

	protected lateinit var openedCircuitView: GraphView

	protected val actorListener = mockk<ActorListener>(relaxed = true)

	override fun getCircuitView(): GraphView = openedCircuitView

	protected fun openCircuit(uuid: UUID) {
		ProjectModule.projectManagementService.open(UUID("e70cb564-42c2-4880-baf4-17c507b1526a"))
		val metaGraph = ProjectModule.projectHolder.p!!.getMetaGraph(uuid)
		openedCircuitView = metaGraph.graph.graphView
	}
}