package ch.scorpion.jabbah.draw.view

import ch.scorpion.jabbah.base.mreact.ResizeDetectorFunctionProps
import ch.scorpion.jabbah.base.mreact.useResizeDetector
import kotlinx.css.*
import kotlinx.html.id
import org.w3c.dom.HTMLCanvasElement
import react.RMutableRef
import react.RProps
import react.dom.attrs
import react.functionalComponent
import styled.css
import styled.styledCanvas
import styled.styledDiv

external interface ResponsiveCanvasProps : RProps {
	var canvasId: String
	var canvasJsProvider: () -> CanvasJs?
}

data class ResizeDetectorFunctionPropsObj(
	override var onResize: ((Int, Int) -> Unit)?,
	override var targetRef: RMutableRef<HTMLCanvasElement>?
) : ResizeDetectorFunctionProps<HTMLCanvasElement>

/**
 * A React component that observes the size of a <div> in which a [CanvasJs] is wrapped,
 * and calls [CanvasJs.resize] whenever that size changes.
 */
val responsiveCanvas = functionalComponent<ResponsiveCanvasProps> { props ->

	val resizeDetectionResult = useResizeDetector<HTMLCanvasElement>(
		ResizeDetectorFunctionPropsObj(
			onResize = { w, h ->
				props.canvasJsProvider.invoke()?.let { canvasJs ->
					canvasJs.resize(w, h)
				}
		   },
			targetRef = null
		)
	)

	styledDiv {
		ref = resizeDetectionResult.ref
		css {
			flexGrow = 1.0
		}
		styledCanvas {
			css {
				width = 100.vw
				height = 100.vh
				margin = "0px"
				border = "0"
			}
			attrs {
				id = props.canvasId
			}
		}
	}
}
