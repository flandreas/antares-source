package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.base.checkState
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
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
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.graph.module.GraphModuleJvm
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
    eventBus: EventBus,
    viewManager: ViewManager,
    graphNavigationPanelFactory: GraphNavigationPanelFactory,
    private val scheduler: Scheduler
) : JPanel() {

    constructor(): this(
        BaseModule.eventBus,
        DrawViewModule.viewManager,
        GraphModuleJvm.graphNavigationPanelFactory,
        ExecutionModule.scheduler
    )

    private val mainSplitPane = JSplitPane(JSplitPane.HORIZONTAL_SPLIT)
    private val sidePanel = JPanel()
    private val slaveGraphNavigationPanels: MutableList<GraphNavigationPanel> = mutableListOf()

    var masterGraphPanel: GraphPanel? = null
        set(value) {
            checkState(field == null)
            checkNotNull(value)
            checkState(slaveGraphNavigationPanels.size == 0)
            field = value
            add(field)
        }

    init {
        sidePanel.layout = GridLayout(0, 1)
        layout = BorderLayout()
        eventBus.register(OpenSubGraphRequest::class, {
            if (it.quickMode) {
                val subGraphView = it.subGraphVerticeView.createSubGraphView()
                val graphCanvas = CanvasJvm({
                    val drawingView = EditModule.drawingViewFactory.invoke(subGraphView as Drawing<Component>, it)
                    drawingView
                })
                val drawingView = graphCanvas.view as DrawingView<GraphView<GraphElementView<*>>>
                drawingView.applicationContext = masterGraphPanel!!.editor.view.applicationContext

                addGraphNavigationPanel(
                        graphNavigationPanelFactory.create(
                                isRoot = false,
                                drawingView = drawingView,
                                viewManager = viewManager,
                                closeHandler = { closeGraphNavigationPanel(it) },
                                scheduler = scheduler
                        )
                )
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

    fun closeGraphNavigationPanel(graphNavigationPanel: GraphNavigationPanel) {
        graphNavigationPanel.dispose()
        slaveGraphNavigationPanels.remove(graphNavigationPanel)

        if (slaveGraphNavigationPanels.isEmpty()) {
            remove(mainSplitPane)
            mainSplitPane.remove(mainSplitPane)
            mainSplitPane.remove(sidePanel)
            add(masterGraphPanel)
        }

        sidePanel.remove(graphNavigationPanel)
        revalidate()
        repaint()
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
}