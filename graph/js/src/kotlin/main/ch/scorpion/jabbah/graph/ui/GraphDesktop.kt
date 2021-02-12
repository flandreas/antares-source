package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.draw.graphics.CompositeColor
import ch.scorpion.jabbah.draw.view.ViewManager
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeView
import react.*

external interface GraphDesktopViewJsProps : RProps {
	var controller: GraphDesktopViewController
	var graphEditView: ReactElement
}

fun RBuilder.graphDesktopView(handler: GraphDesktopViewJsProps.() -> Unit): ReactElement {
	return child(GraphDesktopViewJs::class) {
		this.attrs(handler)
	}
}

class GraphDesktopViewJs(
	props: GraphDesktopViewJsProps
) : RComponent<GraphDesktopViewJsProps, RState>(props), GraphDesktopView {

	init {
		console.log("Setting View in GraphDesktopController")
		props.controller.view = this
	}

	override fun dispose() { }

	/** ---- [RComponent] */

	override fun RBuilder.render() {
		console.log("GraphDesktopViewJs.render")

		// TODO: This doesn't work
		childList.add(props.graphEditView)
	}

	/** ---- [GraphDesktopView] */

	override val mainDesktopViewItem: GraphDesktopViewItem
		get() = TODO("mainDesktopViewItem not implemented")

	override fun createSubGraphDesktopItem(verticeView: SubGraphVerticeView<*>, referenceColor: CompositeColor, isParentDetached: Boolean, viewManager: ViewManager): GraphDesktopViewItem {
		throw UnsupportedOperationException("createSubGraphDesktopItem not implemented")
	}

	override fun addGraphDesktopItem(item: GraphDesktopViewItem) {
		TODO("addGraphDesktopItem not implemented")
	}

	override fun closeItem(item: GraphDesktopViewItem) {
		TODO("closeItem not implemented")
	}

	override fun closeAll(establishSingleView: Boolean) {
		// TODO not used so far. Implement once desktop management is used
	}
}