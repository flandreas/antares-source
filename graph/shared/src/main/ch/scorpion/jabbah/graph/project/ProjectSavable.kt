package ch.scorpion.jabbah.graph.project

import ch.scorpion.jabbah.app.Application
import ch.scorpion.jabbah.app.Savable
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.library.*

/**
 * Saves the edited [MetaGraph] of a [ContainerLibraryElement] in the containing [LibraryDirectory].
 */
class ProjectSavable(
	val metaGraph: MetaGraph,
	val element: ContainerLibraryElement,
	private val project: Project = ProjectModule.projectHolder.project!!,
	private val libraryService: LibraryService = ProjectModule.projectLibraryService.invoke(),
	private val projectService: ProjectService = ProjectModule.projectService
) : Savable {

	override val description: String
		get() = "${Translations.getString("project.savable.prefix")} \"${element.name}\""

	override val defined: Boolean get() = true

	override val supportsMostRecent: Boolean get() = true

	override fun open(application: Application): Boolean {
		projectService.open(project.name, element.uuid)
		return true
	}

	override fun save(application: Application): Boolean {
		libraryService.updateContainerLibraryElement(project, metaGraph, element)
		application.savable = this
		return true
	}
}