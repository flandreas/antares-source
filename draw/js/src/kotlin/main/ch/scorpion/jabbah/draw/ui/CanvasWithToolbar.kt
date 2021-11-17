package ch.scorpion.jabbah.draw.ui

import ch.scorpion.jabbah.base.geom.Dimension2D
import ch.scorpion.jabbah.base.mreact.IntersectionObserver
import ch.scorpion.jabbah.base.mreact.IntersectionObserverEntry
import ch.scorpion.jabbah.base.mreact.IntersectionObserverOptions
import ch.scorpion.jabbah.draw.View
import ch.scorpion.jabbah.draw.view.ViewSpace
import kotlinx.css.*
import org.w3c.dom.Element
import react.*
import styled.css
import styled.styledDiv

/**
 * @property size the size of the canvas in view coordinates (used for "portlet" scenarios),
 * or `null` if the canvas should adjust to the available size (used for "iframe" or "desktop" scenarios)
 */
external interface CanvasWithToolbarProps : Props {
	var canvasId: String
	var view: View<*>
	var size: Dimension2D?
	var responsive: Boolean
	var toolbarRenderer: (RBuilder) -> Unit
}

fun RBuilder.canvasWithToolbar(handler: CanvasWithToolbarProps.() -> Unit) {
	child(CanvasWithToolbar::class) {
		this.attrs(handler)
	}
}

/**
 * [CanvasWithToolbar] displays the toolbar created by [CanvasWithToolbarProps.toolbarRenderer]
 * at the top of the canvas when the mouse hovers the canvas.
 *
 * Displaying the toolbar is controlled by stylesheet rules in Antares.css, relying on the
 * class "toolbarOverCanvas" for the div that contains the toolbar.
 *
 * It also uses [ViewSpace.reduceTop] when the toolbar is displayed. Detecting whether the
 * toolbar is displayed or not is based on [IntersectionObserver]. MutationObserver wouldn't
 * work because displaying/hiding the toolbar is controlled by the stylesheet, whose changes
 * seem to not trigger MutationObserver.
 */
class CanvasWithToolbar(
	props: CanvasWithToolbarProps
) : RComponent<CanvasWithToolbarProps, State>(props) {

	private var intersection: Int = 0

	private val canvasRef = createRef<Element>()

	private val toolbarOverCanvasRef = createRef<Element>()

	private val intersectionObserver = IntersectionObserver(
		::intersectionObserverCallback,
		IntersectionObserverOptions {
			root = canvasRef.current
		}
	)

	override fun componentDidMount() {
		intersectionObserver.observe(toolbarOverCanvasRef.current!!)
	}

	override fun componentWillUnmount() {
		intersectionObserver.unobserve(toolbarOverCanvasRef.current!!)
	}

	override fun RBuilder.render() {
		styledDiv {
			css {
				position = Position.relative
				display = Display.flex
				flexDirection = FlexDirection.column
				flex(1.0)

			}
			if (props.size == null || props.responsive) {
				jCanvas {
					canvasId = props.canvasId
					view = props.view
					responsive = props.responsive
					ref = canvasRef
				}
				styledDiv {
					css {
						position = Position.absolute
						top = 7.px
						left = 7.px
						width = 100.pct - 14.px
						classes.add("toolbarOverCanvas")
					}
					ref = toolbarOverCanvasRef
					props.toolbarRenderer(this)
				}
			} else {
				jCanvas {
					canvasId = props.canvasId
					view = props.view
					size = props.size!!
					responsive = props.responsive
					ref = canvasRef
				}
				styledDiv {
					css {
						position = Position.absolute
						top = 1.px
						left = 1.px
						width = props.size!!.width.px - 2.px
						classes.add("toolbarOverCanvas")
					}
					ref = toolbarOverCanvasRef
					props.toolbarRenderer(this)
				}
			}
		}
	}

	private fun intersectionObserverCallback(entries: Array<IntersectionObserverEntry>, observer: IntersectionObserver) {
		if (observer !== intersectionObserver) {
			return
		}
		entries.firstOrNull()?.let {
			if (it.isIntersecting) {
				intersection = it.intersectionRect.height.toInt()
				props.view.space.reduceTop(intersection)
			} else {
				props.view.space.removeTopReduction(intersection)
			}
		}
	}
}