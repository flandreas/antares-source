package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.base.geom.Dimension2D
import ch.scorpion.jabbah.draw.CloseViewRequest
import ch.scorpion.jabbah.draw.graphics.CompositeColor
import ch.scorpion.jabbah.draw.ui.canvasWithToolbar
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.DrawingViewContent
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.ui.desktop.GraphDesktopViewItem
import kotlinx.css.*
import react.*
import styled.css
import styled.styledDiv

external interface GraphNavigationViewJsProps : Props {
	var canvasId: String
	var controller: GraphNavigationViewController
	var size: Dimension2D?
	var responsive: Boolean
	var canvasToolbarRenderer: (RBuilder) -> Unit
	var addMargins: Boolean?
	var toolbarBackgroundColor: String
}

fun RBuilder.graphNavigationView(handler: GraphNavigationViewJsProps.() -> Unit) {
	child(GraphNavigationViewJs::class) {
		this.attrs(handler)
	}
}

/**
 * A React Material implementation of [GraphNavigationView].
 */
private class GraphNavigationViewJs(
	props: GraphNavigationViewJsProps
) : RComponent<GraphNavigationViewJsProps, State>(props), GraphNavigationView {

	init {
		props.controller.view = this
	}

	override val showsNavigationRoot: Boolean
		get() = props.controller.navigationStackViewController.navigationStack.size == 1

	override fun componentWillUnmount() {
		dispose()
	}

	override fun RBuilder.render() {
		styledDiv {
			css {
				if (props.addMargins == true) {
					marginLeft = 40.px
					marginBottom = 20.px
				}
				if (props.size == null) {
					display = Display.flex
					height = 100.pct
					flexDirection = FlexDirection.column
					overflow = Overflow.hidden
				}
			}
			canvasWithToolbar {
				canvasId = props.canvasId
				view = props.controller.drawingView
				size = props.size
				responsive = props.responsive
				toolbarRenderer = {
					it.navigationStackView {
						controller = props.controller.navigationStackViewController
						backgroundColor = props.toolbarBackgroundColor
					}
					props.canvasToolbarRenderer(it)
				}
			}
		}
	}

	override val graphView: GraphView get() = props.controller.drawingView.drawing

	override fun refresh() {
		forceUpdate()
	}

	override fun dispose() { }

	/** ---- [GraphDesktopViewItem] */

	override val drawingView: DrawingView<GraphView> get() = props.controller.drawingView

	override var contextColor: CompositeColor? = null

	override val isDetached: Boolean get() = false

	override fun findContent(condition: (DrawingViewContent<GraphView>) -> Boolean): DrawingViewContent<*>? = null

	override fun createCloseRequest(): Any = CloseViewRequest(drawingView)

	override fun disposeItem() {
		dispose()
	}
}