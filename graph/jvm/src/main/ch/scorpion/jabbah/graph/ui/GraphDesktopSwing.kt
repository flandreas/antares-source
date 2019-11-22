package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.swing.UiUtil
import ch.scorpion.jabbah.draw.graphics.CompositeColor
import ch.scorpion.jabbah.draw.graphics.Graphics2DJvm
import ch.scorpion.jabbah.draw.module.DrawModule
import ch.scorpion.jabbah.draw.view.CanvasJvm
import ch.scorpion.jabbah.draw.view.ViewManager
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.DrawingViewContent
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.execution.scheduler.Scheduler
import ch.scorpion.jabbah.graph.view.GraphElementView
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.VerticeView
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeView
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.GridLayout
import javax.swing.*
import javax.swing.border.Border
import kotlin.math.max

/** Displays the contents of a [VerticeView] within a separate [GraphDesktop] view.*/
interface GraphDesktopItem {

	val drawingView: DrawingView<GraphView<GraphElementView<*>>>?

	var contextColor: CompositeColor?

	fun dispose()

	fun findContent(condition: (DrawingViewContent<GraphView<GraphElementView<*>>>) -> Boolean): DrawingViewContent<*>?
}

data class GraphDesktopItemCloseRequest(val item: GraphDesktopItem)

abstract class AbstractGraphDesktopItemPanel : JPanel(), GraphDesktopItem {

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

	protected abstract fun addContextColorBorder(color: ch.scorpion.jabbah.draw.graphics.Color)

	protected abstract fun removeContextColorBorder()

	protected fun createContextColorBorder(contextColor: ch.scorpion.jabbah.draw.graphics.Color): Border =
		BorderFactory.createLineBorder(Graphics2DJvm.toAwtColor(contextColor), BORDER_THICKNESS, true)
}

class GraphDesktopItemHeaderPanel(
	private val graphDesktopItem: GraphDesktopItem,
	content: JComponent,
	private val eventBus: EventBus = BaseModule.eventBus,
	allowClose: Boolean = true
) : JPanel() {

	companion object {
		const val PROP_BACKGROUND_COLOR = "graph.ui.GraphDesktopItemHeader.background"
		const val PREF_HEIGHT = 27
		const val LEFT_INSET = 10
	}

	init {
		layout = BoxLayout(this, BoxLayout.LINE_AXIS)
		add(Box.createHorizontalStrut(LEFT_INSET))
		add(content)
		background = Graphics2DJvm.toAwtColor(DrawModule.properties.getColor(PROP_BACKGROUND_COLOR))

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
			eventBus.post(GraphDesktopItemCloseRequest(graphDesktopItem))
		}
	}
}

class GraphDesktopSwing(
	private val graphEditPanel: GraphEditPanel
) : JPanel(), GraphDesktop {

	private val mainSplitPane = JSplitPane(JSplitPane.HORIZONTAL_SPLIT)

	/** The [JPanel] at the right side containing all slave views, if any. */
	private val sidePanel = JPanel()

	/** Contains all open [GraphDesktopItem]s that are not the main one.*/
	private val slaveGraphDesktopItems: MutableList<GraphDesktopItem> = mutableListOf()

	init {
		mainSplitPane.border = null
		sidePanel.layout = GridLayout(0, 1)
		layout = BorderLayout()
		background = Color.GRAY.brighter()

		add(graphEditPanel)
	}

	fun dispose() {
		graphEditPanel.dispose()
	}

	/** ---- [GraphDesktop] */

	override val mainDesktopItem: GraphDesktopItem get() = graphEditPanel.graphNavigationPanel

	override fun addGraphDesktopItem(item: GraphDesktopItem) {
		if (slaveGraphDesktopItems.isEmpty()) {
			remove(graphEditPanel)
			sidePanel.add(item as JComponent)
			mainSplitPane.leftComponent = graphEditPanel
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
		slaveGraphDesktopItems.add(item)
	}

	override fun closeItem(item: GraphDesktopItem) {
		slaveGraphDesktopItems.remove(item)
		sidePanel.remove(item as JComponent)
		if (slaveGraphDesktopItems.isEmpty()) {
			establishSingleView()
		}
		revalidate()
		repaint()
	}

	override fun closeAll(establishSingleView: Boolean) {
		slaveGraphDesktopItems.clear()
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
		viewManager: ViewManager,
		scheduler: Scheduler
	): GraphDesktopItem {
		val subGraphView = verticeView.createSubGraphView()
		val graphCanvas = CanvasJvm {
			val drawingView = EditModule.drawingViewFactory.invoke(subGraphView as Drawing<Component>, it)
			drawingView
		}
		val drawingView = graphCanvas.view as DrawingView<GraphView<GraphElementView<*>>>
		return GraphNavigationPanel(
			isRoot = false,
			drawingView = drawingView,
			viewManager = viewManager,
			contextBorderColor = referenceColor,
			scheduler = scheduler
		)
	}

	/** ---- [GraphDesktopSwing] */

	/** Establish the UI for displaying only the root [GraphPanel].*/
	private fun establishSingleView() {
		remove(mainSplitPane)
		mainSplitPane.remove(mainSplitPane)
		mainSplitPane.remove(sidePanel)
		add(graphEditPanel)
		SwingUtilities.invokeLater { graphEditPanel.graphNavigationPanel.drawingView.requestFocus() }
	}

	private fun zoomViews(includeMasterView: Boolean) {
		SwingUtilities.invokeLater {
			if (includeMasterView) {
				graphEditPanel.graphNavigationPanel.drawingView.navigator.fitMaxNormal()
			}
			for (item in slaveGraphDesktopItems) {
				item.drawingView?.navigator?.fitMaxNormal()
			}
		}
	}
}