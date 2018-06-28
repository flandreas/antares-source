package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.app.Savable
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.project.ProjectModule

/**
 * Saves the edited [MetaGraph] of a [ContainerLibraryElement] in the containing [LibraryDirectory].
 */
abstract class AbstractLibrarySavable(
	val metaGraph: MetaGraph,
	val element: ContainerLibraryElement,
	val libraryService: LibraryService = ProjectModule.projectLibraryService.invoke()
) : Savable {

	/** ---- [Savable] */

	override val defined: Boolean get() = true

	override val supportsMostRecent: Boolean get() = true

}