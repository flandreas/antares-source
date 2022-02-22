package ch.scorpion.antares

import ch.scorpion.antares.AntaresApplication.Companion.DEF_LIBRARY_UUID
import ch.scorpion.antares.module.AntaresAkrabProtectedModuleJs
import ch.scorpion.antares.ui.AntaresViewJs
import ch.scorpion.antares.view.theme.AntaresThemes
import ch.scorpion.jabbah.app.AbstractApplicationJs
import ch.scorpion.jabbah.app.Application
import ch.scorpion.jabbah.app.ApplicationDataHolder
import ch.scorpion.jabbah.base.LogLevel
import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.base.auth0.Auth0Provider
import ch.scorpion.jabbah.base.auth0.useAuth0
import ch.scorpion.jabbah.edit.auth.AnonymousWebUserHolder
import ch.scorpion.jabbah.edit.auth.EditAuthModule
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.library.AkrabRestLibraryPersistenceServiceJs
import ch.scorpion.jabbah.graph.library.LibraryModule
import ch.scorpion.jabbah.graph.project.ProjectModule
import ch.scorpion.jabbah.graph.ui.GraphDataViewController
import com.ccfraser.muirwik.components.mTypography
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.await
import kotlinx.coroutines.launch
import org.w3c.dom.url.URLSearchParams
import react.Props
import react.dom.render
import react.fc
import react.useEffectOnce
import react.useState

val mainScope = MainScope()

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

		EditAuthModule.require()
		EditAuthModule.userHolder = AnonymousWebUserHolder

		AntaresAkrabProtectedModuleJs.require()
		LibraryModule.libraryHolder.l = LibraryModule.libraryService.loadLibrary(DEF_LIBRARY_UUID, isSystem = true)

		AntaresThemes.install()

		super.init()
	}

	override fun start() {
		init()
		display()
	}

	override fun openInitialSavable() {
		// This cannot run before the Auth0 token has been read and establish in the service.
		// Reading the Auth0 token must be done from within a hook.
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

			child(Auth0Provider) {
				attrs {
					domain = "dev-wq7i977v.eu.auth0.com"
					clientId = "mYdmErbSZxQUtlr9BW2UHUOmxtHN8WNO"
					audience = "https://antarescircuit.io/api"
				}
				child(antaresJs) {
					attrs {
						application = this@AntaresJs
						applicationDataHolder = controller
						projectUuid = extractProjectUuidFromUrl()
						returnUri = extractReturnUriFromUrl()
						metaGraph = controller.data?.content as MetaGraph?
					}
				}
			}
		}
	}
}

interface AntaresJsProps : Props {
	var application: Application
	var applicationDataHolder: ApplicationDataHolder
	var projectUuid: UUID?
	var returnUri: String?
	var metaGraph: MetaGraph?
}

val antaresJs = fc<AntaresJsProps> { props ->
	val auth0 = useAuth0()
	var accessToken: String? by useState(null)

	useEffectOnce {
		mainScope.launch {
			accessToken = auth0.getAccessTokenSilently().await()
		}
	}

	if (accessToken == null) {
		mTypography("Loading..")
	} else {
		(ProjectModule.projectLibraryPersistenceService as AkrabRestLibraryPersistenceServiceJs).accessToken = accessToken!!

		props.projectUuid?.let {
			(props.application.controller as GraphDataViewController).openProject(it)

			// TODO Open defaultElement
			/*
			// The above would normally result in triggering OpenContainerLibraryElementAction,
			// which is part of LibraryTreeView. In JS, LibraryTreeView has not yet been created at this point,
			// therefore we have to load the default circuit below

			ProjectModule.projectHolder.project!!.run {
				defaultElementUUID?.let { uuid ->
					val element = getContainerLibraryElement(uuid)
					controller.openAsSavable(element!!, "Initial circuit")
				}
			}
			*/
		}

		child(AntaresViewJs::class) {
			attrs.application = props.application
			attrs.applicationDataHolder = props.applicationDataHolder
			attrs.canvasId = "kotlinCanvas"
			attrs.size = null
			attrs.projectName = ProjectModule.projectHolder.project?.name?.getTranslation()
			attrs.returnUri = props.returnUri
			attrs.metaGraph = props.metaGraph
		}
	}
}