package ch.scorpion.jabbah.draw.view

import ch.scorpion.jabbah.base.mreact.ResizeDetectorFunctionProps
import ch.scorpion.jabbah.base.mreact.useResizeDetector
import kotlinx.css.*
import kotlinx.html.id
import kotlinx.html.tabIndex
import org.w3c.dom.HTMLCanvasElement
import react.*
import react.dom.attrs
import styled.css
import styled.styledCanvas
import styled.styledDiv

external interface ResponsiveCanvasProps : Props {
	var canvasId: String
	var canvasJsProvider: () -> CanvasJs?
	var ref: RefObject<*>?
}

data class ResizeDetectorFunctionPropsObj(
	override var onResize: ((Int, Int) -> Unit)?,
	override var targetRef: MutableRefObject<HTMLCanvasElement>?
) : ResizeDetectorFunctionProps<HTMLCanvasElement>

/**
 * A React component that observes the size of a <div> in which a [CanvasJs] is wrapped,
 * and calls [CanvasJs.resize] whenever that size changes.
 */
val responsiveCanvas = fc<ResponsiveCanvasProps> { props ->

	val resizeDetectionResult = useResizeDetector(
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
			display = Display.flex
			flexDirection = FlexDirection.column
			flex(1.0)
		}
		styledCanvas {
			css {
				width = 100.pct - 6.px
				height = 100.pct - 6.px
				margin(3.px)
				border = "1px solid gray"
			}
			attrs {
				id = props.canvasId
				tabIndex = "1"
			}
		}
	}
}
