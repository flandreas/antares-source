package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.base.checkState
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.DrawableContainerEvent
import ch.scorpion.jabbah.draw.container.DrawableContainerAdapter
import ch.scorpion.jabbah.draw.graphics.CompositeColor
import ch.scorpion.jabbah.draw.graphics.ReferenceColorSequenceProvider
import ch.scorpion.jabbah.draw.style.ThemeEvent
import ch.scorpion.jabbah.draw.view.DrawViewModule
import ch.scorpion.jabbah.draw.view.ViewManager
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.execution.module.ExecutionModule
import ch.scorpion.jabbah.execution.scheduler.Scheduler
import ch.scorpion.jabbah.graph.view.GraphElementView
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.vertice.OpenSubGraphRequest
import ch.scorpion.jabbah.draw.view.CanvasJvm
import ch.scorpion.jabbah.edit.DrawingViewContent
import ch.scorpion.jabbah.edit.model.ComponentMessage
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.graph.module.GraphModuleJvm
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeView
import java.awt.BorderLayout
import java.awt.GridLayout
import javax.swing.JPanel
import javax.swing.JSplitPane
import javax.swing.JToolBar
import javax.swing.SwingUtilities

/**
 * Manages a master [GraphPanel] and multiple slave [GraphNavigationPanel]s.
 */
