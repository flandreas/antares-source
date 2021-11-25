package ch.scorpion.antares

import ch.scorpion.antares.module.AntaresModuleJs
import ch.scorpion.antares.view.theme.AntaresThemes
import ch.scorpion.jabbah.base.LogLevel
import ch.scorpion.jabbah.base.LogSystem
import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.edit.auth.EditAuthModule
import ch.scorpion.jabbah.edit.auth.User
import ch.scorpion.jabbah.graph.library.LibraryModule
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
		private const val CIRCUIT_UUID_PARAM = "circuit"
		private const val THEME_PARAM = "theme"
	}

	fun show() {
		val params = URLSearchParams(kotlinx.browser.window.location.search)
		val projectUuid = params.get(PROJECT_UUID_PARAM)
		val circuitUuid = params.get(CIRCUIT_UUID_PARAM)
		val themeName = params.get(THEME_PARAM)

		if (projectUuid == null) {
			return displayError("Missing parameter '$PROJECT_UUID_PARAM'")
		}
		if (circuitUuid == null) {
			return displayError("Missing parameter '$CIRCUIT_UUID_PARAM'")
		}

		initialize(projectUuid, themeName)
		displayCircuit(circuitUuid)
	}

	private fun initialize(projectUuid: String, themeName: String? = null) {
		AntaresModuleJs.require()
		EditAuthModule.userHolder.u = User.developer
		LibraryModule.libraryHolder.l = LibraryModule.libraryService.loadLibrary(AntaresApplication.DEF_LIBRARY_UUID, isSystem = true)

		loadProject(projectUuid)

		AntaresThemes.install(themeName)
		LogSystem.level = LogLevel.Info
	}

	private fun loadProject(projectUuid: String) {
		ProjectModule.projectHolder.p = ProjectModule.projectManagementService.invoke().load(UUID(projectUuid))
	}

	private fun displayError(msg: String) {
		render(document.getElementById("root")) {
			p { +msg }
		}
	}

	private fun displayCircuit(circuitUuid: String) {
		render(document.getElementById("root")) {
			mCssBaseline()
			graphViewer {
				canvasId = "canvas"
				metaGraphUuid = UUID(circuitUuid)
			}
		}
	}
}