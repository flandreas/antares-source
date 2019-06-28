package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.app.ApplicationDataEvent
import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.invocation.InvocationHandler
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.swing.UiUtil
import ch.scorpion.jabbah.draw.DrawableContainerEvent
import ch.scorpion.jabbah.draw.container.DrawableContainerAdapter
import ch.scorpion.jabbah.draw.graphics.CompositeColor
import ch.scorpion.jabbah.draw.graphics.Graphics2DJvm
import ch.scorpion.jabbah.draw.graphics.ReferenceColorEvent
import ch.scorpion.jabbah.draw.graphics.ReferenceColorSequenceProvider
import ch.scorpion.jabbah.draw.module.DrawModule
import ch.scorpion.jabbah.draw.view.CanvasJvm
import ch.scorpion.jabbah.draw.view.DrawViewModule
import ch.scorpion.jabbah.draw.view.ViewManager
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.DrawingViewContent
import ch.scorpion.jabbah.edit.model.ComponentMessage
import ch.scorpion.jabbah.edit.model.ComponentMessageType
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.execution.module.ExecutionModule
import ch.scorpion.jabbah.execution.scheduler.Scheduler
import ch.scorpion.jabbah.graph.view.VerticeView
import ch.scorpion.jabbah.graph.view.GraphElementView
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.vertice.OpenSubGraphRequest
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

abstract class AbstractGraphDesktopItemPanel() : JPanel(), GraphDesktopItem {

	companion object {
		private const val BORDER_THICKNESS = 5
	}

	override var contextColor: CompositeColor? = null
		set(value) {
			if (field == value) {
				return
			}

			when {
				field == null -> addContextColorBorder(value!!)
				value == null -> removeContextColorBorder()
				else -> updateContextColorBorder(value)
			}

			field = value
			revalidate()
			repaint()
		}

	protected abstract fun addContextColorBorder(color: CompositeColor)

	protected abstract fun removeContextColorBorder()

	protected abstract fun updateContextColorBorder(color: CompositeColor)

