package ch.scorpion.jabbah.graph.project

import ch.scorpion.jabbah.app.Savable
import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.library.ContainerLibraryElement
import ch.scorpion.jabbah.graph.library.Library
import ch.scorpion.jabbah.graph.library.LibraryImpl
import ch.scorpion.jabbah.graph.library.LibraryService
import ch.scorpion.jabbah.io.*

interface Project : Library {

	/**
	 * The [UUID] of the [Library] imported by this [Project], i.e. the [Library] from which this [Project]
	 * imports [MetaGraph]s. This is currently only used by [Project]s, since [Libraries][Library] cannot
	 * yet be based on other [Libraries][Library].
	 */
	var importedLibrary: UUID?
}

class ProjectImpl(
	name: TranslatableText = TranslatableText(),
	description: TranslatableText = TranslatableText(),
	libraryService: LibraryService = ProjectModule.projectLibraryService.invoke(),
	storableCreator: StorableCreator = IOModule.storableCreator,
	objectTypeKey: String = "project.project.name"
) : LibraryImpl(
	name = name,
	description = description,
	libraryService = libraryService,
	storableCreator = storableCreator,
	objectTypeKey = objectTypeKey
), Project {

	constructor(name: String, description: String, libraryService: LibraryService)
		: this(TranslatableText(name), TranslatableText(description), libraryService)

	override var importedLibrary: UUID? = null

	/** ---- [Storable] interface */

	override fun read(reader: StoreReader) {
		super.read(reader)
		if (reader.hasAttribute("import")) {
			importedLibrary = System.createUUID(reader.readString("import"))
		}
	}

	override fun write(writer: StoreWriter) {
		super.write(writer)
		if (importedLibrary != null) {
			writer.writeString("import", importedLibrary.toString())
		}
	}

	/** ---- [LibraryImpl] */

	override fun createSavable(element: ContainerLibraryElement): Savable = ProjectSavable(element)
}