package ch.scorpion.antares

import ch.scorpion.antares.module.AntaresModuleJs
import ch.scorpion.antares.ui.AntaresCanvas
import ch.scorpion.antares.view.theme.AntaresThemes
import ch.scorpion.jabbah.base.LogLevel
import ch.scorpion.jabbah.base.LogSystem
import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.auth.EditAuthModule
import ch.scorpion.jabbah.edit.auth.User
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.library.LibraryModule
import ch.scorpion.jabbah.io.DomXmlReader
import ch.scorpion.jabbah.io.StoreXmlReader
import kotlinx.browser.document
import org.w3c.xhr.XMLHttpRequest
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.dom.*

fun main() {
	AntaresModuleJs.require()
	AntaresThemes.install()

	LogSystem.level = LogLevel.Info

	EditAuthModule.userHolder.u = User.anybody
	LibraryModule.libraryHolder.l = LibraryModule.libraryService.loadLibrary(AntaresApplication.DEF_LIBRARY_UUID, isSystem = true)

	render(document.getElementById("root")) {
		child(App::class) {}
	}
}

class App : RComponent<RProps, RState>() {

	override fun RBuilder.render() {
		h1 {
			+"Antares Web: Level 5"
		}

		/*
		ul {
			li { +"Use the mouse wheel to zoom" }
			li { +"Click/drag with middle mouse button to pan" }
			li { +"Click/drag with left mouse button to move components" }
			li { +"Click 'Play' button to start simulation" }
			li { +"Click on input components to change input values" }
			li { +"Click 'Pause' button to activate single step mode" }
			li { +"Click 'Resume' button to resume after breakpoint "}
			li { +"Drag slider knob to change simulation speed" }
		}

		p {
			+"Note that signal flow animation is activated below 33% system speed."
		}
		*/

		child(AntaresCanvas::class) {
			attrs.canvasId = "kotlinCanvas"
			attrs.width = 800
			attrs.height = 600
			attrs.drawing = loadLevel2LibraryDrawing()
		}
	}

	private fun loadLevel2LibraryDrawing(): Drawing<Component> {
		return loadMetaGraph(UUID("08aba425-96c2-4c43-b10b-2e0c72ce8300"))
			.graph.graphView as Drawing<Component>
	}

	// TODO Replace by service call
	private fun loadMetaGraph(uuid: UUID): MetaGraph {
		val baseUrl = ".."
		val libraryUuid = "6707f981-110d-4629-a0bf-c35a4688025c"
		val request = XMLHttpRequest()
		val url = "$baseUrl/libraries/${libraryUuid}/${uuid.id}.cir"
		request.open("GET", url, async = false)
		request.overrideMimeType("text/xml")
		request.send()
		return StoreXmlReader(DomXmlReader(request.responseXML!!)).readStorable() as MetaGraph
	}
}


