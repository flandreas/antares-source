package ch.scorpion.jabbah.base.swing.dynamictree

import javax.swing.tree.TreeNode

/**
 * Strategy to dynamically initialize the children of [DynamicTreeNode]s.
 * Implementations of this interface are typically service-like objects that know
 * how to gather the necessary data that is to displayed as a tree, but are not themselves
 * UI tree objects.
 */
interface DynamicInitializer {

	/**
	 * Creates a [TreeNode] that is displayed as a single child while initializing the specified parent.
	 * @param parent the parent node for which to create the initializer node.
	 * @return the node used as single child while initializing.
	 */
	fun createInitializerTreeNode(parent: TreeNode): TreeNode

	/**
	 * Initializes the children of a dynamic node using the specified receiver.
	 * @param value of the parent to initialize.
	 * @param receiver the receiver of child data.
	 */
	fun initialize(value: Any, receiver: DynamicReceiver)
}