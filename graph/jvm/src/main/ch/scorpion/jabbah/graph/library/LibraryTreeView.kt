package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.app.CurrentSavableEvent
import ch.scorpion.jabbah.app.Savable
import ch.scorpion.jabbah.base.ActionWrapperSwing
import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.swing.JTreeUtil
import ch.scorpion.jabbah.base.swing.JTreeUtil.findTreeNode
import ch.scorpion.jabbah.base.swing.JTreeUtil.getPath
import ch.scorpion.jabbah.graph.ApplicationModeEvent
import ch.scorpion.jabbah.graph.project.CloseProjectAction
import ch.scorpion.jabbah.graph.project.Project
import ch.scorpion.jabbah.graph.project.ProjectPropertiesAction
import ch.scorpion.jabbah.graph.ui.ContainerLibraryElementIcon
import java.awt.Component
import java.awt.font.TextAttribute
import javax.swing.*
import javax.swing.tree.*


/** Posted on a [LibraryTreeView]'s [EventBus] if the current selection in that [LibraryTreeView] has changed.*/
data class LibrarySelectionChangedEvent(val libraryTreeView: LibraryTreeView)

/**
 * Displays the current [Project] and the current [Library] as a tree.
 *
 * Instances of this class post the following events on the [EventBus]:
 * - A [LibrarySelectionChangedEvent] when the user selects a tree item
 */
