package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.app.Application
import ch.scorpion.jabbah.base.invocation.InvocationHandler
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.swing.JTreeUtil.getPath
import ch.scorpion.jabbah.draw.graphics.Graphics2DJvm
import ch.scorpion.jabbah.edit.model.text.NamableTreeNode
import ch.scorpion.jabbah.graph.module.GraphModuleJvm
import ch.scorpion.jabbah.graph.ui.library.LibraryTreeView
import ch.scorpion.jabbah.graph.ui.library.LibraryTreeViewController
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.DropMode
import javax.swing.SwingUtilities
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import kotlin.math.min

class LibraryTreeViewSwing(
	controller: LibraryTreeViewController,
	application: Application,
	showWorkspaceNode: Boolean = true
) : BasicLibraryTreeViewSwing<LibraryTreeView>(controller, showWorkspaceNode), LibraryTreeView {

	companion object {
		private val LOG by logger(LibraryTreeViewSwing::class)
	}

	private val controller: LibraryTreeViewController get() = basicController as LibraryTreeViewController

	val actions = GraphModuleJvm.libraryTreeViewActionsProvider(
		LibraryTreeViewActionsParams(controller, controller.type, application))

	init {
		addMouseListener(MouseListener())
		addKeyListener(EnterKeyListener())

		transferHandler = LibraryTreeViewTransferHandler(controller)

		dragEnabled = controller.active
		dropMode = DropMode.ON_OR_INSERT
	}

	override fun dispose() {
		super.dispose()
		actions.dispose()
	}

	/** ---- [LibraryTreeView] interface */

	override val folderOfSelectedItem: LibraryDirectory? get() =
		(selectionPath?.parentPath?.lastPathComponent as DefaultMutableTreeNode?)?.userObject as LibraryDirectory?

	override fun refresh() {
		super.refresh()
		dragEnabled = controller.active
	}

	override fun handle(event: LibraryItemAddedEvent) {
		findOptionalTreeNode(event.parent)?.let {
			val newNode = NamableTreeNode(event.item, Graphics2DJvm.fromAwtFont(font))
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
			if (it is NamableTreeNode) {
				it.richTextName.reset()
			}
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

	/** ---- [LibraryTreeViewSwing] */

	private fun openSelectedItem() {
		if (controller.selectedItem is LibraryItem) {
			if (!controller.isCurrentItem(controller.selectedItem as LibraryItem)) {
				InvocationHandler.invoke {
					(controller.selectedItem as LibraryItem).open(controller.eventBus)
				}
			}
		}
	}

	private inner class MouseListener : MouseAdapter() {
		override fun mousePressed(e: MouseEvent?) {
			when (e?.button) {
				MouseEvent.BUTTON1 -> {
					if (e.clickCount == 2) {
						openSelectedItem()
					}
				}
				MouseEvent.BUTTON3 -> {
					showPopupMenu(e)
				}
			}
		}

		private fun showPopupMenu(e: MouseEvent) {
			getPathForLocation(e.x, e.y)?.let { path ->
				requestFocusInWindow()
				selectionPath = path
				val menu = actions.getPopupMenu(path.lastPathComponent as DefaultMutableTreeNode)
				menu?.show(this@LibraryTreeViewSwing, e.x, e.y)
			}
		}
	}

	private inner class EnterKeyListener : KeyAdapter() {
		override fun keyPressed(e: KeyEvent) {
			if (e.keyCode == KeyEvent.VK_ENTER && controller.selectedItem is LibraryItem) {
				openSelectedItem()
				SwingUtilities.invokeLater { requestFocusInWindow() }
			}
		}
	}
}
