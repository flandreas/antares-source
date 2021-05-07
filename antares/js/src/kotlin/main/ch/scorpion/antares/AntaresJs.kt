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
import react.dom.*

class AntaresJs : AbstractApplicationJs(GraphDataViewController()), AntaresApplication {

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
		val initialProjectUuid = UUID("532f0477-722c-4c88-ada3-c419a386d06a")
		(controller as GraphDataViewController).openProject(initialProjectUuid)

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

	private fun display() {
		render(document.getElementById("root")) {
			/*
			h2 { +"Antares Web: Level 7" }

			h3 { +"View control" }

			ul {
				li { +"Use the mouse wheel to zoom" }
				li { +"Click/drag with middle mouse button to pan" }
			}

			h3 { +"Editing (saving not yet supported)" }

			ul {
				li { +"Click on component to select" }
				li { +"Click/drag with left mouse button to move components" }
				li { +"Click/drag a component from the library tree into the circuit" }
			}

			h3 { +"Simulation" }

			ul {
				li { +"Click 'Play' button to start simulation" }
				li { +"Click on input components to change input values" }
				li { +"Click 'Pause' button to activate single step mode" }
				li { +"Click 'Resume' button to resume after breakpoint " }
				li { +"Drag slider knob to change simulation speed" }
			}

			p {
				+"Note that signal flow animation is activated below 33% system speed."
			}

			h3 { +"Navigation" }

			ul {
				li { +"Double click on circuit in library tree to open the circuit" }
				li { +"Double click on sub circuit symbol to dive into with animation" }
				li { +"Cmd/Double click on subcircuit symbol to dive into without animation" }
				li { +"Click on breadcrumb to surface from subcircuit" }
			}
			*/
			child(AntaresViewJs::class) {
				attrs.application = this@AntaresJs
				attrs.applicationDataHolder = controller
				attrs.canvasId = "kotlinCanvas"
				attrs.size = null
				attrs.metaGraph = controller.data!!.content as MetaGraph
			}
		}
	}
}