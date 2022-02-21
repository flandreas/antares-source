package ch.scorpion.antares

import ch.scorpion.antares.module.AntaresModuleJs
import ch.scorpion.antares.view.theme.AntaresThemes
import ch.scorpion.antares.view.theme.BlackAndWhiteThemeBuilder
import ch.scorpion.jabbah.base.LogLevel
import ch.scorpion.jabbah.base.LogSystem
import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.base.geom.Dimension2D
import ch.scorpion.jabbah.edit.auth.DesktopUser
import ch.scorpion.jabbah.edit.auth.DesktopUserHolder
import ch.scorpion.jabbah.edit.auth.EditAuthModule
import ch.scorpion.jabbah.graph.library.LibraryModule
import ch.scorpion.jabbah.graph.project.ProjectModule
import ch.scorpion.jabbah.graph.ui.graphViewer
import com.ccfraser.muirwik.components.MTypographyVariant
import com.ccfraser.muirwik.components.mContainer
import com.ccfraser.muirwik.components.mCssBaseline
import com.ccfraser.muirwik.components.mTypography
import com.ccfraser.muirwik.components.styles.Breakpoint
import kotlinx.browser.document
import kotlinx.css.Color
import kotlinx.css.backgroundColor
import react.dom.render
import styled.css

class TestPage {

	fun show() {
		initialize()
		display()
	}

	private fun initialize() {
		console.info("Initializing TestPage")

		AntaresModuleJs.require()

		EditAuthModule.userHolder = DesktopUserHolder(DesktopUser.developer)

		LibraryModule.libraryHolder.l = LibraryModule.libraryService.loadLibrary(AntaresApplication.DEF_LIBRARY_UUID, isSystem = true)
		loadProject()

		AntaresThemes.install(BlackAndWhiteThemeBuilder.name)

		LogSystem.level = LogLevel.Info
	}

	private fun loadProject() {
		val projectUuid = UUID("a445e8d3-9aa8-4e84-91e9-04ec98ede249")
		ProjectModule.projectHolder.p = ProjectModule.projectManagementService.invoke().load(projectUuid)
	}

	private fun display() {
		render(document.getElementById("root")) {
			mCssBaseline()

			mContainer(maxWidth = Breakpoint.md) {
				css {
					backgroundColor = Color.white
				}
				mTypography("NMOS Transistor", variant = MTypographyVariant.h3)
				graphViewer {
					canvasId = "canvas1"
					metaGraphUuid = UUID("67f91a27-32da-4c8d-a081-f5b705c99618")
					size = Dimension2D(600, 400)
					addMargins = true
				}

				mTypography("PMOS Transistor", variant = MTypographyVariant.h3)
				graphViewer {
					canvasId = "canvas2"
					metaGraphUuid = UUID("b33f254e-73c4-443d-87cc-5668bf6ca202")
					size = Dimension2D(600, 700)
					addMargins = true
				}
			}
		}
	}
}