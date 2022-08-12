package ch.scorpion.antares

import ch.scorpion.antares.module.AntaresAkrabPublicModule
import ch.scorpion.antares.view.theme.AntaresThemes
import ch.scorpion.jabbah.base.LogLevel
import ch.scorpion.jabbah.base.LogSystem
import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.edit.auth.AnonymousWebUserHolder
import ch.scorpion.jabbah.edit.auth.EditAuthModule
import ch.scorpion.jabbah.edit.auth.UserIdentity
import ch.scorpion.jabbah.graph.library.LibraryIdentification
import ch.scorpion.jabbah.graph.library.LibraryModule
import ch.scorpion.jabbah.graph.project.AkrabApiError
import ch.scorpion.jabbah.graph.project.AkrabApiException
import ch.scorpion.jabbah.graph.project.ProjectModule
import ch.scorpion.jabbah.graph.ui.graphViewer
import com.ccfraser.muirwik.components.mCssBaseline
import kotlinx.browser.document
import org.w3c.dom.url.URLSearchParams
import react.dom.p
import react.dom.render

class AntaresIFrame {

	companion object {
		private const val PROJECT_UUID_PARAM = "project"
		private const val OWNER_UUID_PARAM = "owner"
		private const val CIRCUIT_UUID_PARAM = "circuit"
		private const val THEME_PARAM = "theme"
	}

	private var error: AkrabApiError? = null

	fun show() {
		val params = URLSearchParams(kotlinx.browser.window.location.search)
		val projectUuid = params.get(PROJECT_UUID_PARAM)
		val ownerUuid = params.get(OWNER_UUID_PARAM)
		val circuitUuid = params.get(CIRCUIT_UUID_PARAM)
		val themeName = params.get(THEME_PARAM)

		if (projectUuid == null) {
			return displayError("Missing parameter '$PROJECT_UUID_PARAM'")
		}
		if (ownerUuid == null) {
			return displayError("Missing parameter '$OWNER_UUID_PARAM'")
		}
		if (circuitUuid == null) {
			return displayError("Missing parameter '$CIRCUIT_UUID_PARAM'")
		}

		initialize(LibraryIdentification(UUID(projectUuid), UserIdentity(ownerUuid)), themeName)
		displayCircuit(circuitUuid)
	}

	private fun initialize(projectId: LibraryIdentification, themeName: String? = null) {
		EditAuthModule.require()
		EditAuthModule.userHolder = AnonymousWebUserHolder

		AntaresAkrabPublicModule.require()
		LibraryModule.libraryHolder.l = LibraryModule.libraryService.loadLibrary(
			LibraryIdentification(LibraryModule.DEF_LIBRARY_UUID, null),
			isSystem = true)

		LogSystem.level = LogLevel.Debug

		loadProject(projectId)

		AntaresThemes.install(themeName)
	}

	private fun loadProject(projectId: LibraryIdentification) {
		try {
			LibraryModule.libraryHolder.l = ProjectModule.projectManagementService.invoke().load(projectId)
		} catch (e: AkrabApiException) {
			error = e.error
		}
	}

	private fun displayError(msg: String) {
		render(document.getElementById("root")) {
			p { +msg }
		}
	}

	private fun displayCircuit(circuitUuid: String) {
		if (error != null) {
			displayError(error!!.msg ?: "Error")
			return
		}
		render(document.getElementById("root")) {
			mCssBaseline()
			graphViewer {
				canvasId = "canvas"
				metaGraphUuid = UUID(circuitUuid)
			}
		}
	}
}