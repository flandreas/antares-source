package ch.scorpion.antares

import ch.scorpion.antares.module.AntaresModuleJs
import ch.scorpion.antares.view.theme.AntaresThemes
import ch.scorpion.jabbah.base.LogLevel
import ch.scorpion.jabbah.base.LogSystem
import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.base.geom.Dimension2D
import ch.scorpion.jabbah.edit.auth.EditAuthModule
import ch.scorpion.jabbah.edit.auth.User
import ch.scorpion.jabbah.graph.library.LibraryModule
import ch.scorpion.jabbah.graph.ui.GraphViewerJs
import ch.scorpion.jabbah.graph.ui.graphViewer
import kotlinx.browser.document
import react.dom.h2
import react.dom.render

/** A React application displaying possible multiple [GraphViewerJs].*/
class AntaresPage {

	fun show() {
		initialize()
		display()
	}

	private fun initialize() {
		console.info("Initializing AntaresPage")

		AntaresModuleJs.require()

		EditAuthModule.userHolder.u = User.developer
		LibraryModule.libraryHolder.l = LibraryModule.libraryService.loadLibrary(AntaresApplication.DEF_LIBRARY_UUID, isSystem = true)

		AntaresThemes.install()

		LogSystem.level = LogLevel.Info
	}

	private fun display() {
		render(document.getElementById("root")) {
			h2 { +"This is AntaresPage" }
			graphViewer {
				canvasId = "canvas1"
				metaGraphUuid = UUID("52255dc4-c010-4f6f-8ea6-9c2c8f5f9a82")
				size = Dimension2D(300, 200)
			}
		}
	}
}