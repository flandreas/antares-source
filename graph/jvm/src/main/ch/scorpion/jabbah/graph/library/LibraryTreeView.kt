package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.app.CurrentSavableEvent
import ch.scorpion.jabbah.app.Savable
import ch.scorpion.jabbah.base.ActionWrapperSwing
import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.swing.JTreeUtil
import ch.scorpion.jabbah.base.swing.JTreeUtil.findTreeNode
import ch.scorpion.jabbah.graph.ApplicationMode
import ch.scorpion.jabbah.graph.ApplicationModeEvent
import ch.scorpion.jabbah.graph.project.*
import ch.scorpion.jabbah.graph.ui.ContainerLibraryElementIcon
import java.awt.Component
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*
import javax.swing.tree.*

/**
 * Displays the {@link Library} as a tree.
 *
 * Instances of this class post the following events on the {@link T2Bus}:
 * - A [LibrarySelectionChangedEvent] when the user selects a tree item
 * - A [OpenContainerLibraryElementRequest] when the user double clicks on a [ContainerLibraryElement]
 */
class LibraryTreeView(
    private val eventBus: EventBus = BaseModule.eventBus,
    val libraryHolder: LibraryHolder = LibraryModule.libraryHolder,
    val projectHolder: ProjectHolder = ProjectModule.projectHolder
) : JTree(createLibraryTreeModel(libraryHolder.library, projectHolder.project)) {

    private val directoryPopupMenu = JPopupMenu()

    private val containerPopupMenu = JPopupMenu()

	private var currentSavable: Savable? = null
		set(value) {
			if (field != value) {
				field = value
				invalidate()
				repaint()
			}
		}

    init {
        selectionModel.selectionMode = TreeSelectionModel.SINGLE_TREE_SELECTION
        selectionModel.addTreeSelectionListener { setupPopupMenu(it.newLeadSelectionPath) }

        transferHandler = LibraryElementTransferHandler()

        setRowHeight(24)
	    isRootVisible = false
        setCellRenderer(Renderer())
        addTreeSelectionListener({ eventBus.post(LibrarySelectionChangedEvent(this)) })
        addMouseListener(DoubleClickListener())

        dragEnabled = true
        dropMode = DropMode.ON

        // TODO Update TreeModel selectively
        eventBus.register(LibraryItemAddedEvent::class, { reloadLibrary() })
        eventBus.register(LibraryItemRemovedEvent::class, { reloadLibrary() })
        eventBus.register(LibraryItemUpdatedEvent::class, { handle(it) })
	    eventBus.register(ProjectEvent::class, { reloadLibrary() })

        eventBus.register(ApplicationModeEvent::class, { dragEnabled = it.applicationMode === ApplicationMode.EDIT })
	    eventBus.register(CurrentSavableEvent::class, { handle(it) })
	    eventBus.register(OpenContainerLibraryElementRequest::class, { handle(it) })

        directoryPopupMenu.add(ActionWrapperSwing(AddLibraryFolderAction()))
        directoryPopupMenu.add(ActionWrapperSwing(NewGraphAction()))
        directoryPopupMenu.add(ActionWrapperSwing(AddGraphToLibraryAction()))

        containerPopupMenu.add(ActionWrapperSwing(DeleteContainerLibraryElementAction()))

	    expandRow(0)
    }

    companion object {

        fun createLibraryTreeModel(library: Library, project: Project?): TreeModel {
	        val root = DefaultMutableTreeNode()

	        if (project != null) {
		        val projectNode = DefaultMutableTreeNode(project)
		        addItems(projectNode, project)
		        root.add(projectNode)
	        }

	        val libraryNode = DefaultMutableTreeNode(library)
            addItems(libraryNode, library)
	        root.add(libraryNode)

            return DefaultTreeModel(root)
        }

        private fun addItems(parent: DefaultMutableTreeNode, directory: LibraryDirectory) {
            // TODO Sort
            for (item in directory.getItems()) {
                val node = DefaultMutableTreeNode(item)
                parent.add(node)
                if (item is LibraryDirectory) {
                    addItems(node, item)
                }
            }
        }
    }


    /** ---- [LibraryTreeView] */

    fun getSelectedItem(): LibraryItem? {
        val path = selectionPath ?: return null
        return (path.lastPathComponent as DefaultMutableTreeNode).userObject as LibraryItem
    }

    private fun reloadLibrary() {
        SwingUtilities.invokeLater {
            model = createLibraryTreeModel(libraryHolder.library, projectHolder.project)
        }
    }

    private inner class DoubleClickListener : MouseAdapter() {
        override fun mousePressed(e: MouseEvent) {
            if (e.clickCount == 2 && getSelectedItem() is ContainerLibraryElement) {
                eventBus.post(OpenContainerLibraryElementRequest(getSelectedItem() as ContainerLibraryElement))
            }
        }
    }

    /** Setup the popup menu according to the currently selected [TreeNode]' user object.*/
    private fun setupPopupMenu(newSelectionPath: TreePath?) {
        if (newSelectionPath == null) {
            componentPopupMenu = null
            return
        }
        componentPopupMenu = when ((newSelectionPath.lastPathComponent as DefaultMutableTreeNode).userObject) {
            is LibraryFolder -> directoryPopupMenu
            is LibraryDirectory -> directoryPopupMenu
            else -> containerPopupMenu
        }
    }

	private fun handle(event: CurrentSavableEvent) {
		if (event.savable is LibrarySavable || event.savable is ProjectSavable) {
			currentSavable = event.savable
		} else {
			currentSavable = null
		}
	}

	/**
	 * Expand the [JTree] to the node that contains the opened [ContainerLibraryElement].
	 * This is primarily needed when the request originates from opening a [Project].
	 */
	private fun handle(event: OpenContainerLibraryElementRequest) {
		SwingUtilities.invokeLater {
			val node = findTreeNode(treeModel.root as TreeNode, { (it as DefaultMutableTreeNode).userObject == event.element })
			if (node != null) {
				selectionPath = JTreeUtil.getPath(node)
			}
		}
	}

	/**
	 * Updates the user object of the [TreeNode] that contains the updated [LibraryItem] with the new one.
	 * This is necessary to reflect the possibly changed [LibraryItem] name in the [TreeNode].
	 */
	private fun handle(event: LibraryItemUpdatedEvent) {
		val node = JTreeUtil.findTreeNode(model.root as TreeNode, { (it as DefaultMutableTreeNode).userObject == event.item }) as MutableTreeNode
		node.setUserObject(event.item)
		(model as DefaultTreeModel).nodeChanged(node)
	}

    private inner class Renderer : DefaultTreeCellRenderer() {

        private val iconCache: MutableMap<String, Icon> = mutableMapOf()
        private val containerLibElemIcon = ContainerLibraryElementIcon()
	    private val currentContainerLibElemIcon = ContainerLibraryElementIcon(current = true)

        override fun getTreeCellRendererComponent(tree: JTree?, value: Any?, sel: Boolean, expanded: Boolean, leaf: Boolean, row: Int, hasFocus: Boolean): Component {
            val component = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus) as JLabel
	        if ((value as DefaultMutableTreeNode).userObject != null) {
		        val iconPath = (value.userObject as LibraryItem).iconPath
		        component.font = this@LibraryTreeView.font
		        if (StringUtils.isNotEmpty(iconPath)) {
			        component.icon = getIcon(iconPath!!)
		        } else if (value.userObject is ContainerLibraryElement) {
			        component.icon = containerLibElemIcon
			        if (
				        currentSavable is LibrarySavable && (currentSavable as LibrarySavable).element == value.userObject ||
				        currentSavable is ProjectSavable && (currentSavable as ProjectSavable).element == value.userObject
			        ) {
				        component.icon = currentContainerLibElemIcon
			        }
		        }
	        }
            return component
        }

        private fun getIcon(iconPath: String): Icon {
            return iconCache.getOrPut(iconPath, { ImageIcon(LibraryTreeView::class.java.getResource(iconPath)) })
        }
    }
}

/**
 * Posted on a [LibraryTreeView]'s [EventBus] if the current selection in this [LibraryTreeView] has changed.
 */
data class LibrarySelectionChangedEvent(val libraryTreeView: LibraryTreeView)
