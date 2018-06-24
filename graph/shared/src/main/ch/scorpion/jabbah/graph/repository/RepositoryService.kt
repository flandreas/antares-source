package ch.scorpion.jabbah.graph.repository

import ch.scorpion.jabbah.base.exception.IllegalStateException
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.edit.CommandManager
import ch.scorpion.jabbah.edit.command.AbstractCommand
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.graph.library.*
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVertice
import ch.scorpion.jabbah.graph.project.ProjectModule

/**
 * A service for managing the repository, which is the combination of the [Project] and the [Library].
 */
interface RepositoryService {

	/**
	 * Moves the specified [ContainerLibraryElement] to another [LibraryDirectory], which can be in the same
	 * or in another [Library].
	 * @throws LibraryDependencyException if a [ContainerLibraryElement] is moved from a [Project] to the
	 * current [Library], but contains [Project] [MetaGraph]s.
	 */
	fun move(elem: ContainerLibraryElement, destination: LibraryDirectory)
}

/** Indicates that an operation would result in a [Library] containing elements that depend on a [Project].*/
class LibraryDependencyException(val subGraphVertice: SubGraphVertice) : Throwable()

/** Standard implementation of the [RepositoryService] interface.*/
class RepositoryServiceImpl(
	private val libraryService: LibraryService = LibraryModule.libraryService.invoke(),
	private val projectLibraryService: LibraryService = ProjectModule.projectLibraryService.invoke(),
	private val commandManager: CommandManager = EditModule.commandManager
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

		if (isMoveFromProjectToLibrary(elem, destination)) {
			checkLibraryDependency(elem)
		}

		val origDir = origService.getDirectoryOf(origService.currentLibrary!!, elem)

		commandManager.beginTransaction(MoveRepositoryItemCommand(
			origService = origService,
			destService = destService,
			elem = elem,
			origDir = origDir,
			origPos = origDir.indexOf(elem),
			destDir = destination
		))
		commandManager.commitTransaction()
	}

	/**
	 * Checks if `elem` contains a [SubGraphVertice] that references a [Project] [MetaGraph].
	 * @throws LibraryDependencyException if it does
	 */
	private fun checkLibraryDependency(elem: ContainerLibraryElement) {
		val projectSubGraphVertice = elem.metaGraph!!.graph.model!!.elements
			.filter { it is SubGraphVertice }
			.map { it as SubGraphVertice }
			.firstOrNull { projectLibraryService.currentLibrary!!.containsMetaGraph(it.graphUUID!!) }
		if (projectSubGraphVertice != null) {
			throw LibraryDependencyException(projectSubGraphVertice)
		}
	}

	private fun isMoveFromProjectToLibrary(elem: ContainerLibraryElement, destination: LibraryDirectory): Boolean =
		elem.library === projectLibraryService.currentLibrary && destination.library === libraryService.currentLibrary

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

class MoveRepositoryItemCommand(
	private val origService: LibraryService,
	private val destService: LibraryService,
	elem: ContainerLibraryElement,
	private val origDir: LibraryDirectory,
	private val origPos: Int,
	private val destDir: LibraryDirectory
) : AbstractCommand("repository.move.name") {

	private var elem: ContainerLibraryElement = elem

	override fun execute() {
		val newElem = destService.addContainerLibraryElement(destService.currentLibrary!!, elem.metaGraph!!, destDir)
		origService.removeLibraryItem(origService.currentLibrary!!, elem, origDir)
		elem = newElem
	}

	override fun undo() {
		val newElem = origService.addContainerLibraryElement(origService.currentLibrary!!, elem.metaGraph!!, origDir, origPos)
		destService.removeLibraryItem(destService.currentLibrary!!, elem, destDir)
		elem = newElem
	}
}