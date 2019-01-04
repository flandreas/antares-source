package ch.scorpion.jabbah.graph.repository

import ch.scorpion.jabbah.base.exception.IllegalStateException
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.edit.CommandManager
import ch.scorpion.jabbah.edit.command.AbstractCommand
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.library.*
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVertice
import ch.scorpion.jabbah.graph.project.ProjectModule
import ch.scorpion.jabbah.graph.project.Project

/**
 * A service for managing the repository, which is the combination of the [Project] and the [Library].
 */
interface RepositoryService {

	/**
	 * Moves the specified [ContainerLibraryElement] to another position, which can be in the same
	 * or in another [Library].
	 *
	 * @param elem the [ContainerLibraryElement] to be moved
	 * @param destination the [LibraryDirectory] to which `elem` is to be moved (can be the same where it is already contained)
	 * @param index the index in `destination` to which `elem` is moved (counting with `elem` still being at its old position).
	 * If `null`, `elem` is moved to the end of `destination`
	 * @throws LibraryDependencyException if a [ContainerLibraryElement] is moved from a [Project] to the
	 * current [Library], but contains [Project] [MetaGraph]s.
	 */
	fun move(elem: ContainerLibraryElement, destination: LibraryDirectory, index: Int? = null)
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

	override fun move(elem: ContainerLibraryElement, destination: LibraryDirectory, index: Int?) {
		val origService = getOwningLibraryService(elem)
		val destService = getOwningLibraryService(destination)

		LOG.debug("RepositoryServiceImpl: moving '${elem.name}' in '${origService.currentLibrary?.name}' "
			+ "to '${destination.name}' at $index in '${destService.currentLibrary?.name}'")

		if (isMoveFromProjectToLibrary(elem, destination)) {
			checkLibraryDependency(elem)
		}

		val origDir = origService.getDirectoryOf(origService.currentLibrary!!, elem)

		var newIndex = index
		if (index != null && origDir == destination && origDir.indexOf(elem) < index) {
			newIndex = index - 1
		}

		commandManager.beginTransaction(MoveRepositoryItemCommand(
			origService = origService,
			destService = destService,
			elem = elem,
			origDir = origDir,
			destDir = destination,
			destPos = newIndex ?: destination.size
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
	private var elem: ContainerLibraryElement,
	private val origDir: LibraryDirectory,
	private val destDir: LibraryDirectory,
	private val destPos: Int
) : AbstractCommand(descriptionKey = "repository.move.name", changesApplicationData = false) {

	private val origPos = origDir.indexOf(elem)

	override fun execute() {
		if (origDir == destDir) {
			origService.move(origService.currentLibrary!!, elem, destPos)
		} else {
			val newElem = destService.addContainerLibraryElement(destService.currentLibrary!!, elem.metaGraph!!, destDir, destPos)
			origService.removeLibraryItem(origService.currentLibrary!!, elem, origDir)
			elem = newElem
		}
	}

	override fun undo() {
		if (origDir == destDir) {
			origService.move(origService.currentLibrary!!, elem, origPos)
		} else {
			val newElem = origService.addContainerLibraryElement(origService.currentLibrary!!, elem.metaGraph!!, origDir, origPos)
			destService.removeLibraryItem(destService.currentLibrary!!, elem, destDir)
			elem = newElem
		}
	}
}