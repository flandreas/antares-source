package ch.scorpion.jabbah.graph.library

/**
 * Contains the transitive hull of imported [Libraries][Library] of a particular root [Library].
 */
class LibraryImports(val root: Library) {

	companion object {

		/**
		 * Calculates the [LibraryImports] of [root] using the specified [LibraryManagementService].
		 */
		fun calculate(
			root: Library,
			service: LibraryManagementService = LibraryModule.libraryManagementService
		): LibraryImports =
			LibraryImports(root).also { calculateImpl(root, service, it) }

		private fun calculateImpl(library: Library, service: LibraryManagementService, imports: LibraryImports) {
			imports.addImport(library)
			for (uuid in library.importedLibraryIds) {
				if (imports._libraries.none { it.uuid == uuid }) {
					val import = service.getOptionalLibrary(uuid)
					if (import == null) {
						imports.incrementStateImportCount()
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

	private val _libraries = mutableListOf<Library>()

	private fun addImport(library: Library) {
		_libraries.add(library)
	}

	private fun incrementStateImportCount() {
		staleImportCount++
	}
}