package io.antarescircuit.jabbah.graph.ui

import dev.mokkery.MockMode
import dev.mokkery.answering.calls
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.matcher.capture.Capture
import dev.mokkery.matcher.capture.capture
import dev.mokkery.matcher.capture.get
import dev.mokkery.mock
import io.antarescircuit.jabbah.draw.CanvasMockBuilder
import io.antarescircuit.jabbah.draw.graphics.CompositeColor
import io.antarescircuit.jabbah.edit.module.EditModule
import io.antarescircuit.jabbah.graph.ui.desktop.GraphDesktopView
import io.antarescircuit.jabbah.graph.ui.desktop.GraphDesktopViewController
import io.antarescircuit.jabbah.graph.ui.desktop.GraphDesktopViewItem
import io.antarescircuit.jabbah.graph.view.vertice.SubGraphVerticeView

class GraphDesktopViewMockBuilder(private val controller: GraphDesktopViewController) {

	private val view = mock<GraphDesktopView>(MockMode.autofill)
	private val referenceColorSlot = Capture.slot<CompositeColor>()
	val referenceColor get() = referenceColorSlot.get()

	private val subGraphVerticeViewSlot = Capture.slot<SubGraphVerticeView<*>>()

	init {
		controller.view = view
		withMainViewItem(GraphDesktopViewItemMockBuilder().build())
		withCreateGraphNavigationViewSubGraphDesktopItem(isParentDetached = false)
	}

	fun withMainViewItem(item: GraphDesktopViewItem): GraphDesktopViewMockBuilder {
		controller.show(item)
		return this
	}

	fun withCreatedSubGraphDesktopItem(item: GraphDesktopViewItem): GraphDesktopViewMockBuilder {
		every { view.createSubGraphDesktopItem(any(), capture(referenceColorSlot), any(), any()) } returns item
		return this
	}

	fun withCreateGraphNavigationViewSubGraphDesktopItem(isParentDetached: Boolean) {
		every { view.createSubGraphDesktopItem(capture(subGraphVerticeViewSlot), any(), any(), any()) } calls {
			createGraphNavigationViewDesktopItem(subGraphVerticeViewSlot.get(), isParentDetached)
		}
	}

	private fun createGraphNavigationViewDesktopItem(verticeView: SubGraphVerticeView<*>, isParentDetached: Boolean): GraphDesktopViewItem {
		val subGraphView = verticeView.createSubGraphView(null)

		val drawingView = EditModule.drawingViewFactory.create(
			subGraphView,
			controller.applicationContextHolder,
			false,
			"")

		drawingView.canvas = CanvasMockBuilder().build()

		val controller = GraphNavigationViewController(
			isRoot = false,
			isParentDetached = isParentDetached,
			drawingView = drawingView)
		GraphNavigationViewMockBuilder(controller)

		controller.setRootGraphView(drawingView.drawing, editable = false, applyZoomStrategy = true)

		return GraphDesktopViewItemMockBuilder()
			//.withDrawingView(drawingView)
			.withGraphNavigationView(controller)
			.build()
	}

	fun build(): GraphDesktopView = view
}