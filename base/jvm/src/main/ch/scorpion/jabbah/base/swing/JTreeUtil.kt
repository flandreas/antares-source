package ch.scorpion.jabbah.base.swing

import java.util.*
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

	/** Finds the first [TreeNode] in the subtree rooted in ´treeNode´ that fulfills the specified condition.*/
	fun findTreeNode(treeNode: TreeNode, cond: (TreeNode) -> Boolean): TreeNode? {
		if (cond.invoke(treeNode)) {
			return treeNode
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
}
