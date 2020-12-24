package ch.scorpion.jabbah.graph.ui.desktop

import ch.scorpion.jabbah.draw.graphics.CompositeColor
import ch.scorpion.jabbah.graph.ui.GraphDesktopView
import ch.scorpion.jabbah.graph.ui.GraphDesktopViewController
import ch.scorpion.jabbah.graph.ui.GraphDesktopViewItem
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot

class GraphDesktopViewMockBuilder(controller: GraphDesktopViewController) {

	private val view = mockk<GraphDesktopView>(relaxed = true)
	private val referenceColorSlot = slot<CompositeColor>()
	val referenceColor get() = referenceColorSlot.captured

	init {
		controller.view = view
	}

	fun withMainViewItem(item: GraphDesktopViewItem): GraphDesktopViewMockBuilder {
		every { view.mainDesktopViewItem } returns item
		return this
	}

	fun withCreatedSubGraphDesktopItem(item: GraphDesktopViewItem): GraphDesktopViewMockBuilder {
		every { view.createSubGraphDesktopItem(any(), capture(referenceColorSlot), any(), any()) } returns item
		return this
	}

	fun build(): GraphDesktopView = view
}