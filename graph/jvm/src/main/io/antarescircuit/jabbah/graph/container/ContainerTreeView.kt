package io.antarescircuit.jabbah.graph.container

import io.antarescircuit.jabbah.base.StringUtils
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.logger
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.base.swing.UiUtil
import io.antarescircuit.jabbah.base.swing.dynamictree.DynamicTreeModel
import io.antarescircuit.jabbah.base.ui.UIBasics.PROP_TREE_SHOW_ROOT_HANDLES
import io.antarescircuit.jabbah.draw.graphics.Graphics2DJvm
import io.antarescircuit.jabbah.draw.richtext.RichTextLabel
import io.antarescircuit.jabbah.draw.style.DrawStyleModule
import io.antarescircuit.jabbah.draw.style.StyleProvider
import io.antarescircuit.jabbah.edit.Component
import io.antarescircuit.jabbah.edit.Drawing
import io.antarescircuit.jabbah.edit.DrawingView
import io.antarescircuit.jabbah.graph.model.module.GraphModelModule
import io.antarescircuit.jabbah.graph.model.port.PortFactory
import io.antarescircuit.jabbah.graph.ui.MetaGraphIconProvider
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.module.GraphViewModule
import io.antarescircuit.jabbah.graph.view.port.PortViewFactory
import javax.swing.DropMode
import javax.swing.Icon
import javax.swing.JTree
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.TreeModel

/**
 * Displays the objects that can be dragged into a [ContainerDrawing], such as [PortViewComponent]s and controls.
 */
open class ContainerTreeView(
	protected val mainDrawingView: DrawingView<Drawing<Component>>,
	private val portFactory: PortFactory = GraphModelModule.portFactory,
	private val portViewFactory: PortViewFactory = GraphViewModule.portViewFactory,
	private val styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	protected val eventBus: EventBus = BaseModule.eventBus
) : JTree() {

	companion object {
		private val LOG = logger(ContainerTreeView::class)
	}

	/** Fills and manages the [DynamicTreeModel] displayed by this [ContainerTreeView].*/
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
		setShowsRootHandles(BaseModule.properties.getBoolean(PROP_TREE_SHOW_ROOT_HANDLES))
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
					ContainerTreeItemType.Image -> getIcon((value.userObject as DraggableTreeItem).iconPath)
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
