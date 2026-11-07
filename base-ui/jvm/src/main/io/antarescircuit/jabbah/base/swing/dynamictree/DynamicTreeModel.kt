package io.antarescircuit.jabbah.base.swing.dynamictree

import java.awt.EventQueue
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreeModel

/**
 * [TreeModel] for dynamic trees.
 * Clients must call [initDynamicRoot] after initialization.
 */
open class DynamicTreeModel() : DefaultTreeModel(null), DynamicNotifier {

	/**
	 * Initializes a new dynamic tree model for the specified root object and child initializer.
	 * @param root the root object user value.
	 * @param initializer the initializer strategy to use to lazily attach children to nodes.
	 * @param hasChildren determines whether the root [DynamicTreeNode] of this [DynamicTreeNode] has dynamic children,
	 * i.e. whether it has not already loaded its children dynamically.
	 */
	fun initDynamicRoot(
		root: Any,
		initializer: DynamicInitializer,
		hasChildren: Boolean = true
	) {
		setRoot(createNode(root, initializer, this, hasChildren))
	}

	/** ---- [DynamicNotifier] */

	/**
	 * Notifies the tree model that the given node has changed.
	 * @param node the node that has changed.
	 */
	override fun notifyNodeStructureChanged(node: DynamicTreeNode) {
		EventQueue.invokeLater { this@DynamicTreeModel.reload(node) }
	}

	override fun notifyNodeChanged(node: DynamicTreeNode) {
		EventQueue.invokeLater { this@DynamicTreeModel.nodeChanged(node) }
	}

	override fun notifyNodeAdded(node: DynamicTreeNode, index: Int) {
		EventQueue.invokeLater { this@DynamicTreeModel.nodesWereInserted(node, intArrayOf(index)) }
	}

	override fun notifyNodeRemoved(node: DynamicTreeNode, index: Int, removedNode: DynamicTreeNode) {
		EventQueue.invokeLater { this@DynamicTreeModel.nodesWereRemoved(node, intArrayOf(index), arrayOf<Any>(removedNode)) }
	}

	/** ---- [DynamicTreeModel] */

	/**
	 * Creates a new dynamic tree node with the specified user value and initializer.
	 *
	 * Intended to be overwritten by subclasses in order to implement
	 * [DynamicTreeNode] objects of specialized types. This implementation returns an instance of
	 * [DynamicTreeNode].
	 *
	 * @param value the user value encapsulated by this dynamic node.
	 * @param initializer the initializer strategy to use to lazily attach children to this node.
	 * @param notifier the notifier to use if the children of this node change.
	 * @param hasChildren determines whether the created [DynamicTreeNode] has dynamic children, i.e. whether
	 * it has not already loaded its children dynamically.
	 */
	protected open fun createNode(value: Any, initializer: DynamicInitializer, notifier: DynamicNotifier, hasChildren: Boolean): DynamicTreeNode {
		return DynamicTreeNode(value, initializer, notifier, hasChildren)
	}
}