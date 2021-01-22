package ch.scorpion.antares

import ch.scorpion.antares.AntaresApplication.Companion.DEF_LIBRARY_UUID
import ch.scorpion.antares.module.AntaresModuleJs
import ch.scorpion.antares.ui.AntaresCanvas
import ch.scorpion.antares.view.theme.AntaresThemes
import ch.scorpion.jabbah.app.AbstractApplicationJs
import ch.scorpion.jabbah.app.ApplicationData
import ch.scorpion.jabbah.app.ApplicationDataViewController
import ch.scorpion.jabbah.app.UnimplementedApplicationDataRepository
import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.edit.auth.EditAuthModule
import ch.scorpion.jabbah.edit.auth.User
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.library.ContainerLibraryElement
import ch.scorpion.jabbah.graph.library.LibraryModule
import ch.scorpion.jabbah.graph.library.LibrarySavable
import ch.scorpion.jabbah.io.DomXmlReader
import ch.scorpion.jabbah.io.StoreXmlReader
import kotlinx.browser.document
import org.w3c.xhr.XMLHttpRequest
import react.dom.h1
import react.dom.render

class AntaresJs(
	private val initialCircuitUuid: UUID
) : AbstractApplicationJs(
		ApplicationDataViewController(
			newStorableProvider = { MetaGraph() },
			repository = UnimplementedApplicationDataRepository())
		),
		AntaresApplication
{

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
	}

	override fun openInitialSavable() {
		val metaGraph = loadMetaGraph(initialCircuitUuid)

		render(document.getElementById("root")) {
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
				attrs.drawing = metaGraph.graph.graphView
			}
		}

		val savable = LibrarySavable(ContainerLibraryElement(
			initialCircuitUuid,
			initialName = metaGraph.graph.model!!.name.translation
		))

		controller.data = ApplicationData(metaGraph, savable)
	}

	// TODO Replace by service call
	private fun loadMetaGraph(uuid: UUID): MetaGraph {
		val baseUrl = ".."
		val libraryUuid = DEF_LIBRARY_UUID
		val request = XMLHttpRequest()
		val url = "$baseUrl/libraries/${libraryUuid}/${uuid.id}.cir"
		request.open("GET", url, async = false)
		request.overrideMimeType("text/xml")
		request.send()
		return StoreXmlReader(DomXmlReader(request.responseXML!!)).readStorable() as MetaGraph
	}
}