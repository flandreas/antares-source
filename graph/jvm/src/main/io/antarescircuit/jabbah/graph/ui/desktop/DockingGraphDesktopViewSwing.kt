package io.antarescircuit.jabbah.graph.ui.desktop

import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.event.EventHandler
import io.antarescircuit.jabbah.base.logger
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.base.swing.FillAllLayout
import io.antarescircuit.jabbah.draw.graphics.CompositeColor
import io.antarescircuit.jabbah.draw.view.ContentViewManager
import io.antarescircuit.jabbah.edit.Component
import io.antarescircuit.jabbah.edit.Drawing
import io.antarescircuit.jabbah.edit.DrawingView
import io.antarescircuit.jabbah.edit.module.EditModule
import io.antarescircuit.jabbah.graph.ui.GraphNavigationViewController
import io.antarescircuit.jabbah.graph.ui.GraphNavigationViewSwing
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.vertice.SubGraphVerticeView
import java.awt.*
import javax.swing.*

/**
 * An implementation of [GraphDesktopView] that supports "Docking", meaning that the user can pick
 * any of its [GraphDesktopViewItems][GraphDesktopViewItem] and drag it to another location.
 */
class DockingGraphDesktopViewSwing(
    private val controller: GraphDesktopViewController,
    maxRowsCount: Int = BaseModule.properties.getInt(GraphDesktopView.PROP_ROWS_PER_COLUMN),
    private val eventBus: EventBus = BaseModule.eventBus
) : JLayeredPane(), GraphDesktopView, DockingView {

    companion object {
        private val LOG by logger(DockingGraphDesktopViewSwing::class)
    }

    /** Holds the [GraphDesktopViewItems][GraphDesktopViewItem] organized in columns. */
    private val items = DockingGraphDesktopViewItems(maxRowsCount)

    /** The [JPanel] where the [GraphDesktopViewItems][GraphDesktopViewItem] (possibly contained in a [JSplitPane] are added. */
    private val content = JPanel(BorderLayout())

    /** Captures the potential new location of a [GraphDesktopViewItem] in drag operation during drag&drop. */
    private var dockingLocation: NewDockingLocation? = null

    /** Highlights the area where the currently dragged [GraphDesktopViewItem] would be dropped during drag&drop. */
    private val dockingTarget = DockingTarget()

    /** Uses for graphically "deleting" the source area of the currently dragged [GraphDesktopViewItem] by painting over it on the glass pane.*/
    private val dockingSource = DockingSource()

    private val dockingStartedHandler: EventHandler<DockingStartedEvent> = { handle(it) }
    private val dockingFinishedHandler: EventHandler<DockingFinishedEvent> = { handle(it) }

    private val dockingController = DockingController()

    /**
     * Holds the list of created [JSplitPanes][JSplitPane] for retrieving there current splitter locations
     * to re-establish them in [rebuildUI].
     */
    private val splitPanes = mutableListOf<JSplitPane>()

    /** Used to paint [dockingSource] and [dockingTarget] during drag&drop operations. */
    val glassPane: JPanel = GlassPane()

    // Visible for testing
    val contentComponent: JComponent get() = content.getComponent(0) as JComponent

    init {
        controller.view = this
        dockingController.view = this

        content.background = UIManager.getColor("Panel.background").darker()

        layout = FillAllLayout()

        glassPane.transferHandler = DockingTransferHandler(this)
        glassPane.isOpaque = false
        glassPane.isVisible = false

        add(content, DEFAULT_LAYER)
        add(glassPane, DRAG_LAYER)

        setLayer(content, DEFAULT_LAYER)
        setLayer(glassPane, DRAG_LAYER)

        eventBus.register(DockingStartedEvent::class, dockingStartedHandler)
        eventBus.register(DockingFinishedEvent::class, dockingFinishedHandler)
    }

    override fun dispose() {
        eventBus.unregister(dockingStartedHandler)
        eventBus.unregister(dockingFinishedHandler)
    }

    override fun isOptimizedDrawingEnabled(): Boolean {
        return false
    }

    /** ---- DnD control interface */

    private fun handle(event: DockingStartedEvent) {
        LOG.trace("Docking started")

        // The main graph is displayed by GraphEditViewSwing, but DockingStartedEvent originates from the
        // inner GraphNavigationViewSwing's header. getCurrentLocationOf() needs the outer view object,
        // not the inner GraphNavigationViewSwing.
        val target = if (event.graphDesktopViewItem is GraphNavigationViewSwing) {
            event.graphDesktopViewItem.controller.closeTarget
        } else {
            event.graphDesktopViewItem
        }
        val currentLocation = getCurrentLocationOf(target)
        dockingController.startDragging(currentLocation)

        with(dockingController.getBounds(currentLocation)) {
            dockingSource.setBounds(xInt, yInt, widthInt, heightInt)
        }
        glassPane.add(dockingSource)

        glassPane.isVisible = true
    }

    private fun handle(@Suppress("unused") event: DockingFinishedEvent) {
        LOG.trace("Docking finished")
        glassPane.isVisible = false
        glassPane.remove(dockingSource)
    }

    /** Called by the drag&drop handler during drag operations to highlight the possible drop area. */
    fun setDropLocation(dropLocation: Point) {
        val loc = dockingController.mouseDragged(dropLocation.x, dropLocation.y)

        if (loc == null) {
            if (dockingLocation != null) {
                glassPane.remove(dockingTarget)
            }
            dockingLocation = null
        } else {
            dockingTarget.setBounds(loc.area.xInt, loc.area.yInt, loc.area.widthInt, loc.area.heightInt)
            if (dockingLocation == null) {
                // Make sure the dockingTarget gets drawn ABOVE the dockingSource
                glassPane.add(dockingTarget, 0)
                repaint()
            }
            dockingLocation = loc
        }
    }

    /** Moves [item] to the location specified by [dockingLocation]. */
    fun handleDrop(item: GraphDesktopViewItem) {
        LOG.debug("handleDrop")
        if (dockingLocation == null) {
            return
        }
        move(item, dockingLocation!!)
        dockingLocation = null
    }

    /** ---- [DockingView] interface */

    override val viewWidth: Int get() = super.width

    override val viewHeight: Int get() = super.height

    override val columnsCount: Int get() = items.columnsCount

    override fun getRowsCount(column: Int): Int = items.getRowsCount(column)

    override fun getColumnWidth(column: Int): Int = items.getColumnWidth(column)

    override fun getRowHeight(column: Int, row: Int): Int = items.getRowHeight(column, row)

    fun getCurrentLocationOf(item: GraphDesktopViewItem): CurrentDockingLocation = items.getCurrentLocationOf(item)

    /** ---- [GraphDesktopView] interface */

    override fun showMainItem(item: GraphDesktopViewItem) {
        LOG.debug("Showing main item")
        items.addMainItem(item)
        rebuildUI()
    }

    override fun showChildItem(item: GraphDesktopViewItem) {
        if (items.isEmpty) {
            throw IllegalStateException("Adding child without main item")
        }
        LOG.debug("Showing child item")
        items.addChildItem(item)
        rebuildUI()
    }

    override fun closeChildItem(item: GraphDesktopViewItem) {
        LOG.debug("Closing child item")
        items.remove(item)
        rebuildUI()
    }

    override fun closeAll() {
        LOG.debug("Closing all")

        closeAllImpl()
        rebuildUI()
    }

    private fun closeAllImpl() {
        items.clear()
    }

    fun rebuildUI() {
        // Capture the splitPane's divider location
        val columnToSplitterLocation = mutableMapOf<Int, Int>()
        splitPanes.forEachIndexed { index, pane -> columnToSplitterLocation[index] = pane.dividerLocation }

        content.removeAll()
        splitPanes.clear()

        if (columnsCount > 0) {
            content.add(buildColumn(0))
        }
        content.revalidate()
        repaint()

        SwingUtilities.invokeLater {
            val proportion = 1.0 / items.columnsCount
            splitPanes.forEachIndexed { index, pane ->
                columnToSplitterLocation[index]
                    ?.let { location -> pane.dividerLocation = location }
                    ?: pane.setDividerLocation(proportion)
            }
            zoomViews(true)
        }
    }

    private fun buildColumn(column: Int): JComponent {
        val columnPanel = JPanel(GridLayout(0, 1))
        items.getRows(column).forEach { row -> columnPanel.add(row as JComponent)}
        return if (column == columnsCount - 1) {
            return columnPanel
        } else {
            JSplitPane(JSplitPane.HORIZONTAL_SPLIT, columnPanel, buildColumn(column + 1)).also {
                splitPanes.add(it)
            }
        }
    }

    // TODO Refactoring: This should probably not be here
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

    private fun zoomViews(includeMasterView: Boolean) {
        SwingUtilities.invokeLater {
            items.all.forEach { item ->
                if (item !== controller.mainDesktopViewItem || includeMasterView) {
                    item.drawingView?.navigator?.fitMaxNormal()
                }
            }
        }
    }

    private fun move(item: GraphDesktopViewItem, target: NewDockingLocation) {
        items.move(item, target)
        rebuildUI()
    }

    private class GlassPane : JPanel() {
        init {
            layout = null
        }
    }

    private class DockingTarget : JComponent() {
        companion object {
            private val COLOR = Color.ORANGE.let {
                Color(it.red, it.green, it.blue, 24)
            }
        }

        override fun paintComponent(g: Graphics) {
            super.paintComponent(g)
            g.color = COLOR
            g.fillRect(0, 0, width, height)
        }
    }

    private class DockingSource : JComponent() {
        companion object {
            private val COLOR = UIManager.getColor("Panel.background").darker()
        }

        override fun paintComponent(g: Graphics) {
            super.paintComponent(g)
            g.color = COLOR
            g.fillRect(0, 0, width, height)
        }
    }
}