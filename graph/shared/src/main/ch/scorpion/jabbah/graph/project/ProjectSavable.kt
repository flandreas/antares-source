package ch.scorpion.jabbah.graph.project

import ch.scorpion.jabbah.app.Application
import ch.scorpion.jabbah.app.ApplicationDataViewController
import ch.scorpion.jabbah.app.Savable
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.edit.auth.Authorizer
import ch.scorpion.jabbah.edit.auth.Operation.Change
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.library.AbstractContainerLibraryElementSavable
import ch.scorpion.jabbah.graph.library.ContainerLibraryElement
import ch.scorpion.jabbah.graph.library.LibraryDirectory
import ch.scorpion.jabbah.graph.library.LibraryService

/**
 * Saves the edited [MetaGraph] of a [ContainerLibraryElement] in the containing [LibraryDirectory].
 */
class ProjectSavable(
	element: ContainerLibraryElement,
	val project: Project = ProjectModule.projectHolder.project!!,
	libraryService: LibraryService = ProjectModule.projectLibraryService.invoke(),
	private val projectManagementService: ProjectManagementService = ProjectModule.projectManagementService.invoke()
) : AbstractContainerLibraryElementSavable(element, libraryService) {

	/** ---- [Any] */

	override fun equals(other: Any?): Boolean {
		if (other !is ProjectSavable) {
			return false
		}
		return project.name == other.project.name && element.uuid == other.element.uuid
	}

	/** ---- [Savable] */

	override val description: String get() = "${Translations.getString("project.savable.prefix")} \"${element.name.value}\""

	override val editable: Boolean get() = Authorizer.isCurrentUserAuthorizedTo(Change, project)

	override fun open(application: Application): Boolean {
		projectManagementService.open(project.identification, element.uuid)
		return true
	}

	override fun save(appDataViewController: ApplicationDataViewController): Boolean {
		libraryService.updateContainerLibraryElement(project, appDataViewController.data!!.content as MetaGraph, element)
		appDataViewController.data = appDataViewController.data!!.withSavable(this)
		return true
	}
}