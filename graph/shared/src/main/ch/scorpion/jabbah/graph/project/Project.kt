package ch.scorpion.jabbah.graph.project

import ch.scorpion.jabbah.app.Savable
import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.graph.library.ContainerLibraryElement
import ch.scorpion.jabbah.graph.library.Library
import ch.scorpion.jabbah.graph.library.LibraryImpl
import ch.scorpion.jabbah.graph.library.LibraryService
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader

interface Project : Library

class ProjectImpl(
	name: TranslatableText = TranslatableText(),
	description: TranslatableText = TranslatableText(),
	libraryService: LibraryService = ProjectModule.projectLibraryService,
	objectTypeKey: String = "project.project.name"
) : LibraryImpl(
	name = name,
	description = description,
	libraryService = libraryService,
	objectTypeKey = objectTypeKey
), Project {

	constructor(name: String, description: String, libraryService: LibraryService)
		: this(TranslatableText(name), TranslatableText(description), libraryService)

	/** ---- [Storable] interface */

	override fun read(reader: StoreReader) {
		super.read(reader)
		if (reader.hasAttribute("import")) {
			// Backward compatibility since feature #212 "Multi-Lib"
			addImport(System.createUUID(reader.readString("import")))
		}
	}

	/** ---- [LibraryImpl] */

	override fun createSavable(element: ContainerLibraryElement): Savable = ProjectSavable(element)
}