	protected fun createContextColorBorder(contextColor: CompositeColor): Border =
		BorderFactory.createLineBorder(Graphics2DJvm.toAwtColor(contextColor.backgroundColor), BORDER_THICKNESS, true)
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

/**
 * Manages a master [GraphEditPanel] and multiple slave [GraphNavigationPanel]s.
 */
class GraphDesktop(
	private val graphEditPanel: GraphEditPanel,
	private val eventBus: EventBus = BaseModule.eventBus,
	private val scheduler: Scheduler = ExecutionModule.scheduler,
	showContentInitially: Boolean = true,
	private val viewManager: ViewManager = DrawViewModule.viewManager
) : JPanel() {

	companion object {
		private val LOG by logger(GraphDesktop::class)
	}

	private val mainSplitPane = JSplitPane(JSplitPane.HORIZONTAL_SPLIT)

	/** The [JPanel] at the right side containing all slave views, if any. */
	private val sidePanel = JPanel()

	/** Contains all open [GraphDesktopItem]s that are not the main one.*/
	private val slaveGraphDesktopItems: MutableList<GraphDesktopItem> = mutableListOf()

	/** Used for determining a [CompositeColor] for referencing a [SubGraphVerticeView] and its open [GraphDesktopItem].*/
	private var referenceColorSequence = ReferenceColorSequenceProvider.provide()

	/** Associates [SubGraphVerticeView] and their open [GraphNavigationPanel]s.*/
	private val associations = mutableListOf<Association>()

	/** Closes all slave panels when the edited root [GraphView] has changed.*/
	private val editedGraphViewEventHandler: (EditedGraphViewEvent) -> Unit = {
		it.oldGraphView?.removeDrawableContainerListener(removeListener)
		it.newGraphView?.addDrawableContainerListener(removeListener)
	}

	/** Closes an open [GraphDesktopItem] when the corresponding [VerticeView] has been removed.*/
	private val removeListener = object : DrawableContainerAdapter<GraphElementView<*>>() {
		override fun drawableRemoved(event: DrawableContainerEvent<GraphElementView<*>>) {
			associations.firstOrNull { it.ref === event.child }?.let { assoc ->
				{
					closeGraphDesktopItem(assoc.item)
					deassociate(assoc, assoc.sourceItem.drawingView?.content)
				}.invoke()
			}
		}
	}

	init {
		mainSplitPane.border = null
		sidePanel.layout = GridLayout(0, 1)
		layout = BorderLayout()
		background = Color.GRAY.brighter()

		eventBus.register(EditedGraphViewEvent::class, editedGraphViewEventHandler)

		eventBus.register(ApplicationDataEvent::class) {
			closeAll()
			if (it.newData != null) {
				establishSingleView()
			}
			invalidate()
			revalidate()
			repaint()
		}

		// Replace reference color in all Associations
		eventBus.register(ReferenceColorEvent::class) { event ->
			val newAssociations = associations.map { assoc -> assoc.copy(refColor = event.getNewColorFor(assoc.refColor)!!) }
			associations.clear()
			associations.addAll(newAssociations)
			associations.forEach { assoc ->
				assoc.item.contextColor = assoc.refColor
				event.replacements.forEach { assoc.item.drawingView?.highlighter?.replaceColor(it.oldColor, it.newColor) }
			}
			event.replacements.forEach { graphEditPanel.graphNavigationPanel.drawingView.highlighter.replaceColor(it.oldColor, it.newColor) }

		}

		eventBus.register(OpenSubGraphRequest::class) { request ->
			if (request.newView) {
				InvocationHandler.invoke { openSubGraphVerticeView(request.subGraphVerticeView) }
			}
		}

		eventBus.register(GraphDesktopItemCloseRequest::class) { closeGraphDesktopItem(it.item) }

		if (showContentInitially) {
			add(graphEditPanel)
		}
	}

	fun dispose() {
		graphEditPanel.dispose()
	}

	private fun openSubGraphVerticeView(view: SubGraphVerticeView<*>) {
		openVerticeView(view) {
			val subGraphView = view.createSubGraphView()
			val graphCanvas = CanvasJvm {
				val drawingView = EditModule.drawingViewFactory.invoke(subGraphView as Drawing<Component>, it)
				drawingView
			}
			val drawingView = graphCanvas.view as DrawingView<GraphView<GraphElementView<*>>>
			GraphNavigationPanel(
				isRoot = false,
				drawingView = drawingView,
				viewManager = viewManager,
				contextBorderColor = it,
				scheduler = scheduler
			)
		}
	}

	fun openVerticeView(vv: VerticeView<*>, itemFactory: (CompositeColor) -> GraphDesktopItem) {
		val assoc = associations.firstOrNull { it.ref == vv }
		if (assoc != null) {
			eventBus.post(ComponentMessage(type = ComponentMessageType.Info, source = assoc.ref, messageKey = "graph.vertice.alreadyOpen.msg"))
			return
		}

		itemContaining(vv)?.let {
			val refColor = referenceColorSequence.next()
			val newItem = itemFactory.invoke(refColor)
			associations.add(Association(it, vv, newItem, refColor))

			addGraphDesktopItem(newItem)

			it.drawingView?.highlighter?.highlight(vv, refColor.withForegroundLikeBackground())
			it.drawingView?.repaint()
		} ?: LOG.error("VerticeView to be opened not found in open panels")
	}

	private fun addGraphDesktopItem(item: GraphDesktopItem) {
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


	private fun closeGraphDesktopItem(item: GraphDesktopItem) {
		deassociate(item)

		item.dispose()
		slaveGraphDesktopItems.remove(item)
		item.drawingView?.let { viewManager.unregisterView(it) }

		if (slaveGraphDesktopItems.isEmpty()) {
			establishSingleView()
		}

		sidePanel.remove(item as JComponent)
		revalidate()
		repaint()
	}

	private fun closeAll() {
		closeAllSlavesImpl()
		removeAll()
		viewManager.activeView = null
		revalidate()
		repaint()
	}

	private fun closeAllSlavesImpl() {
		slaveGraphDesktopItems.forEach {
			deassociate(it)
			it.dispose()
		}
		slaveGraphDesktopItems.clear()
		sidePanel.removeAll()
	}

	/** Establish the UI for displaying only the root [GraphPanel].*/
	private fun establishSingleView() {
		remove(mainSplitPane)
		mainSplitPane.remove(mainSplitPane)
		mainSplitPane.remove(sidePanel)
		add(graphEditPanel)
		viewManager.registerView(graphEditPanel.graphNavigationPanel.drawingView)
		SwingUtilities.invokeLater { graphEditPanel.graphNavigationPanel.drawingView.requestFocus() }
	}

	/**
	 * Deassociate the specified open [GraphNavigationPanel] when it is being closed.
	 * Checks all existing [Association]s for the [DrawingViewContent]s that contains the associating [SubGraphVerticeView],
	 * and removes that [Association].
	 */
	private fun deassociate(item: GraphDesktopItem) {
		associationOf(item)?.let { assoc ->
			val content = assoc.sourceItem.findContent { it.drawing.contains(assoc.ref) }
			if (content != null) {
				deassociate(assoc, content)
			}
		}
	}

	private fun deassociate(assoc: Association, content: DrawingViewContent<*>?) {
		content?.let {
			it.highlighter.unhighlight(assoc.ref)
			referenceColorSequence.free(assoc.refColor)
			associations.remove(assoc)
		}
	}

	private fun associationOf(item: GraphDesktopItem): Association? =
		associations.firstOrNull { assoc -> assoc.item == item }

	private fun zoomViews(includeMasterView: Boolean) {
		SwingUtilities.invokeLater {
			if (includeMasterView) {
				graphEditPanel.graphNavigationPanel.drawingView.navigator.fitMaxNormal()
			}
			for (panel in slaveGraphDesktopItems) {
				panel.drawingView?.navigator?.fitMaxNormal()
			}
		}
	}

	/**
	 * Finds the [GraphNavigationPanel] that contains the specified [VerticeView].
	 */
	private fun itemContaining(vv: VerticeView<*>): GraphDesktopItem? {
		if (graphEditPanel.graphNavigationPanel.drawingView.drawing.contains(vv)) {
			return graphEditPanel.graphNavigationPanel
		}
		return slaveGraphDesktopItems.firstOrNull { it.drawingView?.drawing?.contains(vv) ?: false }
	}

	/**
	 * Maintains an association between a [VerticeView] and the [GraphDesktopItem] that has been opened
	 * in this [GraphDesktop], along with the [CompositeColor] that is used as a visual reference.
	 */
	private data class Association(
		val sourceItem: GraphDesktopItem,
		val ref: VerticeView<*>,
		val item: GraphDesktopItem,
		val refColor: CompositeColor)
}