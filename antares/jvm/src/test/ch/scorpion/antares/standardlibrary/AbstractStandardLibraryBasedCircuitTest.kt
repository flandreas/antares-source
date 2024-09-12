package ch.scorpion.antares.standardlibrary

import ch.scorpion.antares.AbstractCircuitTest
import ch.scorpion.antares.AntaresApplication
import ch.scorpion.jabbah.graph.library.*
import ch.scorpion.jabbah.graph.library.dictionary.LibraryDictionaryService
import ch.scorpion.jabbah.graph.library.dictionary.ResourceLibraryDictionaryPersistenceService
import ch.scorpion.jabbah.graph.view.GraphView

/**
 * Base class for simulation tests of circuits using components from
 * Antares' standard library.
 */
abstract class AbstractStandardLibraryBasedCircuitTest : AbstractCircuitTest() {

	companion object {
		fun setupLibrary() {
			LibraryModule.DEF_LIBRARY_UUID = AntaresApplication.DEF_LIBRARY_UUID
			LibraryModule.systemLibraryPersistenceService = ResourceLibraryPersistenceService()
			LibraryModule.systemLibraryDictionaryService = LibraryDictionaryService(
				ResourceLibraryDictionaryPersistenceService()
			)
			LibraryModule.libraryManagementService = LibraryManagementService()

			LibraryModule.libraryHolder.l = LibraryModule.libraryService.loadLibrary(
				LibraryIdentification(LibraryModule.DEF_LIBRARY_UUID, null), isSystem = true)
		}
	}

	private lateinit var _circuitView: GraphView

	protected abstract fun createCircuit(): GraphView

	override fun getCircuitView(): GraphView = _circuitView

	override fun setup() {
		super.setup()
		setupLibrary()
		_circuitView = createCircuit()
	}
}