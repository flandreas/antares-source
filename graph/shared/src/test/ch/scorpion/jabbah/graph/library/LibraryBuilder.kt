package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.collection.Stack
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.edit.model.text.description.Name
import ch.scorpion.jabbah.graph.MetaGraph

/** A builder that helps to setup a [LibraryImpl] to be used for testing.*/
class LibraryBuilder(
	name: String,
	libraryService: LibraryService,
	val library: Library = LibraryImpl(name = TranslatableText(name), libraryService = libraryService)
) {

	private val stack = Stack<LibraryDirectory>()

	init {
		stack.push(library.directory)
	}

	/** Steps back from the current [LibraryDirectory].*/
	fun back(): LibraryBuilder {
		stack.pop()
		return this
	}

	fun peek(): LibraryDirectory = stack.peek()

	/** Creates a new [LibraryDirectory] in the current one and makes it the new current one.*/
	fun addDirectory(name: String): LibraryBuilder {
		val directory =library.libraryService.addFolder(library, TranslatableText(name), stack.peek())
		stack.push(directory)
		return this
	}

	/** Creates a new [MetaGraph] for a model with the given name and adds it to the current [LibraryDirectory].*/
	fun addContainerLibraryElement(name: String): LibraryBuilder {
		val metaGraph = MetaGraph()
		metaGraph.graph.model!!.name = Name(name)
		return addContainerLibraryElement(metaGraph)
	}

	/** Adds the specified [MetaGraph] to the current [LibraryDirectory].*/
	fun addContainerLibraryElement(metaGraph: MetaGraph): LibraryBuilder {
		library.libraryService.addContainerLibraryElement(library, metaGraph, stack.peek())
		return this
	}
}