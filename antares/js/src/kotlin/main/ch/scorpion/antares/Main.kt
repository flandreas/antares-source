package ch.scorpion.antares

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
import ch.scorpion.jabbah.edit.module.EditModuleJs
import ch.scorpion.jabbah.edit.view.DrawingViewImpl
import kotlinx.html.id
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.dom.canvas
import react.dom.h1
import react.dom.render
import kotlin.browser.document

fun main() {

	EditModuleJs.require()

	render(document.getElementById("root")) {
		child(App::class) {}
	}
}

class App : RComponent<RProps, RState>() {

	override fun RBuilder.render() {
		val point = Point2D(100, 200)
		h1 {
			+"Hello, Antares with React+Kotlin/JS!"
		}

		child(AntaresCanvas::class) {
			attrs.canvasId = "kotlinCanvas"
			attrs.width = 800
			attrs.height = 600
			attrs.viewFactory = { DrawingViewImpl<Drawing<Component>>(createDrawing(), it) }
		}
	}

	private fun createDrawing(): Drawing<Component> {
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
		EditEditorModule.createEditor(jabbahCanvas.view as DrawingView<Drawing<Component>>)

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
