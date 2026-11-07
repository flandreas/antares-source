package io.antarescircuit.jabbah.base.swing.dynamictree

/** Receiver interface for node change notifications. */
interface DynamicNotifier {

	/**
	 * Notifies the receiver that the given structure below a node has changed.
	 * @param node the node that has changed.
	 */
	fun notifyNodeStructureChanged(node: DynamicTreeNode)

	/**
	 * Notifies the receiver that a node has changed.
	 * @param node the node that has changed.
	 */
	fun notifyNodeChanged(node: DynamicTreeNode)

	/**
	 * Notifies the receiver that a node has been added.
	 * @param node the node that has been added.
	 */
	fun notifyNodeAdded(node: DynamicTreeNode, index: Int)

	/**
	 * Notifies the receiver that a node has been removed.
	 * @param node the node that has been removed.
	 */
	fun notifyNodeRemoved(node: DynamicTreeNode, index: Int, removedNode: DynamicTreeNode)
}