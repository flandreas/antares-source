package ch.scorpion.jabbah.graph.project

import ch.scorpion.jabbah.app.Application
import ch.scorpion.jabbah.app.ApplicationDataViewController
import ch.scorpion.jabbah.app.Savable
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.library.AbstractContainerLibraryElementSavable
import ch.scorpion.jabbah.graph.library.ContainerLibraryElement
import ch.scorpion.jabbah.graph.library.LibraryDirectory
import ch.scorpion.jabbah.graph.library.LibraryModule

/**
 * Saves the edited [MetaGraph] of a [ContainerLibraryElement] in the containing [LibraryDirectory].
 */
class ProjectSavable(
	element: ContainerLibraryElement,
	private val projectManagementService: ProjectManagementService = ProjectModule.projectManagementService
) : AbstractContainerLibraryElementSavable(element) {

	val project: Project get() = element.library as Project

	/** ---- [Any] */

	override fun equals(other: Any?): Boolean {
		if (other !is ProjectSavable) {
			return false
		}
		if (element.library == null) {
			return false
		}
		return element.uuid == other.element.uuid
	}

	/** ---- [Savable] */

	override val description: String get() = "${Translations.getString("project.savable.prefix")} \"${element.name.value}\""

	override fun open(application: Application): Boolean {
		if (element.library == null) {
			// Library has been disposed in the meantime
			if (LibraryModule.libraryHolder.l == null) {
				return false
			}
			LibraryModule.libraryHolder.getContainerLibraryElement(element.uuid)?.let {
				projectManagementService.open(LibraryModule.libraryHolder.library.identification, element.uuid)
				return true
			} ?: return false
		}

		projectManagementService.open(project.identification, element.uuid)
		return true
	}

	override fun save(appDataViewController: ApplicationDataViewController): Boolean {
		project.libraryService.updateContainerLibraryElement(project, appDataViewController.data!!.content as MetaGraph, element)
		appDataViewController.data = appDataViewController.data!!.withSavable(this)
		return true
	}
}