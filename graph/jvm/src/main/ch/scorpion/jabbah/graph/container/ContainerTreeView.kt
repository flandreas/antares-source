package ch.scorpion.jabbah.graph.container

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.swing.UiUtil
import ch.scorpion.jabbah.base.swing.dynamictree.DynamicTreeModel
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.graph.model.module.GraphModelModule
import ch.scorpion.jabbah.graph.ui.ContainerLibraryElementIcon
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.graph.model.port.PortFactory
import ch.scorpion.jabbah.graph.view.port.PortViewFactory
import javax.swing.*
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeCellRenderer
import javax.swing.tree.TreeModel

/**
 * Displays the objects that can be dragged into a [ContainerDrawing], such as [PortViewComponent]s and controls.
 */
open class ContainerTreeView(
	private val portFactory: PortFactory = GraphModelModule.portFactory,
	private val portViewFactory: PortViewFactory = GraphViewModule.portViewFactory,
	private val styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	protected val eventBus: EventBus = BaseModule.eventBus
) : JTree() {

	companion object {
		private val LOG = logger(ContainerTreeView::class)
	}

	/** Fills an manages the [DynamicTreeModel] displayed by this [ContainerTreeView].*/
    protected var containerTree: ContainerTree? = null
		private set

    init {
        setRowHeight(22)
        rootVisible = false
        setCellRenderer(ContainerTreeCellRenderer())
        showsRootHandles = true
        dragEnabled = true
        dropMode = DropMode.ON
    }

	open fun dispose() {
		containerTree?.dispose()
	}

    /**
     * Updates the [TreeModel] by comparing data from [GraphView] and [ContainerDrawing].
     *
     */
    fun update(mainGraphView: GraphView, containerDrawing: ContainerDrawing, editable: Boolean) {
	    dragEnabled = editable

	    containerTree?.dispose()
	    containerTree = ContainerTree(portFactory, portViewFactory, styleProvider, mainGraphView, containerDrawing, eventBus)
	    model = containerTree?.model?.treeModel
    }

    private inner class ContainerTreeCellRenderer : DefaultTreeCellRenderer() {
        private val iconCache: MutableMap<String, Icon> = mutableMapOf()
	    private val folderIcon = UiUtil.themedIcon("/img/folder-20.png")
	    private val subGraphIcon = ContainerLibraryElementIcon()

	    override fun getTreeCellRendererComponent(tree: JTree?, value: Any?, selected: Boolean, expanded: Boolean, leaf: Boolean, row: Int, hasFocus: Boolean): java.awt.Component {
            val label = super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus) as JLabel

		    if (value is DefaultMutableTreeNode && value.userObject is AbstractContainerTreeItem) {
			    val userObject = value.userObject as AbstractContainerTreeItem
			    label.text = userObject.getDescription()
			    label.icon = when (userObject.type) {
				    ContainerTreeItemType.Port -> getIcon((value.userObject as DraggableTreeItem).iconPath)
				    ContainerTreeItemType.Control -> getIcon((value.userObject as DraggableTreeItem).iconPath)
				    ContainerTreeItemType.SubGraph -> subGraphIcon
				    else -> folderIcon
			    }
		    }

            return label
        }

        private fun getIcon(iconPath: String): Icon {
            try {
	            return iconCache.getOrPut(iconPath) { UiUtil.themedIcon(iconPath)}
            } catch (e: Exception) {
                LOG.value.error("Could not load icon $iconPath")
                throw e
            }
        }
    }
}
