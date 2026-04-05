package io.antarescircuit.jabbah.graph.ui

import io.antarescircuit.jabbah.edit.DrawingView
import io.antarescircuit.jabbah.edit.DrawingViewContent
import io.antarescircuit.jabbah.graph.ui.desktop.GraphDesktopViewItem
import io.antarescircuit.jabbah.graph.ui.desktop.GraphDesktopViewItemElementDepthRef
import io.antarescircuit.jabbah.graph.ui.desktop.GraphDesktopViewItemElementRef
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.VerticeView
import dev.mokkery.MockMode
import dev.mokkery.answering.calls
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock

class GraphDesktopViewItemMockBuilder {

	private val item = mock<GraphDesktopViewItem>(MockMode.autofill)

	init {
		withElementRef(GraphDesktopViewItemElementDepthRef(1, 0))
	}

	fun withDrawingView(drawingView: DrawingView<GraphView>): GraphDesktopViewItemMockBuilder {
		every { item.drawingView } returns drawingView
		return this
	}

	fun withFindContent(content: DrawingViewContent<GraphView>): GraphDesktopViewItemMockBuilder {
		every { item.findContent(any()) } returns content
		return this
	}

	fun withElementRef(ref: GraphDesktopViewItemElementRef): GraphDesktopViewItemMockBuilder {
		every { item.createElementRef(any()) } returns ref
		return this
	}

	fun withFindElementWithRef(vv: VerticeView<*>): GraphDesktopViewItemMockBuilder {
		every { item.findElementWithRef(any()) } returns vv
		return this
	}

	fun withGraphNavigationView(controller: GraphNavigationViewController): GraphDesktopViewItemMockBuilder {
		withDrawingView(controller.drawingView)
		every { item.disposeItem() } calls {
			controller.dispose()
		}
		return this
	}

	fun build(): GraphDesktopViewItem = item
}