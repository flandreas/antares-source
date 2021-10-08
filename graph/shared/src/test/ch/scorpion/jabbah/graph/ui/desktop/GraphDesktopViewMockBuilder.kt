package ch.scorpion.jabbah.graph.ui.desktop

import ch.scorpion.jabbah.draw.graphics.CompositeColor
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.graph.ui.*
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeView
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot

class GraphDesktopViewMockBuilder(private val controller: GraphDesktopViewController) {

	private val view = mockk<GraphDesktopView>(relaxed = true)
	private val referenceColorSlot = slot<CompositeColor>()
	val referenceColor get() = referenceColorSlot.captured

	private val subGraphVerticeViewSlot = slot<SubGraphVerticeView<*>>()

	init {
		controller.view = view
		withMainViewItem(GraphDesktopViewItemMockBuilder().build())
		withCreateGraphNavigationViewSubGraphDesktopItem(isParentDetached = false)
	}

	fun withMainViewItem(item: GraphDesktopViewItem): GraphDesktopViewMockBuilder {
		every { view.mainDesktopViewItem } returns item
		return this
	}

	fun withCreatedSubGraphDesktopItem(item: GraphDesktopViewItem): GraphDesktopViewMockBuilder {
		every { view.createSubGraphDesktopItem(any(), capture(referenceColorSlot), any(), any()) } returns item
		return this
	}

	fun withCreateGraphNavigationViewSubGraphDesktopItem(isParentDetached: Boolean) {
		every { view.createSubGraphDesktopItem(capture(subGraphVerticeViewSlot), any(), any(), any()) } answers {
			createGraphNavigationViewDesktopItem(subGraphVerticeViewSlot.captured, isParentDetached)
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