package ch.scorpion.jabbah.graph.ui.desktop

import ch.scorpion.jabbah.draw.graphics.CompositeColor
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.graph.ui.*
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeView
import dev.mokkery.MockMode
import dev.mokkery.answering.calls
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.matcher.capture.Capture
import dev.mokkery.matcher.capture.capture
import dev.mokkery.matcher.capture.get
import dev.mokkery.mock

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
		val drawingView = EditModule.drawingViewFactory.create(subGraphView as Drawing<Component>, controller.applicationContextHolder, false) as DrawingView<GraphView>

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