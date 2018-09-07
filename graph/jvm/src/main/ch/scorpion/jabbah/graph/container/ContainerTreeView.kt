package ch.scorpion.jabbah.graph.container

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.graph.library.LibraryTreeView
import ch.scorpion.jabbah.graph.model.Vertice
import ch.scorpion.jabbah.graph.model.Port
import ch.scorpion.jabbah.graph.view.*
import ch.scorpion.jabbah.graph.view.editor.GraphPortViewEvent
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.graph.view.port.PortFactory
import javax.swing.*
import javax.swing.tree.*

/**
 * Displays the objects that can be dragged into a [ContainerDrawing], such as [PortViewComponent]s and controls.
 * TODO Refactor: Duplicate code for handled event and view types
 */
open class ContainerTreeView(
    private val portFactory: PortFactory,
    private val styleProvider: StyleProvider,
    eventBus: EventBus
) : JTree() {
    constructor(): this(GraphViewModule.portFactory, DrawStyleModule.styleProvider, BaseModule.eventBus)

	companion object {
		private val LOG = logger(ContainerTreeView::class)
	}

    /** The root node that contains the [PortViewComponent]s. Will be set in [update]. */
    private var portsNode: DefaultMutableTreeNode? = null

    /** The root node that contains the [ControlViewSource]s. Will be set in [update]. */
    private var controlsNode: DefaultMutableTreeNode? = null

    init {
        setRowHeight(22)
        rootVisible = false
        setCellRenderer(ContainerTreeCellRenderer())
        showsRootHandles = true
        dragEnabled = true
        dropMode = DropMode.ON

        eventBus.register(GraphPortViewEvent::class) {
	        when(it.type) {
		        GraphPortViewEvent.Type.ADD -> addGraphPortView(it.graphPortView)
		        GraphPortViewEvent.Type.REMOVE -> removeGraphPortView(it.graphPortView.model!!.name!!)
	        }
        }

	    eventBus.register(ControlViewSourceEvent::class) {
		    when(it.type) {
			    ControlViewSourceEvent.Type.ADD -> addControlViewSource(it.source)
			    ControlViewSourceEvent.Type.REMOVE -> removeControlViewSource(it.source.controlId!!)
		    }
	    }
    }

    /** Updates the [TreeModel] by comparing data from [GraphView] and [ContainerDrawing].*/
    fun update(graphView: GraphView<*>, containerDrawing: ContainerDrawing) {
        portsNode = DefaultMutableTreeNode(Translations.getString("graph.component.ports"))
        controlsNode = DefaultMutableTreeNode(Translations.getString("graph.component.controls"))
        model = createTreeModel(graphView, containerDrawing)
    }

    /** Creates a tree node for the specified [GraphPortView] and adds it to this [ContainerTreeView] .*/
    fun addGraphPortView(graphPortView: GraphPortView<*>) {
        val item = ContainerTreeItem(
            { graphPortView.model!!.name!! },
            { "${graphPortView.model!!.portType} ${graphPortView.model!!.name!!}" },
            { portFactory.createPortViewComponent(portFactory.createPortView(portFactory.createSubGraphPort(graphPortView.model!!)))},
            { graphPortView.iconPath }
        )
        portsNode!!.add(DefaultMutableTreeNode(item))
        (model as DefaultTreeModel).nodesWereInserted(portsNode, intArrayOf(portsNode!!.childCount - 1))
    }

    /** Removes the [PortViewComponent] for the [Port] with the specified name from this [ContainerTreeView]. */
    fun removeGraphPortView(portName: String) {
        val index = findGraphPortViewIndex(portName)
        if (index != null) {
            val child = portsNode!!.getChildAt(index)
            portsNode!!.remove(index)
            (model as DefaultTreeModel).nodesWereRemoved(portsNode, intArrayOf(index), arrayOf(child))
        }
    }

    /** Creates a tree node for the specified [ControlViewSource] and adds it to this [ContainerTreeView] .*/
    fun addControlViewSource(source: ControlViewSource<Vertice>) {
        val item = ContainerTreeItem(
            { source.controlId!! },
            { source.controlName},
            { ControlViewComponent(styleProvider, source.createControlView()) },
            { source.iconPath }
        )
        controlsNode!!.add(DefaultMutableTreeNode(item))
        (model as DefaultTreeModel).nodesWereInserted(controlsNode, intArrayOf(controlsNode!!.childCount - 1))
    }

    /** Removes the [ControlViewSource] with the specified ID from this [ContainerTreeView]. */
    fun removeControlViewSource(controlId: String) {
        val index = findControlViewSourceIndex(controlId)
        if (index != null) {
            val child = controlsNode!!.getChildAt(index)
            controlsNode!!.remove(index)
            (model as DefaultTreeModel).nodesWereRemoved(controlsNode, intArrayOf(index), arrayOf(child))
        }
    }

    /** Returns the [TreeNode] with the [PortViewComponent] for the [Port] with the specified name. */
    protected fun getPortsTreeNode(portName: String): DefaultMutableTreeNode? {
        if (portsNode != null) {
            val index = findGraphPortViewIndex(portName)
            if (index != null) {
                return portsNode!!.getChildAt(index) as DefaultMutableTreeNode
            }
        }
        return null
    }

    private fun findGraphPortViewIndex(portName: String): Int? {
        if (portsNode != null) {
            for (index in 0 until portsNode!!.childCount) {
                val item = (portsNode!!.getChildAt(index) as DefaultMutableTreeNode).userObject as ContainerTreeItem
                if (item.id.invoke() == portName) {
                    return index
                }
            }
        }
        return null
    }

    private fun findControlViewSourceIndex(controlId: String): Int? {
        if (portsNode != null) {
            for (index in 0 until controlsNode!!.childCount) {
                val item = (controlsNode!!.getChildAt(index) as DefaultMutableTreeNode).userObject as ContainerTreeItem
                if (item.id.invoke() == controlId) {
                    return index
                }
            }
        }
        return null
    }

    private fun createTreeModel(graphView: GraphView<*>, containerDrawing: ContainerDrawing): TreeModel {
        val root = DefaultMutableTreeNode("Container")
        fillGraphPortViews(graphView, containerDrawing)
        fillControlViewSources(graphView, containerDrawing)
        root.add(portsNode)
        root.add(controlsNode)
        return DefaultTreeModel(root)
    }

    /**
     * Adds all [GraphPortView]s of the [GraphView] to this [ContainerTreeView] that are not contained
     * in the [ContainerDrawing].
     */
    private fun fillGraphPortViews(graphView: GraphView<*>, containerDrawing: ContainerDrawing) {
        graphView.getGraphPortViews()
            .filter { containerDrawing.getPortViewComponent(it.model!!.name!!) == null }
            .forEach { addGraphPortView(it) }
    }

    /**
     * Adds all [ControlViewSource]s of the [GraphView] to this [ContainerTreeView] that are not contained
     * in the [ContainerDrawing].
     */
    private fun fillControlViewSources(graphView: GraphView<*>, containerDrawing: ContainerDrawing) {
        graphView.getControlViewSources()
            .filter { containerDrawing.getControlViewComponent(it.controlId!!) == null }
            .forEach { addControlViewSource(it) }
    }

    private inner class ContainerTreeCellRenderer : DefaultTreeCellRenderer() {
        private val iconCache: MutableMap<String, Icon> = mutableMapOf()
	    private val folderIcon = ImageIcon(LibraryTreeView::class.java.getResource("/img/folder-20.png"))

	    override fun getTreeCellRendererComponent(tree: JTree?, value: Any?, selected: Boolean, expanded: Boolean, leaf: Boolean, row: Int, hasFocus: Boolean): java.awt.Component {
            val treeNode = value as DefaultMutableTreeNode
            val label = super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus) as JLabel
            if (treeNode.userObject is ContainerTreeItem) {
                label.text = (treeNode.userObject as ContainerTreeItem).description.invoke()
                if ((treeNode.userObject as ContainerTreeItem).iconPath.invoke() != "") {
                    label.icon = getIcon((treeNode.userObject as ContainerTreeItem).iconPath.invoke())
                }
            } else if (treeNode === portsNode || treeNode === controlsNode) {
	            label.icon = folderIcon
            }
            return label
        }

        private fun getIcon(iconPath: String): Icon {
            try {
                return iconCache.getOrPut(iconPath) { ImageIcon(ContainerTreeView::class.java.getResource(iconPath)) }
            } catch (e: Exception) {
                LOG.value.error("Could not load icon $iconPath")
                throw e
            }
        }
    }
}

/** Uses as user object in [TreeNode]s. */
class ContainerTreeItem(
    val id: () -> String,
    val description: () -> String,
    val factory: () -> Component,
    val iconPath: () -> String
)