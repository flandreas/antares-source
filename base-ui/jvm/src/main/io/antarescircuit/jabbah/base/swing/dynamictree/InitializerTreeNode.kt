package io.antarescircuit.jabbah.base.swing.dynamictree

import javax.swing.tree.TreeNode
import java.util.Enumeration

class InitializerTreeNode(

	private val parent: TreeNode, private val value: Any?) : TreeNode {

	/** ---- [Any] */

	override fun toString(): String {
		return value?.toString() ?: ""
	}

	/** ---- [TreeNode] */

	override fun children(): Enumeration<out TreeNode> {
		return object : Enumeration<TreeNode> {
			override fun hasMoreElements(): Boolean {
				return false
			}

			override fun nextElement(): TreeNode {
				throw NoSuchElementException("Initializer tree nodes do not have children.")
			}
		}
	}

	override fun getAllowsChildren(): Boolean = false

	override fun getChildAt(childIndex: Int): TreeNode {
		throw IndexOutOfBoundsException("Initializer tree nodes do not have children.")
	}

	override fun getChildCount(): Int = 0

	override fun getIndex(node: TreeNode): Int = -1

	override fun getParent(): TreeNode = this.parent

	override fun isLeaf(): Boolean = true

}
