package ch.scorpion.jabbah.graph.container

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.graph.library.LibraryTreeView
import ch.scorpion.jabbah.graph.ui.ContainerLibraryElementIcon
import ch.scorpion.jabbah.graph.view.ControlViewSourceEvent
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.editor.GraphPortViewEvent
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.graph.view.port.PortFactory
import javax.swing.*
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeCellRenderer
import javax.swing.tree.TreeModel

/**
 * Displays the objects that can be dragged into a [ContainerDrawing], such as [PortViewComponent]s and controls.
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

	/** Fills an manages the [DynamicTreeModel] displayed by this [ContainerTreeView].*/
    var containerTree: ContainerTree? = null
		private set

    init {
        setRowHeight(22)
        rootVisible = false
        setCellRenderer(ContainerTreeCellRenderer())
        showsRootHandles = true
        dragEnabled = true
        dropMode = DropMode.ON

        eventBus.register(GraphPortViewEvent::class) {
	        when(it.type) {
		        GraphPortViewEvent.Type.ADD -> containerTree?.addGraphPortView(it.graphPortView)
		        GraphPortViewEvent.Type.REMOVE -> containerTree?.removeGraphPortView(it.graphPortView.model!!.name!!)
	        }
        }

	    eventBus.register(ControlViewSourceEvent::class) {
		    when(it.type) {
			    ControlViewSourceEvent.Type.ADD -> containerTree?.addControlViewSource(it.source)
			    ControlViewSourceEvent.Type.REMOVE -> containerTree?.removeControlViewSource(it.source.controlId!!)
		    }
	    }
    }

    /** Updates the [TreeModel] by comparing data from [GraphView] and [ContainerDrawing].*/
    fun update(graphView: GraphView<*>, containerDrawing: ContainerDrawing) {
	    containerTree = ContainerTree(portFactory, styleProvider, graphView, containerDrawing)
	    model = containerTree?.treeModel
    }

    private inner class ContainerTreeCellRenderer : DefaultTreeCellRenderer() {
        private val iconCache: MutableMap<String, Icon> = mutableMapOf()
	    private val folderIcon = ImageIcon(LibraryTreeView::class.java.getResource("/img/folder-20.png"))
	    private val subGraphIcon = ContainerLibraryElementIcon()

	    override fun getTreeCellRendererComponent(tree: JTree?, value: Any?, selected: Boolean, expanded: Boolean, leaf: Boolean, row: Int, hasFocus: Boolean): java.awt.Component {
            val label = super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus) as JLabel

		    if (value is DefaultMutableTreeNode && value.userObject is AbstractContainerTreeItem) {
			    val userObject = value.userObject as AbstractContainerTreeItem
			    label.text = userObject.description
			    label.icon = when (userObject.type) {
				    ContainerTreeItemType.Leaf -> getIcon((value.userObject as ContainerTreeLeafItem).iconPath)
				    ContainerTreeItemType.SubGraph -> subGraphIcon
				    else -> folderIcon
			    }
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
