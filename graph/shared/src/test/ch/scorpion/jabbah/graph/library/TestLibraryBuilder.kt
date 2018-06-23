package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.collection.Stack
import ch.scorpion.jabbah.graph.MetaGraph

/** A builder that helps to setup a [LibraryImpl] to be used for testing.*/
class TestLibraryBuilder(
	name: String,
	libraryService: LibraryService
) {

	val library: Library = LibraryImpl(name = name, libraryService = libraryService)
	private val stack = Stack<LibraryDirectory>()

	init {
		stack.push(library)
	}

	/** Steps back from the current [LibraryDirectory].*/
	fun back(): TestLibraryBuilder {
		stack.pop()
		return this
	}

	/** Creates a new [LibraryDirectory] in the current one and makes it the new current one.*/
	fun addDirectory(name: String): TestLibraryBuilder {
		val directory =library.libraryService.addFolder(library, name, stack.peek())
		stack.push(directory)
		return this
	}

	fun addContainerLibraryElement(name: String): TestLibraryBuilder {
		//val elem = ContainerLibraryElement(name = name)
		val metaGraph = MetaGraph()
		metaGraph.graph!!.model!!.name = name
		//elem.updateMetaGraph(metaGraph)
		//stack.peek().add(elem)
		library.libraryService.addContainerLibraryElement(library, metaGraph, stack.peek())
		return this
	}
}