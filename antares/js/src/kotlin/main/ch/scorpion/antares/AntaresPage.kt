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
import ch.scorpion.jabbah.graph.project.ProjectModule
import ch.scorpion.jabbah.graph.ui.GraphViewerJs
import ch.scorpion.jabbah.graph.ui.graphViewer
import com.ccfraser.muirwik.components.MTypographyVariant
import com.ccfraser.muirwik.components.mContainer
import com.ccfraser.muirwik.components.mTypography
import com.ccfraser.muirwik.components.styles.Breakpoint
import kotlinx.browser.document
import kotlinx.css.Color
import kotlinx.css.backgroundColor
import react.dom.h2
import react.dom.p
import react.dom.render
import styled.css

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
		loadProject()

		AntaresThemes.install()

		LogSystem.level = LogLevel.Info
	}

	private fun loadProject() {
		val projectUuid = UUID("532f0477-722c-4c88-ada3-c419a386d06a")
		ProjectModule.projectHolder.p = ProjectModule.projectManagementService.load(projectUuid)
	}

	private fun display() {
		render(document.getElementById("root")) {
			mContainer(maxWidth = Breakpoint.md) {
				css {
					backgroundColor = Color.white
					//backgroundColor = kotlinx.css.Color("#dcedfa")
				}
				//h2 { +"This is AntaresPage" }
				mTypography("Binary Addition", variant = MTypographyVariant.h3)

				//p { +"This is a circuit from the project"  }
				mTypography("This is a circuit from the project", paragraph = true)
				graphViewer {
					canvasId = "canvas0"
					metaGraphUuid = UUID("440b10dc-0999-4426-aa0f-c22c5221f641")
					size = Dimension2D(500, 400)
				}

				//p { +"The following explains how a 'Half Adder' works."  }
				mTypography("The following explains how a 'Half Adder' works.", paragraph = true)
				graphViewer {
					canvasId = "canvas1"
					metaGraphUuid = UUID("52255dc4-c010-4f6f-8ea6-9c2c8f5f9a82")
					size = Dimension2D(500, 400)
				}

				//p { +"The following explains how a 'Full Adder' works."  }
				mTypography("The following explains how a 'Full Adder' works.", paragraph = true)
				graphViewer {
					canvasId = "canvas2"
					metaGraphUuid = UUID("08aba425-96c2-4c43-b10b-2e0c72ce8300")
					size = Dimension2D(500, 400)
				}
			}
		}
	}
}