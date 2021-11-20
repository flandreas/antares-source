package ch.scorpion.antares

import ch.scorpion.antares.AntaresApplication.Companion.DEF_LIBRARY_UUID
import ch.scorpion.antares.module.AntaresModuleJs
import ch.scorpion.antares.ui.AntaresViewJs
import ch.scorpion.antares.view.theme.AntaresThemes
import ch.scorpion.jabbah.app.*
import ch.scorpion.jabbah.base.LogLevel
import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.edit.auth.EditAuthModule
import ch.scorpion.jabbah.edit.auth.User
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.library.LibraryModule
import ch.scorpion.jabbah.graph.project.ProjectModule
import ch.scorpion.jabbah.graph.ui.GraphDataViewController
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.url.URLSearchParams
import react.dom.*

/**
 * Displays the Antares Editor Workbench as a standalone React application.
 * Specify the UUID of the project to display in the URL's query parameter "project".
 */
class AntaresJs : AbstractApplicationJs(GraphDataViewController()), AntaresApplication {

	companion object {
		private const val PROJECT_UUID_PARAM = "project"
		private const val RETURN_URI_PARAM = "returnUrl"
	}

	override val logLevel get() = LogLevel.Info

	override fun init() {
		console.info("Initializing AntaresJs")

		AntaresModuleJs.require()

		EditAuthModule.userHolder.u = User.developer
		LibraryModule.libraryHolder.l = LibraryModule.libraryService.loadLibrary(DEF_LIBRARY_UUID, isSystem = true)

		AntaresThemes.install()

		super.init()
	}

	override fun start() {
		init()
		display()
	}

	override fun openInitialSavable() {
		extractProjectUuidFromUrl()?.let { projectUuid ->
			(controller as GraphDataViewController).openProject(projectUuid)

			// The above would normally result in triggering OpenContainerLibraryElementAction,
			// which is part of LibraryTreeView. In JS, LibraryTreeView has not yet been created at this point,
			// therefore we have to load the default circuit below

			ProjectModule.projectHolder.project!!.run {
				defaultElementUUID?.let { uuid ->
					val element = getContainerLibraryElement(uuid)
					controller.openAsSavable(element!!, "Initial circuit")
				}
			}
		}
	}

	private fun extractProjectUuidFromUrl(): UUID? {
		val params = URLSearchParams(window.location.search)
		return params.get(PROJECT_UUID_PARAM)?.let { UUID(it) }
	}

	private fun extractReturnUriFromUrl(): String? {
		val params = URLSearchParams(window.location.search)
		return params.get(RETURN_URI_PARAM)
	}

	private fun display() {
		render(document.getElementById("root")) {
			child(AntaresViewJs::class) {
				attrs.application = this@AntaresJs
				attrs.applicationDataHolder = controller
				attrs.canvasId = "kotlinCanvas"
				attrs.size = null
				attrs.projectName = ProjectModule.projectHolder.project?.name?.getTranslation()
				attrs.returnUri = extractReturnUriFromUrl()
				attrs.metaGraph = controller.data?.content as MetaGraph?
			}
		}
	}
}