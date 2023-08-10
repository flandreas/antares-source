package ch.scorpion.jabbah.graph.container

import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.swing.UiUtil
import ch.scorpion.jabbah.base.swing.dynamictree.DynamicTreeModel
import ch.scorpion.jabbah.draw.graphics.Graphics2DJvm
import ch.scorpion.jabbah.draw.richtext.RichTextLabel
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.graph.model.module.GraphModelModule
import ch.scorpion.jabbah.graph.model.port.PortFactory
import ch.scorpion.jabbah.graph.ui.MetaGraphIconProvider
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.graph.view.port.PortViewFactory
import javax.swing.DropMode
import javax.swing.Icon
import javax.swing.JTree
import javax.swing.tree.DefaultMutableTreeNode
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
    var containerTree: ContainerTree? = null
		private set

	var isManualContainer: Boolean = false
		set(value) {
			field = value
			containerTree?.isManualContainer = value
		}

    init {
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
	    containerTree!!.isManualContainer = isManualContainer
	    model = containerTree?.model?.treeModel
    }

    private inner class ContainerTreeCellRenderer : RichTextLabel() {
        private val iconCache: MutableMap<String, Icon> = mutableMapOf()
	    private val folderIcon = UiUtil.themedIcon("/img/folder.png")
	    private val jabbahFont = Graphics2DJvm.fromAwtFont(this@ContainerTreeView.font)

	    override fun getTreeCellRendererComponent(tree: JTree?, value: Any?, selected: Boolean, expanded: Boolean, leaf: Boolean, row: Int, hasFocus: Boolean): java.awt.Component {
            val label = super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus) as RichTextLabel

		    if (value is DefaultMutableTreeNode && value.userObject is AbstractContainerTreeItem) {
			    val userObject = value.userObject as AbstractContainerTreeItem
			    label.richText = userObject.getRichText(jabbahFont)
			    label.icon = when (userObject.type) {
				    ContainerTreeItemType.Port -> getIcon((value.userObject as DraggableTreeItem).iconPath)
				    ContainerTreeItemType.Control -> getIcon((value.userObject as DraggableTreeItem).iconPath)
				    ContainerTreeItemType.SubGraph -> {
					    val graph = (value.userObject as SubGraphVerticeViewFolderItem).graphView.graph
					    graph?.let { MetaGraphIconProvider.provideIcon(it.type, false, StringUtils.isNotBlank(it.script)) }
						    ?:MetaGraphIconProvider.provideIcon(GraphModelModule.defaultGraphType, current = false, false)
				    }
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
