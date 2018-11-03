package ch.scorpion.jabbah.graph.project

import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.library.Library
import ch.scorpion.jabbah.graph.library.LibraryImpl
import ch.scorpion.jabbah.graph.library.LibraryService
import ch.scorpion.jabbah.io.IOModule
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StorableCreator
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

interface Project : Library {

	/**
	 * The [UUID] of the [Library] imported by this [Library], i.e. the [Library] from which this [Library]
	 * imports [MetaGraph]s. This is currently only used by projects.
	 */
	val importedLibrary: UUID?
}

class ProjectImpl(
	name: String = "",
	libraryService: LibraryService = ProjectModule.projectLibraryService.invoke(),
	storableCreator: StorableCreator = IOModule.storableCreator,
	descriptionKey: String = "library.library.name"
) : LibraryImpl(
	name = name,
	libraryService = libraryService,
	storableCreator = storableCreator,
	descriptionKey = descriptionKey
), Project {

	override var importedLibrary: UUID? = null

	/** ---- [Storable] interface */

	override fun read(reader: StoreReader) {
		super.read(reader)
		if (reader.hasAttribute("import")) {
			importedLibrary = System.get().createUUID(reader.readString("import"))
		}
	}

	override fun write(writer: StoreWriter) {
		super.write(writer)
		if (importedLibrary != null) {
			writer.writeString("import", importedLibrary.toString())
		}
	}
}