class LibraryTreeView(
	library: Library,
	project: Project? = null,
	private val eventBus: EventBus = BaseModule.eventBus,
	showWorkspaceNode: Boolean = true
) : JTree(LibraryTreeModelBuilder(library, project).build()) {

	companion object {
		private val LOG by logger(LibraryTreeView::class)
	}

	/** Holds the [Library] to display.*/
	var library: Library = library
		set(value) {
			if (field !== value) {
				field = value
				openLibrary(library)
			}
		}

	/** Holds the [Project] to display.*/
	var project: Project? = project
		set(value) {
			if (field !== value) {
				field = value
				if (project == null) {
					closeProject()
				} else {
					openProject(project!!)
				}
			}
		}

	private val desktopPopupMenu = JPopupMenu()

	private val directoryPopupMenu = JPopupMenu()

	private val containerPopupMenu = JPopupMenu()

	private val projectRootMenu = JPopupMenu()

	private val libraryRootMenu = JPopupMenu()

	private val basePopupMenu = JPopupMenu()

	private var currentSavable: Savable? = null
		set(value) {
			if (field != value) {
				field = value
				invalidate()
				repaint()
			}
		}

	private val libraryItemAddedHandler: EventHandler<LibraryItemAddedEvent> = { handle(it) }

	private val libraryItemRemovedHandler: EventHandler<LibraryItemRemovedEvent> = { handle(it) }

	private val libraryItemUpdatedHandler: EventHandler<LibraryItemUpdatedEvent> = { handle(it) }

	private val libraryItemMovedHandler: EventHandler<LibraryItemMovedEvent> = { handle(it) }

	private val libraryItemDirectoryRenamedHandler: EventHandler<LibraryDirectoryRenamedEvent> = { handle(it) }

	private val applicationModeHandler: EventHandler<ApplicationModeEvent> = { dragEnabled = it.applicationMode.isEdit() }

	private val currentSavableHandler: EventHandler<CurrentSavableEvent> = { handle(it) }

	private val openContainerLibraryElementRequestHandler: EventHandler<OpenContainerLibraryElementRequest> = { handle(it) }

	init {
		selectionModel.selectionMode = TreeSelectionModel.SINGLE_TREE_SELECTION
		selectionModel.addTreeSelectionListener { setupPopupMenu(it.newLeadSelectionPath) }

		transferHandler = LibraryTreeViewTransferHandler(this)

		setRowHeight(24)
		isRootVisible = showWorkspaceNode
		setCellRenderer(Renderer())
		addTreeSelectionListener { eventBus.post(LibrarySelectionChangedEvent(this)) }

		dragEnabled = true
		dropMode = DropMode.ON_OR_INSERT
		showsRootHandles = true

		eventBus.register(LibraryItemAddedEvent::class, libraryItemAddedHandler)
		eventBus.register(LibraryItemRemovedEvent::class, libraryItemRemovedHandler)
		eventBus.register(LibraryItemUpdatedEvent::class, libraryItemUpdatedHandler)
		eventBus.register(LibraryItemMovedEvent::class, libraryItemMovedHandler)
		eventBus.register(LibraryDirectoryRenamedEvent::class, libraryItemDirectoryRenamedHandler)

		eventBus.register(ApplicationModeEvent::class, applicationModeHandler)
		eventBus.register(CurrentSavableEvent::class, currentSavableHandler)
		eventBus.register(OpenContainerLibraryElementRequest::class, openContainerLibraryElementRequestHandler)

		val expandAllAction = ExpandAllAction()
		val collapseAllAction = CollapseAllAction()
		val addLibraryFolderAction = AddLibraryFolderAction()
		val newGraphAction = NewGraphAction()
		val deleteLibraryFolderAction = DeleteLibraryFolderAction()
		val deleteLibraryElementAction = DeleteLibraryElementAction()
		val editLibraryAction = EditLibraryAction()
		val libraryFolderPropertiesAction = LibraryFolderPropertiesAction()

		desktopPopupMenu.add(ActionWrapperSwing(expandAllAction))
		desktopPopupMenu.add(ActionWrapperSwing(collapseAllAction))

		projectRootMenu.add(ActionWrapperSwing(expandAllAction))
		projectRootMenu.add(ActionWrapperSwing(collapseAllAction))
		projectRootMenu.addSeparator()
		projectRootMenu.add(ActionWrapperSwing(newGraphAction))
		projectRootMenu.add(ActionWrapperSwing(addLibraryFolderAction))
		projectRootMenu.add(ActionWrapperSwing(CloseProjectAction()))
		projectRootMenu.addSeparator()
		projectRootMenu.add(ActionWrapperSwing(ProjectPropertiesAction()))

		directoryPopupMenu.add(ActionWrapperSwing(expandAllAction))
		directoryPopupMenu.add(ActionWrapperSwing(collapseAllAction))
		directoryPopupMenu.addSeparator()
		directoryPopupMenu.add(ActionWrapperSwing(newGraphAction))
		directoryPopupMenu.add(ActionWrapperSwing(addLibraryFolderAction))
		directoryPopupMenu.add(ActionWrapperSwing(deleteLibraryFolderAction))
		directoryPopupMenu.add(ActionWrapperSwing(libraryFolderPropertiesAction))

		libraryRootMenu.add(ActionWrapperSwing(expandAllAction))
		libraryRootMenu.add(ActionWrapperSwing(collapseAllAction))
		libraryRootMenu.addSeparator()
		libraryRootMenu.add(ActionWrapperSwing(addLibraryFolderAction))
		libraryRootMenu.add(ActionWrapperSwing(newGraphAction))
		libraryRootMenu.add(ActionWrapperSwing(deleteLibraryFolderAction))
		libraryRootMenu.addSeparator()
		libraryRootMenu.add(ActionWrapperSwing(editLibraryAction))
		libraryRootMenu.add(ActionWrapperSwing(LibraryPropertiesAction()))

		containerPopupMenu.add(ActionWrapperSwing(deleteLibraryElementAction))
		containerPopupMenu.add(JCheckBoxMenuItem(ActionWrapperSwing(DefaultContainerLibraryElementAction())))
		containerPopupMenu.add(ActionWrapperSwing(DuplicateGraphAction()))

		basePopupMenu.add(ActionWrapperSwing(deleteLibraryElementAction))

		expandRow(0)
	}

	fun dispose() {
		eventBus.unregister(libraryItemAddedHandler)
		eventBus.unregister(libraryItemRemovedHandler)
		eventBus.unregister(libraryItemUpdatedHandler)
		eventBus.unregister(libraryItemMovedHandler)
		eventBus.unregister(libraryItemDirectoryRenamedHandler)
		eventBus.unregister(applicationModeHandler)
		eventBus.unregister(currentSavableHandler)
		eventBus.unregister(openContainerLibraryElementRequestHandler)
	}

	/** ---- [LibraryTreeView] */

	fun getSelectedItem(): LibraryItem? {
		val path = selectionPath ?: return null
		if ((path.lastPathComponent as DefaultMutableTreeNode).userObject is LibraryItem?) {
			return (path.lastPathComponent as DefaultMutableTreeNode).userObject as LibraryItem?
		}
		return null
	}

	/** Setup the popup menu according to the currently selected [TreeNode]' user object.*/
	private fun setupPopupMenu(newSelectionPath: TreePath?) {
		if (newSelectionPath == null) {
			componentPopupMenu = null
			return
		}
		componentPopupMenu = when ((newSelectionPath.lastPathComponent as DefaultMutableTreeNode).userObject) {
			is Project -> projectRootMenu
			is Library -> libraryRootMenu
			is LibraryFolder -> directoryPopupMenu
			is LibraryDirectory -> directoryPopupMenu
			is ContainerLibraryElement -> containerPopupMenu
			is BaseLibraryElement -> basePopupMenu
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

	private fun displaysLibrary(library: Library?): Boolean = library === this.library || library === project

	/**
	 * Expand the [JTree] to the node that contains the opened [ContainerLibraryElement].
	 * This is primarily needed when the request originates from opening a [Project].
	 */
	private fun handle(event: OpenContainerLibraryElementRequest) {
		if (displaysLibrary(event.element.library)) {
			SwingUtilities.invokeLater {
				val node = findTreeNode(treeModel.root as TreeNode) { (it as DefaultMutableTreeNode).userObject == event.element }
				if (node != null) {
					selectionPath = JTreeUtil.getPath(node)
				}
			}
		}
	}

	private fun handle(event: LibraryItemAddedEvent) {
		if (displaysLibrary(event.item.library)) {
			findOptionalTreeNode(event.parent)?.let {
				it.add(DefaultMutableTreeNode(event.item))
				(model as DefaultTreeModel).nodesWereInserted(it, intArrayOf(it.childCount - 1))
				expandPath(getPath(it))
			}
		}
	}

	/**
	 * Updates the user object of the [TreeNode] that contains the updated [LibraryItem] with the new one.
	 * This is necessary to reflect the possibly changed [LibraryItem] name in the [TreeNode].
	 */
	private fun handle(event: LibraryItemUpdatedEvent) {
		if (displaysLibrary(event.item.library)) {
			val node = findTreeNode(event.item)
			node.userObject = event.item
			(model as DefaultTreeModel).nodeChanged(node)
		}
	}

	private fun handle(event: LibraryItemRemovedEvent) {
		if (displaysLibrary(event.parent.library)) {
			val node = findTreeNode(event.item)
			val parent = node.parent
			val nodeIndex = parent.getIndex(node)
			node.removeFromParent()
			(model as DefaultTreeModel).nodesWereRemoved(parent, intArrayOf(nodeIndex), arrayOf(node))
		}
	}

	private fun handle(event: LibraryItemMovedEvent) {
		if (displaysLibrary(event.item.library)) {
			findOptionalTreeNode(event.parent)?.let {
				findTreeNode(event.item).removeFromParent()
				it.insert(DefaultMutableTreeNode(event.item), event.index)
				(model as DefaultTreeModel).nodeStructureChanged(it)
			}
		}
	}

	private fun handle(event: LibraryDirectoryRenamedEvent) {
		if (displaysLibrary(event.directory.library)) {
			val node = findTreeNode(event.directory)
			(model as DefaultTreeModel).nodeChanged(node)
		}
	}

	private fun openLibrary(library: Library) {
		LOG.debug("open Library '${library.name}'")
		val root = model.root as DefaultMutableTreeNode
		val oldLibraryNode = getLibraryNode()
		val newLibraryNode = DefaultMutableTreeNode(library)
		LibraryTreeModelBuilder.addItems(newLibraryNode, library)
		val libraryNodeIndex = if (getProjectNode() == null) 0 else 1

		root.remove(libraryNodeIndex)
		(model as DefaultTreeModel).nodesWereRemoved(root, intArrayOf(libraryNodeIndex), arrayOf(oldLibraryNode))
		root.insert(newLibraryNode, libraryNodeIndex)
		(model as DefaultTreeModel).nodesWereInserted(root, intArrayOf(libraryNodeIndex))

		expandRow(0)
	}

	private fun openProject(project: Project) {
		LOG.debug("open Project '${project.name}'")
		val root = model.root as DefaultMutableTreeNode
		val oldProjectNode = getProjectNode()
		val newProjectNode = DefaultMutableTreeNode(project)
		LibraryTreeModelBuilder.addItems(newProjectNode, project)

		if (oldProjectNode == null) {
			root.insert(newProjectNode, 0)
			(model as DefaultTreeModel).nodesWereInserted(root, intArrayOf(0))
		} else {
			root.remove(0)
			(model as DefaultTreeModel).nodesWereRemoved(root, intArrayOf(0), arrayOf(oldProjectNode))
			root.insert(newProjectNode, 0)
			(model as DefaultTreeModel).nodesWereInserted(root, intArrayOf(0))
		}

		expandRow(0)
	}

	private fun closeProject() {
		LOG.debug("close Project")
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
			(it as DefaultMutableTreeNode).userObject == item
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
					if (value.userObject == library) {
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
