package io.antarescircuit.jabbah.graph.project

import io.antarescircuit.jabbah.app.Savable
import io.antarescircuit.jabbah.base.System
import io.antarescircuit.jabbah.edit.model.text.TranslatableText
import io.antarescircuit.jabbah.graph.library.ContainerLibraryElement
import io.antarescircuit.jabbah.graph.library.Library
import io.antarescircuit.jabbah.graph.library.LibraryImpl
import io.antarescircuit.jabbah.graph.library.LibraryService
import io.antarescircuit.jabbah.io.Storable
import io.antarescircuit.jabbah.io.StoreReader

interface Project : Library

class ProjectImpl(
	name: TranslatableText = TranslatableText(),
	description: TranslatableText = TranslatableText(),
	objectTypeKey: String = "project.project.name"
) : LibraryImpl(
	name = name,
	description = description,
	objectTypeKey = objectTypeKey
), Project {

	constructor(name: String, description: String) : this(TranslatableText(name), TranslatableText(description))

	override val libraryService: LibraryService get() = ProjectModule.projectLibraryService

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