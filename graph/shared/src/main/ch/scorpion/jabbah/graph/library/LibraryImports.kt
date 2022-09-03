package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.base.collection.DirectedGraph
import ch.scorpion.jabbah.base.collection.TopologicalSort
import ch.scorpion.jabbah.edit.model.text.TranslatableText

/**
 * Contains the transitive hull of imported [Libraries][Library] of a particular root [Library].
 */
class LibraryImports private constructor(
	val root: Library,
	private val graph: DirectedGraph<UUID> = DirectedGraph()
) {

	companion object {

		/**
		 * Calculates the [LibraryImports] of [root] using the specified [LibraryManagementService].
		 */
		fun calculate(
			root: Library,
			service: LibraryManagementService = LibraryModule.libraryManagementService
		): LibraryImports {
			return LibraryImports(root).also {
				calculateImpl(root, service, it)
				it.sort()
			}
		}

		private fun calculateImpl(library: Library, service: LibraryManagementService, imports: LibraryImports) {
			imports.addImport(library)

			for (uuid in library.importedLibraryIds) {
				imports.graph.addNode(uuid)
				imports.graph.addEdge(library.uuid, uuid)
				if (imports._libraries.none { it.uuid == uuid }) {
					val import = service.getOptionalLibrary(uuid)
					if (import == null) {
						imports.incrementStateImportCount()
						LibraryModule.libraryFactory.createEmptyLibrary(
							LibraryProperties(name = TranslatableText(Translations.getString("library.stateReference.name")))
						).also {
							it.isBrokenImport = true
							it.uuid = uuid
							imports.addImport(it)
							imports.graph.addNode(uuid)
							imports.graph.addEdge(library.uuid, uuid)
						}
					} else {
						calculateImpl(import, service, imports)
					}
				}
			}
		}
	}

	/** Contains the transitive hull of imported [Libraries][Library]. */
	val libraries: List<Library> get() = _libraries

	/** Returns the number of referenced [Libraries][Library] not available in the system.*/
	var staleImportCount: Int = 0
		private set

	fun contains(uuid: UUID): Boolean = _libraries.any { it.uuid == uuid }

	private val _libraries = mutableListOf<Library>()

	private fun addImport(library: Library) {
		_libraries.add(library)
		graph.addNode(library.uuid)
	}

	private fun incrementStateImportCount() {
		staleImportCount++
	}

	private fun sort() {
		val sortedLibraries = TopologicalSort
			.sort(graph)
			.mapNotNull { uuid -> _libraries.firstOrNull { it.uuid == uuid } }
			.toList()

		_libraries.clear()
		_libraries.addAll(sortedLibraries)
	}
}