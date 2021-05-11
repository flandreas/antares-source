package ch.scorpion.jabbah.draw.view

import ch.scorpion.jabbah.base.mreact.useResizeDetector
import kotlinx.css.*
import kotlinx.html.id
import react.RProps
import react.dom.canvas
import react.functionalComponent
import styled.css
import styled.styledDiv

external interface ResponsiveCanvasProps : RProps {
	var canvasId: String
	var canvasJsProvider: () -> CanvasJs?
}

/**
 * A React component that observes the size of a <div> in which a [CanvasJs] is wrapped,
 * and calls [CanvasJs.resize] whenever that size changes.
 */
val responsiveCanvas = functionalComponent<ResponsiveCanvasProps> { props ->
	val resizeDetectionResult = useResizeDetector<Any>()
	props.canvasJsProvider.invoke()?.let {
		it.resize(resizeDetectionResult.width, resizeDetectionResult.height)
	}

	styledDiv {
		ref = resizeDetectionResult.ref
		css {
			backgroundColor = Color.yellow
			display = Display.flex
			height = LinearDimension.fillAvailable
			width = LinearDimension.fillAvailable
		}
		canvas {
			attrs.id = props.canvasId
		}
	}
}
