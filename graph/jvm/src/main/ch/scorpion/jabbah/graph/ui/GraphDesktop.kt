package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.app.ApplicationDataEvent
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.DrawableContainerEvent
import ch.scorpion.jabbah.draw.container.DrawableContainerAdapter
import ch.scorpion.jabbah.draw.graphics.CompositeColor
import ch.scorpion.jabbah.draw.graphics.ReferenceColorEvent
import ch.scorpion.jabbah.draw.graphics.ReferenceColorSequenceProvider
import ch.scorpion.jabbah.draw.view.CanvasJvm
import ch.scorpion.jabbah.draw.view.DrawViewModule
import ch.scorpion.jabbah.draw.view.ViewManager
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.DrawingViewContent
import ch.scorpion.jabbah.edit.model.ComponentMessage
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.execution.module.ExecutionModule
import ch.scorpion.jabbah.execution.scheduler.Scheduler
import ch.scorpion.jabbah.graph.module.GraphModuleJvm
import ch.scorpion.jabbah.graph.view.GraphElementView
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.vertice.OpenSubGraphRequest
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeView
import java.awt.BorderLayout
import java.awt.Color
import java.awt.GridLayout
import javax.swing.JPanel
import javax.swing.JSplitPane
import javax.swing.SwingUtilities

/**
 * Manages a master [GraphEditPanel] and multiple slave [GraphNavigationPanel]s.
 */
class GraphDesktop(
	private val graphEditPanel: GraphEditPanel,
	private val eventBus: EventBus = BaseModule.eventBus,
	viewManager: ViewManager = DrawViewModule.viewManager,
	graphNavigationPanelFactory: GraphNavigationPanelFactory = GraphModuleJvm.graphNavigationPanelFactory,
	private val scheduler: Scheduler = ExecutionModule.scheduler
) : JPanel() {

    companion object {
        private val LOG by logger(GraphDesktop::class)
    }

	private val mainSplitPane = JSplitPane(JSplitPane.HORIZONTAL_SPLIT)

    /** The [JPanel] at the right side containing all slave views, if any. */
    private val sidePanel = JPanel()

    /** Contains all open [GraphNavigationPanel]s that are not the main one.*/
    private val slaveGraphNavigationPanels: MutableList<GraphNavigationPanel> = mutableListOf()

    /** Used for determining a [CompositeColor] for referencing a [SubGraphVerticeView] and its open [GraphNavigationPanel].*/
    private var referenceColorSequence = ReferenceColorSequenceProvider.provide()

    /** Associates [SubGraphVerticeView] and their open [GraphNavigationPanel]s.*/
    private val associations = mutableListOf<Association>()

    /** Closes all slave panels when the edited root [GraphView] has changed.*/
    private val editedGraphViewEventHandler: (EditedGraphViewEvent) -> Unit = {
        it.oldGraphView?.removeDrawableContainerListener(removeListener)
        closeAllSlavesImpl()
        it.newGraphView?.addDrawableContainerListener(removeListener)
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

    init {
        mainSplitPane.border = null
        sidePanel.layout = GridLayout(0, 1)
        layout = BorderLayout()
	    background = Color.GRAY

	    eventBus.register(EditedGraphViewEvent::class, editedGraphViewEventHandler)

	    eventBus.register(ApplicationDataEvent::class, {
		    if (it.newData == null) {
			    closeAll()
		    } else if (it.oldData == null) {
			    establishSingleView()
			    invalidate()
			    revalidate()
			    repaint()
		    }
	    })

        // Replace reference color in all Associations
        eventBus.register(ReferenceColorEvent::class, { event ->
            val newAssociations = associations.map { assoc -> assoc.copy(refColor = event.getNewColorFor(assoc.refColor)!!) }
            associations.clear()
            associations.addAll(newAssociations)
            associations.forEach { assoc ->
                assoc.panel.contextColor = assoc.refColor
                event.replacements.forEach { assoc.panel.drawingView.highlighter.replaceColor(it.oldColor, it.newColor) }
            }
            event.replacements.forEach { graphEditPanel.graphNavigationPanel.drawingView.highlighter.replaceColor(it.oldColor, it.newColor) }

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

	    add(graphEditPanel)
    }

    fun dispose() {
	    graphEditPanel.dispose()
    }

    private fun addGraphNavigationPanel(panel: GraphNavigationPanel) {
        if (slaveGraphNavigationPanels.isEmpty()) {
            remove(graphEditPanel)
            sidePanel.add(panel)
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
		closeAllSlavesImpl()
        establishSingleView()
        revalidate()
        repaint()
    }

	private fun closeAll() {
		closeAllSlavesImpl()
		removeAll()
		revalidate()
		repaint()
	}

	private fun closeAllSlavesImpl() {
		slaveGraphNavigationPanels.forEach {
			deassociate(it)
			it.dispose()
		}
		slaveGraphNavigationPanels.clear()
		sidePanel.removeAll()
	}

    /** Establish the UI for displaying only the root [GraphPanel].*/
    private fun establishSingleView() {
        remove(mainSplitPane)
        mainSplitPane.remove(mainSplitPane)
        mainSplitPane.remove(sidePanel)
        add(graphEditPanel)
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
                deassociate(assoc, content)
            }
        }
    }

    private fun deassociate(assoc: Association, content: DrawingViewContent<*>) {
        content.highlighter.unhighlight(assoc.ref)
        referenceColorSequence.free(assoc.refColor)
        associations.remove(assoc)
    }

    private fun associationOf(panel: GraphNavigationPanel): Association? =
            associations.firstOrNull { assoc -> assoc.panel == panel }

    private fun zoomViews(includeMasterView: Boolean) {
        SwingUtilities.invokeLater {
            if (includeMasterView) {
                graphEditPanel.graphNavigationPanel.drawingView.navigator.fitMaxNormal()
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
        if (graphEditPanel.graphNavigationPanel.drawingView.drawing.contains(vv)) {
            return graphEditPanel.graphNavigationPanel
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