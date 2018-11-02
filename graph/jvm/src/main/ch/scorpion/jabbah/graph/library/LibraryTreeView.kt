package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.app.CurrentSavableEvent
import ch.scorpion.jabbah.app.Savable
import ch.scorpion.jabbah.base.ActionWrapperSwing
import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.Translations
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
import java.awt.font.TextAttribute
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
	projectHolder: ProjectHolder = ProjectModule.projectHolder
) : JTree(createLibraryTreeModel(libraryHolder.library, projectHolder.project)) {

	private val desktopPopupMenu = JPopupMenu()

    private val directoryPopupMenu = JPopupMenu()

    private val containerPopupMenu = JPopupMenu()

	private val projectRootMenu = JPopupMenu()

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

        transferHandler = LibraryTreeViewTransferHandler(this)

        setRowHeight(24)
	    isRootVisible = true
        setCellRenderer(Renderer())
        addTreeSelectionListener { eventBus.post(LibrarySelectionChangedEvent(this)) }
	    addMouseListener(DoubleClickListener())

        dragEnabled = true
        dropMode = DropMode.ON
	    showsRootHandles = true

        eventBus.register(LibraryItemAddedEvent::class) { handle(it) }
	    eventBus.register(LibraryItemRemovedEvent::class) { handle(it) }
	    eventBus.register(LibraryItemUpdatedEvent::class) { handle(it) }
	    eventBus.register(ProjectEvent::class) { handle(it) }
	    eventBus.register(LibraryEvent::class) { handle(it)}

	    eventBus.register(ApplicationModeEvent::class) { dragEnabled = it.applicationMode === ApplicationMode.EDIT }
	    eventBus.register(CurrentSavableEvent::class) { handle(it) }
	    eventBus.register(OpenContainerLibraryElementRequest::class) { handle(it) }

	    val expandAllAction = ExpandAllAction()
	    val collapseAllAction = CollapseAllAction()
	    val addLibraryFolderAction = AddLibraryFolderAction()
	    val newGraphAction = NewGraphAction()
	    val addGraphToLibraryAction = AddGraphToLibraryAction()

	    desktopPopupMenu.add(ActionWrapperSwing(expandAllAction))
	    desktopPopupMenu.add(ActionWrapperSwing(collapseAllAction))

	    projectRootMenu.add(ActionWrapperSwing(expandAllAction))
	    projectRootMenu.add(ActionWrapperSwing(collapseAllAction))
	    projectRootMenu.addSeparator()
	    projectRootMenu.add(ActionWrapperSwing(addLibraryFolderAction))
	    projectRootMenu.add(ActionWrapperSwing(newGraphAction))
	    projectRootMenu.add(ActionWrapperSwing(addGraphToLibraryAction))
	    projectRootMenu.add(ActionWrapperSwing(CloseProjectAction()))

	    directoryPopupMenu.add(ActionWrapperSwing(expandAllAction))
	    directoryPopupMenu.add(ActionWrapperSwing(collapseAllAction))
	    directoryPopupMenu.addSeparator()
	    directoryPopupMenu.add(ActionWrapperSwing(addLibraryFolderAction))
	    directoryPopupMenu.add(ActionWrapperSwing(newGraphAction))
	    directoryPopupMenu.add(ActionWrapperSwing(addGraphToLibraryAction))
	    directoryPopupMenu.add(ActionWrapperSwing(DeleteLibraryFolderAction()))

        containerPopupMenu.add(ActionWrapperSwing(DeleteContainerLibraryElementAction()))
	    containerPopupMenu.add(JCheckBoxMenuItem(ActionWrapperSwing(DefaultContainerLibraryElementAction())))
	    containerPopupMenu.add(ActionWrapperSwing(DuplicateGraphAction()))

	    expandRow(0)
    }

    companion object {

        fun createLibraryTreeModel(library: Library, project: Project?): TreeModel {
	        val root = DefaultMutableTreeNode(Translations.getString("graph.desktop.name"))

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
	    if ((path.lastPathComponent as DefaultMutableTreeNode).userObject is LibraryItem?) {
		    return (path.lastPathComponent as DefaultMutableTreeNode).userObject as LibraryItem?
	    }
        return null
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
	        is Project -> projectRootMenu
	        is Library -> directoryPopupMenu
            is LibraryFolder -> directoryPopupMenu
            is LibraryDirectory -> directoryPopupMenu
	        is ContainerLibraryElement -> containerPopupMenu
	        is String -> desktopPopupMenu
            else -> null
        }
    }

	private fun handle(event: CurrentSavableEvent) {
		currentSavable = if (event.savable is AbstractLibrarySavable) {
			event.savable
		} else {
			null
		}
	}

	/**
	 * Expand the [JTree] to the node that contains the opened [ContainerLibraryElement].
	 * This is primarily needed when the request originates from opening a [Project].
	 */
	private fun handle(event: OpenContainerLibraryElementRequest) {
		SwingUtilities.invokeLater {
			val node = findTreeNode(treeModel.root as TreeNode) { (it as DefaultMutableTreeNode).userObject == event.element }
			if (node != null) {
				selectionPath = JTreeUtil.getPath(node)
			}
		}
	}

	private fun handle(event: LibraryItemAddedEvent) {
		findOptionalTreeNode(event.parent)?.let {
			it.add(DefaultMutableTreeNode(event.item))
			(model as DefaultTreeModel).nodesWereInserted(it, intArrayOf(it.childCount - 1))
		}
	}

	/**
	 * Updates the user object of the [TreeNode] that contains the updated [LibraryItem] with the new one.
	 * This is necessary to reflect the possibly changed [LibraryItem] name in the [TreeNode].
	 */
	private fun handle(event: LibraryItemUpdatedEvent) {
		val node = findTreeNode(event.item)
		node.userObject = event.item
		(model as DefaultTreeModel).nodeChanged(node)
	}

	private fun handle(event: LibraryItemRemovedEvent) {
		val node = findTreeNode(event.item)
		val parent = node.parent
		val nodeIndex = parent.getIndex(node)
		node.removeFromParent()
		(model as DefaultTreeModel).nodesWereRemoved(parent, intArrayOf(nodeIndex), arrayOf(node))
	}

	private fun handle(event: ProjectEvent) {
		if (event.project != null) {
			openProject(event.project)
		} else {
			closeProject()
		}
	}

	private fun handle(event: LibraryEvent) {
		openLibrary(event.library)
	}

	private fun openLibrary(library: Library) {
		val root = model.root as DefaultMutableTreeNode
		val oldLibraryNode = getLibraryNode()
		val newLibraryNode = DefaultMutableTreeNode(library)
		addItems(newLibraryNode, library)
		val libraryNodeIndex = if (getProjectNode() == null) 0 else 1

		root.remove(libraryNodeIndex)
		(model as DefaultTreeModel).nodesWereRemoved(root, intArrayOf(libraryNodeIndex), arrayOf(oldLibraryNode))
		root.insert(newLibraryNode, libraryNodeIndex)
		(model as DefaultTreeModel).nodesWereInserted(root, intArrayOf(libraryNodeIndex))
	}

	private fun openProject(project: Project) {
		val root = model.root as DefaultMutableTreeNode
		val oldProjectNode = getProjectNode()
		val newProjectNode = DefaultMutableTreeNode(project)
		addItems(newProjectNode, project)

		if (oldProjectNode == null) {
			root.insert(newProjectNode, 0)
			(model as DefaultTreeModel).nodesWereInserted(root, intArrayOf(0))
		} else {
			root.remove(0)
			(model as DefaultTreeModel).nodesWereRemoved(root, intArrayOf(0), arrayOf(oldProjectNode))
			root.insert(newProjectNode, 0)
			(model as DefaultTreeModel).nodesWereInserted(root, intArrayOf(0))
		}
	}

	private fun closeProject() {
		val projectNode = getProjectNode()
		if (projectNode != null) {
			val root = model.root as DefaultMutableTreeNode
			root.remove(0)
			(model as DefaultTreeModel).nodesWereRemoved(root, intArrayOf(0), arrayOf(projectNode))
		}
	}

	private fun getProjectNode(): DefaultMutableTreeNode? {
		val root = model.root as DefaultMutableTreeNode
		if (root.childCount > 1) {
			return root.getChildAt(0) as DefaultMutableTreeNode
		}
		return null
	}

	private fun getLibraryNode(): DefaultMutableTreeNode {
		val root = model.root as DefaultMutableTreeNode
		if (root.childCount > 1) {
			return root.getChildAt(1) as DefaultMutableTreeNode
		}
		return root.getChildAt(0) as DefaultMutableTreeNode
	}

	/** Finds the [TreeNode] that contains the specified [LibraryItem] as user object.*/
	private fun findTreeNode(item: LibraryItem): DefaultMutableTreeNode {
		return findOptionalTreeNode(item)!!
	}

	private fun findOptionalTreeNode(item: LibraryItem): DefaultMutableTreeNode? {
		return JTreeUtil.findTreeNode(model.root as TreeNode) {
			val userObject = (it as DefaultMutableTreeNode).userObject
			if (userObject is Library) {
				userObject.libraryFolder == item || userObject == item
			} else {
				userObject == item
			}
		} as DefaultMutableTreeNode?
	}

	private inner class Renderer : DefaultTreeCellRenderer() {

        private val iconCache: MutableMap<String, Icon> = mutableMapOf()
        private val containerLibElemIcon = ContainerLibraryElementIcon()
	    private val currentContainerLibElemIcon = ContainerLibraryElementIcon(current = true)
	    private val defaultElemFont = this@LibraryTreeView.font.deriveFont(mapOf(TextAttribute.UNDERLINE to TextAttribute.UNDERLINE_ON))
		private val projectIcon = ImageIcon(LibraryTreeView::class.java.getResource("/img/project-24.png"))
		private val libraryIcon = ImageIcon(LibraryTreeView::class.java.getResource("/img/library-24.png"))
		private val folderIcon = ImageIcon(LibraryTreeView::class.java.getResource("/img/folder-20.png"))
		private val desktopIcon = ImageIcon(LibraryTreeView::class.java.getResource("/img/table-20.png"))

        override fun getTreeCellRendererComponent(tree: JTree?, value: Any?, sel: Boolean, expanded: Boolean, leaf: Boolean, row: Int, hasFocus: Boolean): Component {
            val component = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus) as JLabel
	        if ((value as DefaultMutableTreeNode).userObject is LibraryItem) {
		        val iconPath = (value.userObject as LibraryItem).iconPath
		        component.font = this@LibraryTreeView.font
		        if (StringUtils.isNotEmpty(iconPath)) {
			        component.icon = getIcon(iconPath!!)
		        } else if (value.userObject is ContainerLibraryElement) {
			        component.icon = containerLibElemIcon
			        if (isCurrentElement(value.userObject as ContainerLibraryElement)) {
				        component.icon = currentContainerLibElemIcon
			        }
			        if (isDefaultElement(value.userObject as ContainerLibraryElement)) {
				        component.font = defaultElemFont
			        }
		        } else if (value.userObject is Library) {
			        if (value.userObject == libraryHolder.library) {
				        component.icon = libraryIcon
			        } else {
				        component.icon = projectIcon
			        }
		        } else if (value.userObject is LibraryFolder) {
			        component.icon = folderIcon
		        }
	        } else {
		        component.icon = desktopIcon
	        }
            return component
        }

        private fun getIcon(iconPath: String): Icon {
            return iconCache.getOrPut(iconPath) { ImageIcon(LibraryTreeView::class.java.getResource(iconPath)) }
        }

	    private fun isCurrentElement(element: ContainerLibraryElement): Boolean {
		    return currentSavable is AbstractLibrarySavable && (currentSavable as AbstractLibrarySavable).element == element
	    }

	    private fun isDefaultElement(element: ContainerLibraryElement): Boolean {
		    return element.library?.defaultElementUUID == element.uuid
	    }
    }
}

/**
 * Posted on a [LibraryTreeView]'s [EventBus] if the current selection in this [LibraryTreeView] has changed.
 */
data class LibrarySelectionChangedEvent(val libraryTreeView: LibraryTreeView)
