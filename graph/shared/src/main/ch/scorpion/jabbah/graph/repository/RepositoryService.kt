package ch.scorpion.jabbah.graph.repository

import ch.scorpion.jabbah.base.exception.IllegalStateException
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.graph.library.*
import ch.scorpion.jabbah.graph.project.ProjectModule

/**
 * A service for managing the repository, which is the combination of the [Project] and the [Library].
 */
interface RepositoryService {

	/**
	 * Moves the specified [ContainerLibraryElement] to another [LibraryDirectory], which can be in the same
	 * or in another [Library].
	 */
	fun move(elem: ContainerLibraryElement, destination: LibraryDirectory)
}

/** Standard implementation of the [RepositoryService] interface.*/
class RepositoryServiceImpl(
	private val libraryService: LibraryService = LibraryModule.libraryService.invoke(),
	private val projectLibraryService: LibraryService = ProjectModule.projectLibraryService.invoke()
) : RepositoryService {

	companion object {
		private val LOG by logger(RepositoryServiceImpl::class)
	}

	/** ---- [RepositoryServiceImpl] interface */

	override fun move(elem: ContainerLibraryElement, destination: LibraryDirectory) {
		val origService = getOwningLibraryService(elem)
		val destService = getOwningLibraryService(destination)

		// TODO Use Command to make undoable

		LOG.debug("RepositoryServiceImpl: moving '${elem.name}' in '${origService.currentLibrary?.name}' "
			+ "to '${destination.name}' in '${destService.currentLibrary?.name}'")

		val origDirectory = origService.getDirectoryOf(origService.currentLibrary!!, elem)

		destService.addContainerLibraryElement(destService.currentLibrary!!, elem.metaGraph!!, destination)
		origService.removeLibraryItem(origService.currentLibrary!!, elem, origDirectory)
	}

	/** ---- [RepositoryServiceImpl] */

	/**
	 * Returns the [LibraryService] to be used for accessing the [Library] that currently
	 * owns the specified [LibraryItem].
	 * @throws IllegalStateException if neither the current [Library] nor the current [Project] owns `item`
	 */
	private fun getOwningLibraryService(item: LibraryItem): LibraryService {
		if (item.library == null) {
			throw IllegalStateException("item doesn't belong to a Library")
		}
		if (item.library === libraryService.currentLibrary) {
			return libraryService
		}
		if (item.library === projectLibraryService.currentLibrary) {
			return projectLibraryService
		}
		throw IllegalStateException("neither current Library nor current Project own item")
	}
}