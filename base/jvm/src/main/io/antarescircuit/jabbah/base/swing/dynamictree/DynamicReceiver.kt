package io.antarescircuit.jabbah.base.swing.dynamictree

import javax.swing.tree.TreeNode
import javax.swing.tree.MutableTreeNode

/** Strategy to dynamically receive child data. */
interface DynamicReceiver {

	/**
	 * Adds children with the specified values to the receiver.
	 * @param values the values of the children to add.
	 */
	fun addChildren(values: Array<DynamicTreeNodeValue>)

	fun addChildren(children: List<MutableTreeNode>)

	fun getPath(): Array<TreeNode>
}