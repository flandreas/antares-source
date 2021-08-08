package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.swing.UiUtil
import ch.scorpion.jabbah.draw.graphics.CompositeColor
import ch.scorpion.jabbah.draw.graphics.Graphics2DJvm
import ch.scorpion.jabbah.draw.view.ViewManager
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.execution.module.ExecutionModule
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeView
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.GridLayout
import javax.swing.*
import javax.swing.border.Border
import kotlin.math.max

abstract class AbstractGraphDesktopItemPanel : JPanel(), GraphDesktopViewItem {

	companion object {
		private const val BORDER_THICKNESS = 5
	}

	override var contextColor: CompositeColor? = null
		set(value) {
			if (field == value) {
				return
			}

			when {
				field == null -> addContextColorBorder(value!!.foregroundColor)
				value == null -> removeContextColorBorder()
				else -> addContextColorBorder(value.foregroundColor)
			}

			field = value
			revalidate()
			repaint()
		}

	override val isDetached: Boolean = false

	protected abstract fun addContextColorBorder(color: ch.scorpion.jabbah.draw.graphics.Color)

	protected abstract fun removeContextColorBorder()

	protected fun createContextColorBorder(contextColor: ch.scorpion.jabbah.draw.graphics.Color): Border =
		BorderFactory.createLineBorder(Graphics2DJvm.toAwtColor(contextColor), BORDER_THICKNESS, true)
}

class GraphDesktopItemHeaderPanel(
	private val graphDesktopViewItem: GraphDesktopViewItem,
	content: JComponent,
	private val eventBus: EventBus = BaseModule.eventBus,
	allowClose: Boolean = true
) : JPanel() {

	companion object {
		const val PROP_BACKGROUND_COLOR = "graph.ui.GraphDesktopItemHeader.background"
		const val PREF_HEIGHT = 27
		const val LEFT_INSET = 10

		val headerBackgroundColor: Color get() = UiUtil.getBackgroundDivertColor(UIManager.getColor("Panel.background"))
	}

	init {
		layout = BoxLayout(this, BoxLayout.LINE_AXIS)
		add(Box.createHorizontalStrut(LEFT_INSET))
		add(content)
		background = headerBackgroundColor

		if (allowClose) {
			add(Box.createHorizontalGlue())
			add(UiUtil.createToolBarButton(CloseAction()))
		}
	}

	override fun getPreferredSize(): Dimension {
		return Dimension(super.getPreferredSize().width, max(PREF_HEIGHT, super.getPreferredSize().height))
	}

	private inner class CloseAction : AbstractAction("base.action.close") {

		init {
			imagePath = "/img/close-16.png"
		}

		override fun execute(event: ActionEvent) {
			eventBus.post(graphDesktopViewItem.createCloseRequest())
		}
	}
}

class GraphDesktopViewSwing(
	controller: GraphDesktopViewController,
	private val graphEditView: GraphEditViewSwing
) : JPanel(), GraphDesktopView {

	private val mainSplitPane = JSplitPane(JSplitPane.HORIZONTAL_SPLIT)

	/** The [JPanel] at the right side containing all slave views, if any. */
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

	override fun dispose() {
		graphEditView.dispose()
	}

	/** ---- [GraphDesktopView] */

	override val mainDesktopViewItem: GraphDesktopViewItem get() = graphEditView.graphNavigationView

	override fun addGraphDesktopItem(item: GraphDesktopViewItem) {
		if (slaveGraphDesktopViewItems.isEmpty()) {
			remove(graphEditView)
			sidePanel.add(item as JComponent)
			mainSplitPane.leftComponent = graphEditView
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

	override fun closeItem(item: GraphDesktopViewItem) {
		slaveGraphDesktopViewItems.remove(item)
		sidePanel.remove(item as JComponent)
		if (slaveGraphDesktopViewItems.isEmpty()) {
			establishSingleView()
		}
		revalidate()
		repaint()
	}

	override fun closeAll(establishSingleView: Boolean) {
		slaveGraphDesktopViewItems.clear()
		sidePanel.removeAll()
		removeAll()
		if (establishSingleView) {
			establishSingleView()
		}
		revalidate()
		repaint()
	}

	override fun createSubGraphDesktopItem(
		verticeView: SubGraphVerticeView<*>,
		referenceColor: CompositeColor,
		isParentDetached: Boolean,
		viewManager: ViewManager
	): GraphDesktopViewItem {
		val subGraphView = verticeView.createSubGraphView()
		val drawingView = EditModule.drawingViewFactory.invoke(subGraphView as Drawing<Component>) as DrawingView<GraphView>

		val controller = GraphNavigationViewController(
			isRoot = false,
			isParentDetached = isParentDetached,
			drawingView = drawingView,
			scheduler = ExecutionModule.scheduler)

		val graphNavigationView = GraphNavigationViewSwing(
			controller = controller,
			drawingView = drawingView,
			viewManager = viewManager,
			contextBorderColor = referenceColor
		)

		controller.setRootGraphView(drawingView.drawing, editable = false, applyZoomStrategy = true)

		return graphNavigationView
	}

	/** ---- [GraphDesktopViewSwing] */

	/** Establish the UI for displaying only the root [GraphPanelViewSwing].*/
	private fun establishSingleView() {
		remove(mainSplitPane)
		mainSplitPane.remove(mainSplitPane)
		mainSplitPane.remove(sidePanel)
		add(graphEditView)
		SwingUtilities.invokeLater { graphEditView.graphNavigationView.drawingView.requestFocus() }
	}

	private fun zoomViews(includeMasterView: Boolean) {
		SwingUtilities.invokeLater {
			if (includeMasterView) {
				graphEditView.graphNavigationView.drawingView.navigator.fitMaxNormal()
			}
			for (item in slaveGraphDesktopViewItems) {
				item.drawingView?.navigator?.fitMaxNormal()
			}
		}
	}
}