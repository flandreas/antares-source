package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.app.Application
import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.swing.JTreeUtil
import ch.scorpion.jabbah.base.swing.JTreeUtil.findTreeNode
import ch.scorpion.jabbah.base.swing.JTreeUtil.getPath
import ch.scorpion.jabbah.base.swing.UiUtil
import ch.scorpion.jabbah.graph.project.Project
import ch.scorpion.jabbah.graph.ui.ContainerLibraryElementIcon
import ch.scorpion.jabbah.graph.ui.library.LibraryTreeView
import ch.scorpion.jabbah.graph.ui.library.LibraryTreeViewController
import java.awt.Component
import java.awt.font.TextAttribute
import javax.swing.*
import javax.swing.tree.*

class LibraryTreeViewSwing(
	private val controller: LibraryTreeViewController,
	application: Application,
	showWorkspaceNode: Boolean = true
) : JTree(LibraryTreeModelBuilderSwing(controller.library, controller.project).build()), LibraryTreeView {

	companion object {
		private val LOG by logger(LibraryTreeViewSwing::class)
	}

	val actions = LibraryTreeViewActionsSwing(controller, controller.type, application)

	init {
		controller.view = this

		selectionModel.selectionMode = TreeSelectionModel.SINGLE_TREE_SELECTION
		selectionModel.addTreeSelectionListener { setupPopupMenu(it.newLeadSelectionPath) }

		transferHandler = LibraryTreeViewTransferHandler(controller)

		setRowHeight(26)
		isRootVisible = showWorkspaceNode
		setCellRenderer(Renderer())
		addTreeSelectionListener { controller.selectedItem = getSelectedItem() }

		dragEnabled = controller.active
		dropMode = DropMode.ON_OR_INSERT
		showsRootHandles = true

		expandRow(0)
	}

	override fun dispose() { }

	/** ---- [LibraryTreeView] interface */

	override fun refresh() {
		invalidate()
		validate()
		dragEnabled = controller.active
	}

	override val folderOfSelectedItem: LibraryDirectory? get() =
		(selectionPath?.parentPath?.lastPathComponent as DefaultMutableTreeNode?)?.userObject as LibraryDirectory?

	override fun handle(event: LibraryItemAddedEvent) {
		findOptionalTreeNode(event.parent)?.let {
			val newNode = DefaultMutableTreeNode(event.item)
			it.add(newNode)
			(model as DefaultTreeModel).nodesWereInserted(it, intArrayOf(it.childCount - 1))
			expandPath(getPath(it))
			scrollPathToVisible(getPath(newNode))
		}
	}

	override fun handle(event: LibraryItemRemovedEvent) {
		val node = findTreeNode(event.item)
		val parent = node.parent
		val nodeIndex = parent.getIndex(node)
		node.removeFromParent()
		(model as DefaultTreeModel).nodesWereRemoved(parent, intArrayOf(nodeIndex), arrayOf(node))
	}

	override fun handle(event: LibraryItemUpdatedEvent) {
		findOptionalTreeNode(event.item)?.let {
			it.userObject = event.item
			(model as DefaultTreeModel).nodeChanged(it)
		}
	}

	override fun handle(event: LibraryItemMovedEvent) {
		findOptionalTreeNode(event.parent)?.let {
			findTreeNode(event.item).removeFromParent()
			it.insert(DefaultMutableTreeNode(event.item), event.index)
			(model as DefaultTreeModel).nodeStructureChanged(it)
		}
	}

	override fun handle(event: LibraryDirectoryRenamedEvent) {
		val node = findTreeNode(event.directory)
		(model as DefaultTreeModel).nodeChanged(node)
	}

	override fun expandTo(element: ContainerLibraryElement) {
		SwingUtilities.invokeLater {
			val node = findTreeNode(treeModel.root as TreeNode) { (it as DefaultMutableTreeNode).userObject == element }
			if (node != null) {
				selectionPath = getPath(node)
			}
		}
	}

	override fun expandAllFromSelection() {
		JTreeUtil.expandAll(this, selectionPath)
	}

	override fun collapseAtSelection() {
		JTreeUtil.collapseAll(this, selectionPath)
	}

	override fun openLibrary(library: Library) {
		LOG.debug("open Library '${library.name}'")
		val root = model.root as DefaultMutableTreeNode
		val oldLibraryNode = getLibraryNode()
		val newLibraryNode = DefaultMutableTreeNode(library)
		LibraryTreeModelBuilderSwing.addLibrary(newLibraryNode, library)
		val libraryNodeIndex = if (getProjectNode() == null) 0 else 1

		root.remove(libraryNodeIndex)
		(model as DefaultTreeModel).nodesWereRemoved(root, intArrayOf(libraryNodeIndex), arrayOf(oldLibraryNode))
		root.insert(newLibraryNode, libraryNodeIndex)
		(model as DefaultTreeModel).nodesWereInserted(root, intArrayOf(libraryNodeIndex))

		expandRow(0)
	}

	override fun openProject(project: Project) {
		LOG.debug("open Project '${project.name}'")
		val root = model.root as DefaultMutableTreeNode
		val oldProjectNode = getProjectNode()
		val newProjectNode = DefaultMutableTreeNode(project)
		LibraryTreeModelBuilderSwing.addLibrary(newProjectNode, project)

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

	override fun closeProject() {
		LOG.debug("close Project")
		val projectNode = getProjectNode()
		if (projectNode != null) {
			val root = model.root as DefaultMutableTreeNode
			root.remove(0)
			(model as DefaultTreeModel).nodesWereRemoved(root, intArrayOf(0), arrayOf(projectNode))
		}
	}

	/** ---- [LibraryTreeViewSwing] */

	private fun getSelectedItem(): LibraryItem? {
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
		componentPopupMenu = actions.getPopupMenu(newSelectionPath.lastPathComponent as DefaultMutableTreeNode)
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
		return findTreeNode(model.root as TreeNode) {
			(it as DefaultMutableTreeNode).userObject == item
		} as DefaultMutableTreeNode?
	}

	private inner class Renderer : DefaultTreeCellRenderer() {

		private val iconCache: MutableMap<String, Icon> = mutableMapOf()
		private val containerLibElemIcon = ContainerLibraryElementIcon()
		private val currentContainerLibElemIcon = ContainerLibraryElementIcon(current = true)
		private val defaultElemFont = this@LibraryTreeViewSwing.font.deriveFont(mapOf(TextAttribute.UNDERLINE to TextAttribute.UNDERLINE_ON))
		private val projectIcon = UiUtil.themedIcon("/img/project-24.png")
		private val libraryIcon = UiUtil.themedIcon("/img/library-24.png")
		private val folderIcon = UiUtil.themedIcon("/img/folder-20.png")
		private val desktopIcon = UiUtil.themedIcon("/img/table-20.png")

		override fun getTreeCellRendererComponent(tree: JTree?, value: Any?, sel: Boolean, expanded: Boolean, leaf: Boolean, row: Int, hasFocus: Boolean): Component {
			val component = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus) as JLabel
			if ((value as DefaultMutableTreeNode).userObject is LibraryItem) {
				val iconPath = (value.userObject as LibraryItem).iconPath
				component.font = this@LibraryTreeViewSwing.font
				if (StringUtils.isNotEmpty(iconPath)) {
					component.icon = getIcon(iconPath!!)
				} else if (value.userObject is ContainerLibraryElement) {
					component.icon = containerLibElemIcon
					if (controller.isCurrentElement(value.userObject as ContainerLibraryElement)) {
						component.icon = currentContainerLibElemIcon
					}
					if (controller.isDefaultElement(value.userObject as ContainerLibraryElement)) {
						component.font = defaultElemFont
					}
				} else if (value.userObject is Library) {
					if (value.userObject == controller.library) {
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
			return iconCache.getOrPut(iconPath) { UiUtil.themedIcon(iconPath) }
		}
	}
}
