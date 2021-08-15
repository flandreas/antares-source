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
import react.dom.p
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

			p { +"The following explains how a 'Half Adder' works."  }
			graphViewer {
				canvasId = "canvas1"
				metaGraphUuid = UUID("52255dc4-c010-4f6f-8ea6-9c2c8f5f9a82")
				size = Dimension2D(600, 400)
			}

			p { +"The following explains how a 'Full Adder' works."  }
			graphViewer {
				canvasId = "canvas2"
				metaGraphUuid = UUID("08aba425-96c2-4c43-b10b-2e0c72ce8300")
				size = Dimension2D(600, 400)
			}
		}
	}
}