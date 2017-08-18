package ch.scorpion.jabbah.graph.container

import ch.scorpion.jabbah.app.ApplicationDataEvent
import ch.scorpion.jabbah.app.ToolBar
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.collection.ImmutableList
import ch.scorpion.jabbah.base.collection.toImmutableList
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.DrawableContainerEvent
import ch.scorpion.jabbah.draw.view.FocusPanel
import ch.scorpion.jabbah.draw.container.DrawableContainerAdapter
import ch.scorpion.jabbah.draw.view.ViewManager
import ch.scorpion.jabbah.edit.*
import ch.scorpion.jabbah.edit.model.polyline.PolylineComponent
import ch.scorpion.jabbah.edit.model.polyline.PolylineTool
import ch.scorpion.jabbah.edit.model.rectangle.EllipseComponent
import ch.scorpion.jabbah.edit.model.rectangle.RectangleComponent
import ch.scorpion.jabbah.edit.model.rectangle.RectangleTool
import ch.scorpion.jabbah.edit.model.text.LabelComponent
import ch.scorpion.jabbah.edit.model.text.LabelTool
import ch.scorpion.jabbah.edit.module.EditModuleJvm
import ch.scorpion.jabbah.graph.GraphApplicationContext
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.module.GraphModuleJvm
import ch.scorpion.jabbah.graph.view.GraphView
import java.awt.BorderLayout
import javax.swing.*

/**
 * A [JPanel] for editing the outside [ContainerDrawing] of a [GraphView].
 */
class ContainerPanel(
        val editor: ContainerEditor,
        propertySheetFactory: PropertySheetPanelFactory,
        eventBus: EventBus,
        viewManager: ViewManager
) : JPanel() {

    constructor(editor: ContainerEditor, viewManager: ViewManager): this(editor, EditModuleJvm.propertySheetPanelFactory, BaseModule.eventBus, viewManager)

    /** The [TreeView] containing all objects of [graphView] that have not yet been added to the [ContainerDrawing].*/
    private val treeView = GraphModuleJvm.containerTreeViewFactory.invoke()

    /** The [GraphView] whose outside view is edited by this [ContainerPanel]. Defined in [setData]. */
    private var graphView: GraphView<*>? = null

    private val balancer = Balancer()

    private val propertyPanel: ComponentPropertyPanel

    val toolbars: ImmutableList<JToolBar> = listOf(createToolbar(editor)).toImmutableList()

    init {
        editor.view.navigator.setZoomFactor(2.0)

        eventBus.register(ApplicationDataEvent::class, {
            val metaGraph = it.newData as MetaGraph
            setData(metaGraph.graph!!.graphView!!, metaGraph.containerDrawing!!)
        })

        propertyPanel = ComponentPropertyPanel(editor, propertySheetFactory, eventBus)

        // TODO Toolbar

        treeView.transferHandler = ContainerTransferHandler()
        (editor.view.canvas as JPanel).transferHandler = ComponentTransferHandler(editor, eventBus, ComponentTransferable.FLAVOR)

        buildUI(viewManager)
    }

    fun initialize() {
        editor.view.initialize()
        editor.view.applicationContext = GraphApplicationContext()
    }

    fun setData(graphView: GraphView<*>, containerDrawing: ContainerDrawing) {
        this.graphView = graphView
        editor.view.drawing.removeDrawableContainerListener(balancer)
        editor.view.drawing = containerDrawing
        treeView.update(graphView, containerDrawing)
        editor.view.drawing.addDrawableContainerListener(balancer)
    }

    fun setGraphName(graphName: String) {
        //graphView!!.graph!!.name = graphName
        (editor.drawing as ContainerDrawing).model.name = graphName
    }

    private fun buildUI(viewManager: ViewManager) {
        layout = BorderLayout()

        val treeViewScrollPanel = JScrollPane(treeView, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER)

        val leftSplitPane = JSplitPane(JSplitPane.VERTICAL_SPLIT)
        leftSplitPane.dividerLocation = 600
        leftSplitPane.add(treeViewScrollPanel)
        leftSplitPane.add(propertyPanel)

        val mainSplitPane = JSplitPane(JSplitPane.HORIZONTAL_SPLIT)
        mainSplitPane.dividerLocation = 250
        mainSplitPane.add(leftSplitPane)
        mainSplitPane.add(FocusPanel(editor.view.canvas as JComponent, editor.view, viewManager))

        add(mainSplitPane)
    }

    private fun createToolbar(editor: Editor): JToolBar {
        val toolbar = ToolBar(editor)

        toolbar.addTool(editor.currentTool, "/img/pointer.gif", Translations.getString("edit.tool.select"))
        toolbar.addTool(LabelTool(editor, { LabelComponent() }), "/img/text.gif", Translations.getString("edit.component.label"))
        toolbar.addTool(RectangleTool(editor, { RectangleComponent() }), "/img/rectangle.png", Translations.getString("edit.component.rectangle"))
        toolbar.addTool(RectangleTool(editor, { EllipseComponent() }), "/img/ellipse.png", Translations.getString("edit.component.ellipse"))
        toolbar.addTool(PolylineTool(editor, { PolylineComponent() }), "/img/polyline.gif", Translations.getString("edit.component.polyline"))

        return toolbar
    }

    /**
     * Balances the contents of the [ContainerTreeView] and the [ContainerDrawing] such that each object
     * is always contained only in one of them, but not in both.
     */
    private inner class Balancer : DrawableContainerAdapter<Component> () {

        /** Removes the object that has been added to the [ContainerDrawing] from the [ContainerTreeView].*/
        override fun drawableAdded(event: DrawableContainerEvent<Component>) {
            if (event.child is PortViewComponent<*>) {
                treeView.removeGraphPortView((event.child as PortViewComponent<*>).port.name!!)
            }
            if (event.child is ControlViewComponent) {
                treeView.removeControlViewSource((event.child as ControlViewComponent).controlView!!.controlId!!)
            }
        }

        /**
         * Adds the object of the main [GraphView] to the [ContainerTreeView] when its corresponding object
         * has been removed from the [ContainerDrawing].
         */
        override fun drawableRemoved(event: DrawableContainerEvent<Component>) {
            if (event.child is PortViewComponent<*>) {
                val graphPortView = graphView!!.getGraphPortView((event.child as PortViewComponent<*>).port.name!!)
                if (graphPortView != null) {
                    treeView.addGraphPortView(graphPortView)
                }
            }
            if (event.child is ControlViewComponent) {
                val cvs = graphView!!.getControlViewSource((event.child as ControlViewComponent).controlView!!.controlId!!)
                if (cvs != null) {
                    treeView.addControlViewSource(cvs)
                }
            }
        }
    }
}