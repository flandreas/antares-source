package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.app.Savable
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.project.ProjectModule

/**
 * Saves the edited [MetaGraph] of a [ContainerLibraryElement] in the containing [LibraryDirectory].
 */
abstract class AbstractContainerLibraryElementSavable(
	element: ContainerLibraryElement,
	val libraryService: LibraryService = ProjectModule.projectLibraryService.invoke()
) : AbstractLibraryItemSavable(element) {

	val element: ContainerLibraryElement get() = item as ContainerLibraryElement

	/** ---- [Savable] */

	override val supportsMostRecent: Boolean get() = true
}