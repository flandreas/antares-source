package ch.scorpion.antares

import ch.scorpion.antares.module.AntaresModuleJs
import ch.scorpion.antares.view.theme.AntaresThemes
import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.draw.Canvas
import ch.scorpion.jabbah.draw.InputEventContext
import ch.scorpion.jabbah.draw.View
import ch.scorpion.jabbah.draw.polyline.ArrowHead
import ch.scorpion.jabbah.draw.style.StyleRepository
import ch.scorpion.jabbah.draw.view.CanvasJs
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.SelectionDrawingStrategy
import ch.scorpion.jabbah.edit.editor.EditEditorModule
import ch.scorpion.jabbah.edit.model.DrawingImpl
import ch.scorpion.jabbah.edit.model.polyline.PolylineComponent
import ch.scorpion.jabbah.edit.model.rectangle.RectangleComponent
import ch.scorpion.jabbah.edit.view.DrawingViewImpl
import ch.scorpion.jabbah.graph.GraphApplicationContext
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.io.DomXmlReader
import ch.scorpion.jabbah.io.StoreXmlReader
import kotlinx.browser.document
import kotlinx.html.id
import org.w3c.xhr.XMLHttpRequest
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.dom.*

fun main() {
	AntaresModuleJs.require()
	AntaresThemes.install()

	render(document.getElementById("root")) {
		child(App::class) {}
	}
}

class App : RComponent<RProps, RState>() {

	override fun RBuilder.render() {
		val point = Point2D(100, 200)
		h1 {
			+"Antares Web: Level 2"
		}

		p {
			+"Load and display circuit from standard library."
		}

		child(AntaresCanvas::class) {
			attrs.canvasId = "kotlinCanvas"
			attrs.width = 800
			attrs.height = 600
			attrs.viewFactory = { DrawingViewImpl(loadLevel2LibraryDrawing(), it) }
		}

		h1 {
			+"History"
		}

		h2 {
			+"Level 1"
		}

		p {
			+"Draw basic shapes. Various selection models. Drag and resize. Zoom using the mouse wheel."
		}
	}

	private fun createLevel1Drawing(): Drawing<Component> {
		val drawing = DrawingImpl<Component>()

		val rect = RectangleComponent(x = 100.0, y = 100.0, w = 200.0, h = 100.0)
		rect.filled = true
		rect.preferredSelectionDrawingStrategy = SelectionDrawingStrategy.ABOVE
		drawing.add(rect)

		val rect2 = RectangleComponent(x = 400.0, y = 300.0, w = 100.0, h = 60.0)
		rect2.filled = true
		rect2.preferredSelectionDrawingStrategy = SelectionDrawingStrategy.BELOW
		drawing.add(rect2)

		val polyline = PolylineComponent()
		polyline.addPoint(100.0, 400.0).addPoint(200.0, 500.0).addPoint(300.0, 300.0)
		polyline.endLineTerminator = ArrowHead()
		drawing.add(polyline)

		return drawing
	}

	private fun loadLevel2LibraryDrawing(): Drawing<Component> {
		return loadMetaGraph(UUID("9eb38fe7-5844-4be6-9192-25104a077b0c"))
			.graph.graphView as Drawing<Component>
	}

	private fun loadMetaGraph(uuid: UUID): MetaGraph {
		val baseUrl = ""
		val libraryUuid = "6707f981-110d-4629-a0bf-c35a4688025c"
		val request = XMLHttpRequest()
		val url = "$baseUrl/libraries/${libraryUuid}/${uuid.id}.cir"
		request.open("GET", url, async = false)
		request.overrideMimeType("text/xml")
		request.send()
		console.info(request.responseXML?.toString())
		return StoreXmlReader(DomXmlReader(request.responseXML!!)).readStorable() as MetaGraph
	}
}

external interface AntaresCanvasProps : RProps {
	var canvasId: String
	var width: Int
	var height: Int
	var viewFactory: (Canvas) -> View<out InputEventContext>
}

class AntaresCanvas : RComponent<AntaresCanvasProps, RState>() {

	override fun componentDidMount() {
		val jabbahCanvas = CanvasJs(props.canvasId, props.viewFactory, StyleRepository.INSTANCE)
		val editor = EditEditorModule.createEditor(jabbahCanvas.view as DrawingView<Drawing<Component>>)
		editor.view.applicationContext = GraphApplicationContext()

		jabbahCanvas.repaint()
	}

	override fun RBuilder.render() {
		canvas {
			attrs.id = props.canvasId
			attrs.width = props.width.toString()
			attrs.height = props.height.toString()
		}
	}
}
