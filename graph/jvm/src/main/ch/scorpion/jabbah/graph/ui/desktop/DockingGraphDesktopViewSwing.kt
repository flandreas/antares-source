package ch.scorpion.jabbah.graph.ui.desktop

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.swing.FillAllLayout
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
import java.awt.*
import javax.swing.*

class DockingGraphDesktopViewSwing(
    private val controller: GraphDesktopViewController,
    private val maxRowsCount: Int = DEF_MAX_ROWS_COUNT,
    private val eventBus: EventBus = BaseModule.eventBus
) : JLayeredPane(), GraphDesktopView, DockingView {

    companion object {
        private val LOG by logger(DockingGraphDesktopViewSwing::class)
        private const val DEF_MAX_ROWS_COUNT = 2
    }

    private val _columns: MutableList<MutableList<GraphDesktopViewItem>> = mutableListOf()
    val columns: List<List<GraphDesktopViewItem>> get() = _columns

    private val columnPanels: MutableList<JPanel> = mutableListOf()

    private val content = JPanel(BorderLayout())

    private var dockingLocation: NewDockingLocation? = null
    private val dockingTarget = DockingTarget()

    private val dockingStartedHandler: EventHandler<DockingStartedEvent> = { handle(it) }
    private val dockingFinishedHandler: EventHandler<DockingFinishedEvent> = { handle(it) }

    private val dockingController = DockingController()

    val glassPane: JPanel = GlassPane()

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
        dockingController.startDragging(getCurrentLocationOf(target))

        glassPane.isVisible = true
    }

    private fun handle(event: DockingFinishedEvent) {
        LOG.trace("Docking finished")
        glassPane.isVisible = false
    }

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
                glassPane.add(dockingTarget)
            }
            dockingLocation = loc
        }
    }

    /** ---- [DockingView] interface */

    override val viewWidth: Int get() = super.width

    override val viewHeight: Int get() = super.height

    override val columnsCount: Int get() = _columns.size

    override fun getRowsCount(column: Int): Int = _columns[column].size

    override fun getColumnWidth(column: Int): Int = columnPanels[column].width

    override fun getRowHeight(column: Int, row: Int): Int {
        // TODO Support multiple rows
        return columnPanels[column].height
    }

    fun getCurrentLocationOf(item: GraphDesktopViewItem): CurrentDockingLocation {
        val column = _columns.indexOfFirst { it.contains(item) }
        val row = _columns[column].indexOfFirst { it === item }
        return CurrentDockingLocation(column, row)
    }

    /** ---- [GraphDesktopView] interface */

    override fun showMainItem(item: GraphDesktopViewItem) {
        LOG.debug("Showing main item")
        _columns.clear()
        _columns.add(mutableListOf(item))
        content.removeAll()
        val panel = createColumnPanel(item)
        columnPanels.add(panel)
        content.add(panel)

        content.revalidate()
        repaint()
    }

    override fun showChildItem(item: GraphDesktopViewItem) {
        if (_columns.isEmpty()) {
            throw IllegalStateException("Adding child without main item")
        }
        LOG.debug("Showing child item")

        if (_columns.size == 1) {
            // Only the mainItem has been added so far. Always create a child column.
            LOG.trace("--- Adding first child column, creating JSplitPane")
            _columns.add(mutableListOf(item))
            content.removeAll()
            val rightPanel = createColumnPanel(item)
            val splitPane = JSplitPane(JSplitPane.HORIZONTAL_SPLIT, columnPanels[0], rightPanel)
            columnPanels.add(rightPanel)
            content.add(splitPane)

            content.invalidate()
            content.revalidate()

            SwingUtilities.invokeLater {
                splitPane.setDividerLocation(.5)
                zoomViews(true)
            }
        } else {
            // Existing child columns
            val lastColumnIndex = _columns.size - 1
            val lastColumn = _columns[lastColumnIndex]
            if (lastColumn.size < maxRowsCount) {
                // Add item to the last column
                lastColumn.add(item)
                val panel = (content.getComponent(0) as JSplitPane).rightComponent as JPanel
                panel.add(item as JComponent)
            } else {
                // The last column is full, create a new one
                _columns.add(mutableListOf(item))
                // Replace right side of parent SplitPane with a new SplitPane
                val currentSplitPane = columnPanels[lastColumnIndex].parent as JSplitPane
                val newColumnPanel = createColumnPanel(item)
                columnPanels.add(newColumnPanel)

                val newSplitPane = JSplitPane(
                    JSplitPane.HORIZONTAL_SPLIT,
                    currentSplitPane.rightComponent,
                    newColumnPanel,
                )
                currentSplitPane.rightComponent = newSplitPane
            }

            content.revalidate()
            zoomViews(false)
        }
    }

    private fun createColumnPanel(item: GraphDesktopViewItem): JPanel =
        JPanel(GridLayout(0, 1)).apply { add(item as JComponent) }

    override fun closeChildItem(item: GraphDesktopViewItem) {
        LOG.debug("Closing child item")

        val column = columns.indexOfFirst { it.contains(item) }
        if (column < 0) {
            return
        }

        val columnPanel = columnPanels[column]
        if (columnPanel.componentCount > 1) {
            _columns[column].remove(item)
            columnPanel.remove(item as JComponent)
        } else {
            _columns.removeAt(column)
            if (columnPanel.parent is JSplitPane) {
                // Replace the SplitPane with the column Panel that does NOT contain the removed item
                val splitPane = columnPanel.parent as JSplitPane
                val remainingPanel = if (splitPane.leftComponent === columnPanel) {
                    splitPane.rightComponent as JPanel
                } else if (splitPane.rightComponent === columnPanel) {
                    splitPane.leftComponent as JPanel
                } else {
                    throw IllegalStateException("Cannot identify column panel's parent")
                }

                if (splitPane.parent is JSplitPane) {
                    val parent = splitPane.parent as JSplitPane
                    if (parent.leftComponent === splitPane) {
                        parent.leftComponent = remainingPanel
                    } else {
                        parent.rightComponent = remainingPanel
                    }
                } else {
                    val parent = splitPane.parent as JComponent
                    parent.removeAll()
                    parent.add(remainingPanel)
                }

            } else {
                val parent = columnPanel.parent as JComponent
                parent.removeAll()
            }
        }

        content.revalidate()
        repaint()
    }

    override fun closeAll() {
        LOG.debug("Closing all")

        _columns.clear()
        columnPanels.clear()
        content.removeAll()

        content.revalidate()
        repaint()
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
            _columns.flatten().forEach { item ->
                if (item !== controller.mainDesktopViewItem || includeMasterView) {
                    item.drawingView?.navigator?.fitMaxNormal()
                }
            }
        }
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
}