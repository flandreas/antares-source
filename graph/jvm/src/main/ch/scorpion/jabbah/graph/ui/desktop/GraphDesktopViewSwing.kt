package ch.scorpion.jabbah.graph.ui.desktop

import ch.scorpion.jabbah.draw.graphics.CompositeColor
import ch.scorpion.jabbah.draw.view.ContentViewManager
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.graph.ui.GraphNavigationViewController
import ch.scorpion.jabbah.graph.ui.GraphNavigationViewSwing
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeView
import java.awt.BorderLayout
import java.awt.GridLayout
import javax.swing.*

class GraphDesktopViewSwing(
	private val controller: GraphDesktopViewController,
) : JPanel(), GraphDesktopView {

	private val mainSplitPane = JSplitPane(JSplitPane.HORIZONTAL_SPLIT)

	/** The [JPanel] to the right side containing all slave views, if any. */
	private val sidePanel = JPanel()

	/** Contains all open [GraphDesktopViewItem]s that are not the main one.*/
	private val slaveGraphDesktopViewItems: MutableList<GraphDesktopViewItem> = mutableListOf()

	init {
		controller.view = this

		mainSplitPane.border = null
		sidePanel.layout = GridLayout(0, 1)
		layout = BorderLayout()
		background = UIManager.getColor("Panel.background").darker()
	}

	override fun dispose() { }

	/** ---- [GraphDesktopView] */

	override fun showChildItem(item: GraphDesktopViewItem) {
		if (slaveGraphDesktopViewItems.isEmpty()) {
			remove(controller.mainDesktopViewItem!! as JComponent)
			sidePanel.add(item as JComponent)
			mainSplitPane.leftComponent = controller.mainDesktopViewItem as JComponent
			mainSplitPane.rightComponent = sidePanel
			add(mainSplitPane)

			sidePanel.invalidate()
			revalidate()

			SwingUtilities.invokeLater {
				// Has no effect until JSplitPane is shown on screen
				mainSplitPane.setDividerLocation(0.5)
				zoomViews(true)
			}
		} else {
			sidePanel.add(item as JComponent)
			sidePanel.invalidate()
			revalidate()
			zoomViews(false)
		}
		slaveGraphDesktopViewItems.add(item)
	}

	override fun closeChildItem(item: GraphDesktopViewItem) {
		slaveGraphDesktopViewItems.remove(item)
		sidePanel.remove(item as JComponent)
		if (slaveGraphDesktopViewItems.isEmpty()) {
			establishSingleView()
		}
		revalidate()
		repaint()
	}

	override fun showMainItem(item: GraphDesktopViewItem) {
		slaveGraphDesktopViewItems.clear()
		sidePanel.removeAll()
		removeAll()

		establishSingleView()
		revalidate()
		repaint()
	}

	override fun closeAll() {
		slaveGraphDesktopViewItems.clear()
		sidePanel.removeAll()
		removeAll()
		revalidate()
		repaint()
	}

	override fun createSubGraphDesktopItem(
		verticeView: SubGraphVerticeView<*>,
		referenceColor: CompositeColor?,
		isParentDetached: Boolean,
		viewManager: ContentViewManager
	): GraphDesktopViewItem {
		val subGraphView = verticeView.createSubGraphView(controller.applicationContextHolder.signalHandlerIfActive)
		val drawingView = EditModule.drawingViewFactory.create(
			subGraphView as Drawing<Component>, controller.applicationContextHolder, displayGlobalMessages = false, ""
		) as DrawingView<GraphView>

		val controller = GraphNavigationViewController(
			isRoot = false,
			isParentDetached = isParentDetached,
			drawingView = drawingView)

		val graphNavigationView = GraphNavigationViewSwing(
			controller = controller,
			drawingView = drawingView,
			viewManager = viewManager,
			reusable = false,
			contextBorderColor = referenceColor
		)

		controller.setRootGraphView(drawingView.drawing, editable = false, applyZoomStrategy = true, originSubGraphVerticeView = verticeView)

		return graphNavigationView
	}

	/** ---- [GraphDesktopViewSwing] */

	/** Establish the UI for displaying only the root [GraphPanelViewSwing].*/
	private fun establishSingleView() {
		remove(mainSplitPane)
		mainSplitPane.remove(mainSplitPane)
		mainSplitPane.remove(sidePanel)
		if (controller.mainDesktopViewItem != null) {
			add(controller.mainDesktopViewItem as JComponent)
		}
		SwingUtilities.invokeLater { controller.mainDesktopViewItem?.drawingView?.requestFocus() }
	}

	private fun zoomViews(includeMasterView: Boolean) {
		SwingUtilities.invokeLater {
			if (includeMasterView) {
				controller.mainDesktopViewItem?.drawingView?.navigator?.fitMaxNormal()
			}
			for (item in slaveGraphDesktopViewItems) {
				item.drawingView?.navigator?.fitMaxNormal()
			}
		}
	}
}