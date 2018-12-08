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
	element: ContainerLibraryElement,
	val project: Project = ProjectModule.projectHolder.project!!,
	libraryService: LibraryService = ProjectModule.projectLibraryService.invoke(),
	private val projectManagementService: ProjectManagementService = ProjectModule.projectManagementService
) : AbstractLibrarySavable(element, libraryService) {

	/** ---- [Any] */

	override fun equals(other: Any?): Boolean {
		if (other !is ProjectSavable) {
			return false
		}
		return project.name == other.project.name && element.uuid == other.element.uuid
	}

	/** ---- [Savable] */

	override val description: String get() = "${Translations.getString("project.savable.prefix")} \"${element.name}\""

	override val readOnly: Boolean get() = false

	override fun open(application: Application): Boolean {
		projectManagementService.open(project.name, element.uuid)
		return true
	}

	override fun save(application: Application): Boolean {
		libraryService.updateContainerLibraryElement(project, element)
		application.savable = this
		return true
	}
}