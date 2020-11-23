package ch.scorpion.jabbah.graph.ui.desktop

import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.DrawingViewContent
import ch.scorpion.jabbah.graph.ui.GraphDesktopViewItem
import ch.scorpion.jabbah.graph.view.GraphView
import io.mockk.every
import io.mockk.mockk

class GraphDesktopViewItemMockBuilder {

	private val item = mockk<GraphDesktopViewItem>(relaxed = true)

	fun withDrawingView(drawingView: DrawingView<GraphView>): GraphDesktopViewItemMockBuilder {
		every { item.drawingView } returns drawingView
		return this
	}

	fun withFindContent(content: DrawingViewContent<GraphView>): GraphDesktopViewItemMockBuilder {
		every { item.findContent(any()) } returns content
		return this
	}

	fun build(): GraphDesktopViewItem = item
}