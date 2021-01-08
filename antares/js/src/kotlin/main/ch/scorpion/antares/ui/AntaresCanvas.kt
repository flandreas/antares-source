package ch.scorpion.antares.ui

import ch.scorpion.jabbah.draw.Canvas
import ch.scorpion.jabbah.draw.InputEventContext
import ch.scorpion.jabbah.draw.View
import ch.scorpion.jabbah.draw.style.StyleRepository
import ch.scorpion.jabbah.draw.view.CanvasJs
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.editor.EditEditorModule
import ch.scorpion.jabbah.graph.GraphApplicationContext
import ch.scorpion.jabbah.graph.app.ApplicationModeHolder
import ch.scorpion.jabbah.graph.app.ApplicationModeHolderImpl
import com.ccfraser.muirwik.components.MColor
import com.ccfraser.muirwik.components.button.MButtonVariant
import com.ccfraser.muirwik.components.button.mButton
import kotlinx.html.id
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.dom.canvas
import styled.styledDiv

external interface AntaresCanvasProps : RProps {
	var canvasId: String
	var width: Int
	var height: Int
	var viewFactory: (Canvas) -> View<out InputEventContext>
}

/** Displays a [View] in a [CanvasJs]. */
class AntaresCanvas : RComponent<AntaresCanvasProps, RState>() {

	private lateinit var applicationModeHolder: ApplicationModeHolder

	override fun componentDidMount() {
		val jabbahCanvas = CanvasJs(props.canvasId, props.viewFactory, StyleRepository.INSTANCE)
		val editor = EditEditorModule.createEditor(jabbahCanvas.view as DrawingView<Drawing<Component>>)
		editor.view.applicationContext = GraphApplicationContext()

		applicationModeHolder = ApplicationModeHolderImpl(editor)
	}

	override fun RBuilder.render() {
		styledDiv {
			mButton("Simulate", color = MColor.primary, variant = MButtonVariant.contained)
		}
		canvas {
			attrs.id = props.canvasId
			attrs.width = props.width.toString()
			attrs.height = props.height.toString()
		}
	}
}