class GraphDesktop(
    private val eventBus: EventBus = BaseModule.eventBus,
    viewManager: ViewManager = DrawViewModule.viewManager,
    graphNavigationPanelFactory: GraphNavigationPanelFactory = GraphModuleJvm.graphNavigationPanelFactory,
    private val scheduler: Scheduler = ExecutionModule.scheduler
) : JPanel() {

    private val LOG by logger(GraphDesktop::class)

    private val mainSplitPane = JSplitPane(JSplitPane.HORIZONTAL_SPLIT)

    private val sidePanel = JPanel()

    /** Contains all open [GraphNavigationPanel]s that are not the main one.*/
    private val slaveGraphNavigationPanels: MutableList<GraphNavigationPanel> = mutableListOf()

    /** Used for determining a [CompositeColor] for referencing a [SubGraphVerticeView] and its open [GraphNavigationPanel].*/
    private var referenceColorSequence = ReferenceColorSequenceProvider.provide()

    /** Associates [SubGraphVerticeView] and their open [GraphNavigationPanel]s.*/
    private val associations = mutableListOf<Association>()

    /** Closes all slave panels when the edited root [GraphView] has changed.*/
    private val editedGraphViewEventHandler: (EditedGraphViewEvent) -> Unit = {
        it.oldGraphView.removeDrawableContainerListener(removeListener)
        closeAllSlaves()
        it.newGraphView.addDrawableContainerListener(removeListener)
    }

    /** Closes an open [GraphNavigationPanel] when the corresponding [SubGraphVerticeView] has been removed.*/
    private val removeListener = object : DrawableContainerAdapter<GraphElementView<*>>() {
        override fun drawableRemoved(event: DrawableContainerEvent<GraphElementView<*>>) {
            associations.firstOrNull{ it.ref === event.child }?.let {
                assoc -> {
                    closeGraphNavigationPanel(assoc.panel)
                    deassociate(assoc, assoc.sourcePanel.drawingView.content)
                }.invoke()
            }
        }
    }

    var masterGraphPanel: GraphPanel? = null
        set(value) {
            checkState(field == null)
            checkNotNull(value)
            checkState(slaveGraphNavigationPanels.size == 0)
            field?.let { eventBus.unregister(EditedGraphViewEvent::class, editedGraphViewEventHandler)}
            field = value
            field?.let { eventBus.register(EditedGraphViewEvent::class, editedGraphViewEventHandler) }
            add(field)
        }

    init {
        mainSplitPane.border = null
        sidePanel.layout = GridLayout(0, 1)
        layout = BorderLayout()

        // [ReferenceColorSequenceProvider]s can't deal with changing [Theme]s, so close all open slaves
        // and create a new [ReferenceColorSequence] when the current [Theme] has changed.
        eventBus.register(ThemeEvent::class, {
            closeAllSlaves()
            referenceColorSequence = ReferenceColorSequenceProvider.provide()
        })

        eventBus.register(OpenSubGraphRequest::class, { request ->
            if (request.quickMode) {
                val assoc = associations.firstOrNull{ it.ref == request.subGraphVerticeView}
                if (assoc != null) {
                    eventBus.post(ComponentMessage(source = assoc.ref, messageKey = "graph.vertice.alreadyOpen.msg"))
                    return@register
                }

                val subGraphView = request.subGraphVerticeView.createSubGraphView()
                val graphCanvas = CanvasJvm({
                    val drawingView = EditModule.drawingViewFactory.invoke(subGraphView as Drawing<Component>, it)
                    drawingView
                })
                val drawingView = graphCanvas.view as DrawingView<GraphView<GraphElementView<*>>>

                val refColor = referenceColorSequence.next()
                panelContaining(request.subGraphVerticeView)?.let {
                    val newPanel = graphNavigationPanelFactory.create(
                            isRoot = false,
                            drawingView = drawingView,
                            viewManager = viewManager,
                            closeHandler = { closeGraphNavigationPanel(it) },
                            contextColor = refColor,
                            scheduler = scheduler
                    )
                    associations.add(Association(it, request.subGraphVerticeView, newPanel, refColor))

                    addGraphNavigationPanel(newPanel)

                    it.drawingView.highlighter.highlight(request.subGraphVerticeView, refColor)
                    it.drawingView.repaint()
                } ?: LOG.error("GraphDesktop: SubGraphVerticeView for OpenSubGraphRequest not found in open panels")
            }
        })
    }

    fun dispose() {
        masterGraphPanel?.dispose()
    }

    fun getToolBars(): List<JToolBar> {
        return masterGraphPanel!!.toolbars
    }

    private fun addGraphNavigationPanel(panel: GraphNavigationPanel) {
        if (slaveGraphNavigationPanels.isEmpty()) {
            remove(masterGraphPanel)
            sidePanel.add(panel)
            mainSplitPane.leftComponent = masterGraphPanel
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
            sidePanel.add(panel)
            sidePanel.invalidate()
            revalidate()
            zoomViews(false)
        }
        slaveGraphNavigationPanels.add(panel)
    }

    private fun closeGraphNavigationPanel(panel: GraphNavigationPanel) {
        deassociate(panel)

        panel.dispose()
        slaveGraphNavigationPanels.remove(panel)

        if (slaveGraphNavigationPanels.isEmpty()) {
            establishSingleView()
        }

        sidePanel.remove(panel)
        revalidate()
        repaint()
    }

    /** Closes all open slave [GraphNavigationPanel]s.*/
    private fun closeAllSlaves() {
        slaveGraphNavigationPanels.forEach {
            deassociate(it)
            it.dispose()
        }
        slaveGraphNavigationPanels.clear()
        establishSingleView()
        sidePanel.removeAll()
        revalidate()
        repaint()
    }

    /** Establish the UI for displaying only the root [GraphPanel].*/
    private fun establishSingleView() {
        remove(mainSplitPane)
        mainSplitPane.remove(mainSplitPane)
        mainSplitPane.remove(sidePanel)
        add(masterGraphPanel)
    }

    /**
     * Deassociate the specified open [GraphNavigationPanel] when it is being closed.
     * Checks all existing [Association]s for the [DrawingViewContent]s that contains the associating [SubGraphVerticeView],
     * and removes that [Association].
     */
    private fun deassociate(panel: GraphNavigationPanel) {
        associationOf(panel).let { assoc ->
            val content = assoc!!.sourcePanel.findContent { it.drawing.contains(assoc.ref) }
            if (content != null) {
                deassociate(assoc, content!!)
            }
        }
    }

    private fun deassociate(assoc: Association, content: DrawingViewContent<*>) {
        content.highlighter.unhighlight(assoc.ref)
        referenceColorSequence.free(assoc.refColor)
        associations.remove(assoc)
    }

    private fun associationOf(panel: GraphNavigationPanel): Association? {
        return associations.firstOrNull { assoc -> assoc.panel == panel }
    }

    private fun zoomViews(includeMasterView: Boolean) {
        SwingUtilities.invokeLater {
            if (includeMasterView) {
                masterGraphPanel!!.graphNavigationPanel.drawingView.navigator.fitMaxNormal()
            }
            for (panel in slaveGraphNavigationPanels) {
                panel.drawingView.navigator.fitMaxNormal()
            }
        }
    }

    /**
     * Finds the [GraphNavigationPanel] that contains the specified [SubGraphVerticeView].
     */
    private fun panelContaining(vv: SubGraphVerticeView<*>): GraphNavigationPanel? {
        if (masterGraphPanel!!.graphNavigationPanel.drawingView.drawing.contains(vv)) {
            return masterGraphPanel!!.graphNavigationPanel
        }
        return slaveGraphNavigationPanels.firstOrNull { it.drawingView.drawing.contains(vv) }
    }

    /**
     * Maintains an association between a [SubGraphVerticeView] and the [GraphNavigationPanel] that has been opened
     * in this [GraphDesktop], along with the [CompositeColor] that is used as a visual reference.
     */
    private data class Association(
            val sourcePanel: GraphNavigationPanel,
            val ref: SubGraphVerticeView<*>,
            val panel: GraphNavigationPanel,
            val refColor: CompositeColor)
}