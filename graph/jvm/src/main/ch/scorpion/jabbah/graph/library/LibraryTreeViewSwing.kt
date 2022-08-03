package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.app.Application
import ch.scorpion.jabbah.base.PROP_BEGINNER_HELP_TOOLTIP
import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.swing.JTreeUtil
import ch.scorpion.jabbah.base.swing.JTreeUtil.findTreeNode
import ch.scorpion.jabbah.base.swing.JTreeUtil.getPath
import ch.scorpion.jabbah.base.swing.UiUtil
import ch.scorpion.jabbah.graph.module.GraphModuleJvm
import ch.scorpion.jabbah.graph.project.Project
import ch.scorpion.jabbah.graph.ui.ContainerLibraryElementIcon
import ch.scorpion.jabbah.graph.ui.library.LibraryTreeView
import ch.scorpion.jabbah.graph.ui.library.LibraryTreeViewController
import java.awt.Component
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.font.TextAttribute
import javax.swing.*
import javax.swing.tree.*
import kotlin.math.min

class LibraryTreeViewSwing(
	private val controller: LibraryTreeViewController,
	application: Application,
	showWorkspaceNode: Boolean = true
) : JTree(LibraryTreeModelBuilderSwing(controller.library, controller.project).build()), LibraryTreeView {

	companion object {
		private val LOG by logger(LibraryTreeViewSwing::class)
	}

	private val showBeginnerTips = BaseModule.properties.getBoolean(PROP_BEGINNER_HELP_TOOLTIP)

	private val rightMouseListener = RightMouseListener()

	val actions = GraphModuleJvm.libraryTreeViewActionsProvider(
		LibraryTreeViewActionsParams(controller, controller.type, application))

	init {
		controller.view = this

		addMouseListener(rightMouseListener)

		selectionModel.selectionMode = TreeSelectionModel.SINGLE_TREE_SELECTION

		transferHandler = LibraryTreeViewTransferHandler(controller)

		setRowHeight(26)
		isRootVisible = showWorkspaceNode
		setCellRenderer(Renderer())
		addTreeSelectionListener { controller.selectedItem = getSelectedItem() }

		dragEnabled = controller.active
		dropMode = DropMode.ON_OR_INSERT
		showsRootHandles = true

		expandRow(0)

		ToolTipManager.sharedInstance().registerComponent(this)
	}

	override fun dispose() {
		ToolTipManager.sharedInstance().unregisterComponent(this)
	}

	/** ---- [LibraryTreeView] interface */

	override fun refresh() {
		invalidate()
		repaint()
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
		findOptionalTreeNode(event.oldDirectory)?.let { oldDirectoryNode ->
			val itemNode = findTreeNode(event.item)
			itemNode.removeFromParent()
			(model as DefaultTreeModel).nodeStructureChanged(oldDirectoryNode)

			findOptionalTreeNode(event.newDirectory)?.let {
				it.insert(itemNode, min(event.index, it.childCount))
				(model as DefaultTreeModel).nodeStructureChanged(it)
				selectionPath = getPath(itemNode)
			}
		}
	}

	override fun handle(event: LibraryDirectoryRenamedEvent) {
		val node = findTreeNode(event.directory)
		(model as DefaultTreeModel).nodeChanged(node)
	}

	override fun handle(event: ContainerLibraryElementRenamedEvent) {
		val node = findTreeNode(event.element)
		(model as DefaultTreeModel).nodeChanged(node)
	}

	override fun expandTo(element: ContainerLibraryElement) {
		SwingUtilities.invokeLater {
			val node = findTreeNode(treeModel.root as TreeNode) { (it as DefaultMutableTreeNode).userObject === element }
			if (node != null) {
				getPath(node).also {
					selectionPath = it
					scrollPathToVisible(it)
				}
			}
		}
	}

	override fun expandAllFromSelection() {
		selectionPath?.let {
			JTreeUtil.expandAll(this, it)
		}
	}

	override fun collapseAtSelection() {
		selectionPath?.let {
			JTreeUtil.collapseAll(this, it)
		}
	}

	override fun openLibrary(library: Library) {
		LOG.userTrail("Open Library '${library.name}'")
		val root = model.root as DefaultMutableTreeNode

		// TODO Handle stale imports, e.g. display warning dialog

		root.removeAllChildren()
		for (import in library.expandedImports.libraries) {
			val libraryNode = DefaultMutableTreeNode(import)
			LibraryTreeModelBuilderSwing.addLibrary(libraryNode, import)
			root.add(libraryNode)
		}

		(model as DefaultTreeModel).nodeStructureChanged(root)

		expandRow(0)
	}

	override fun openProject(project: Project) {
		LOG.userTrail("Open Project '${project.name}'")
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
		LOG.userTrail("Close Project")
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

	private inner class RightMouseListener : MouseAdapter() {
		override fun mousePressed(e: MouseEvent?) {
			if (e?.button == MouseEvent.BUTTON3) {
				getPathForLocation(e.x, e.y)?.let { path ->
					requestFocusInWindow()
					selectionPath = path
					val menu = actions.getPopupMenu(path.lastPathComponent as DefaultMutableTreeNode)
					menu?.show(this@LibraryTreeViewSwing, e.x, e.y)
				}
			}
		}
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
			component.toolTipText = null
			if ((value as DefaultMutableTreeNode).userObject is LibraryItem) {
				val iconPath = (value.userObject as LibraryItem).iconPath
				component.font = this@LibraryTreeViewSwing.font
				if (StringUtils.isNotEmpty(iconPath)) {
					if (controller.isCurrentItem(value.userObject as LibraryItem) &&(value.userObject as LibraryItem).activeIconPath != null) {
						component.icon = getIcon((value.userObject as LibraryItem).activeIconPath!!)
					} else {
						component.icon = getIcon(iconPath!!)
					}
					if (showBeginnerTips) {
						component.toolTipText = Translations.getString("library.action.baseElement.tip")
					}
				} else if (value.userObject is ContainerLibraryElement) {
					if (showBeginnerTips) {
						component.toolTipText = Translations.getString("library.action.libraryElement.tip")
					}
					component.icon = containerLibElemIcon
					if (controller.isCurrentItem(value.userObject as ContainerLibraryElement)) {
						component.icon = currentContainerLibElemIcon
					}
					if (controller.isDefaultElement(value.userObject as ContainerLibraryElement)) {
						component.font = defaultElemFont
					}
				} else if (value.userObject is Project) {
					component.icon = projectIcon
				} else if (value.userObject is Library) {
					component.icon = libraryIcon
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
