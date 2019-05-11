package ch.scorpion.jabbah.base.swing

import ch.scorpion.jabbah.base.swing.dynamictree.DynamicTreeNode
import javax.swing.JTree
import javax.swing.tree.TreeModel
import javax.swing.tree.TreeNode
import javax.swing.tree.TreePath

/** Utility methods for working with [JTree] and [TreeModel].*/
object JTreeUtil {

    /** Creates a [TreePath] for the specified [TreeNode] by traversing the tree back to the root.*/
    fun getPath(treeNode: TreeNode): TreePath {
        val nodes = mutableListOf<TreeNode>()
        nodes.add(treeNode)
        var node: TreeNode? = treeNode.parent
        while (node != null) {
            nodes.add(0, node)
            node = node.parent
        }
        return TreePath(nodes.toTypedArray())
    }

	/**
	 * Finds the first [TreeNode] in the subtree rooted in `treeNode` that fulfills the specified condition.
	 * Stops recursion on uninitialized [TreeNode]s.
	 */
	fun findTreeNode(treeNode: TreeNode, cond: (TreeNode) -> Boolean): TreeNode? {
		if (cond.invoke(treeNode)) {
			return treeNode
		}
		if (treeNode is DynamicTreeNode && !treeNode.isInitialized) {
			return null
		}
		val children = treeNode.children()
		while (children.hasMoreElements()) {
			val result = findTreeNode(children.nextElement() as TreeNode, cond)
			if (result != null) {
				return result
			}
		}
		return null
	}

	fun expandAll(node: TreeNode) {
		for (child in node.children().iterator()) {
			if (child.childCount > 0) {
				expandAll(child)
			}
		}
	}

	fun expandAll(tree: JTree) {
		expandAll(tree, TreePath(tree.model.root))
	}

	fun expandAll(tree: JTree, parent: TreePath) {
		val node = parent.lastPathComponent as TreeNode
		if (node.childCount >= 0) {
			for (child in node.children()) {
				val path = parent.pathByAddingChild(child)
				expandAll(tree, path)
			}
		}
		tree.expandPath(parent)
	}

	fun collapseAll(tree: JTree, parent: TreePath) {
		val node = parent.lastPathComponent as TreeNode
		if (node.childCount >= 0) {
			for (child in node.children()) {
				val path = parent.pathByAddingChild(child)
				collapseAll(tree, path)
			}
		}
		tree.collapsePath(parent)
	}
}
