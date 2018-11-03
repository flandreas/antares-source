package ch.scorpion.jabbah.graph.project

import ch.scorpion.jabbah.graph.library.Library
import ch.scorpion.jabbah.graph.library.LibraryImpl
import ch.scorpion.jabbah.graph.library.LibraryService
import ch.scorpion.jabbah.io.IOModule
import ch.scorpion.jabbah.io.StorableCreator

typealias Project = Library

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
)