package ch.scorpion.jabbah.base.swing.dynamictree

import ch.scorpion.jabbah.base.swing.UiUtil
import java.util.*
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.MutableTreeNode
import javax.swing.tree.TreeNode

/**
 * Dynamic [TreeNode] that supports lazy initialization.
 *
 * @property initializer Strategy that will initialize this node
 * @property notifier Notifier to use if the children of this node change
 * @param hasChildren determines whether this [DynamicTreeNode] has dynamic children, i.e. whether
 * it has not already loaded its children dynamically.
 */
open class DynamicTreeNode(
	value: Any,
	private val initializer: DynamicInitializer,
	private val notifier: DynamicNotifier,
	hasChildren: Boolean = true
) : DefaultMutableTreeNode(value, true), DynamicReceiver {

	/** The current state of this dynamic node. */
	var state: State
		private set

	/** The tree node temporarily used as child node while initializing. */
	private val initializerTreeNode: TreeNode by lazy { initializer.createInitializerTreeNode(this) }

	/** Check if this node has been initialized. */
	val isInitialized: Boolean get() = state == INITIALIZED

	/**
	 * Primitive to check if this node is a leaf node.
	 * Does not take the dynamic state of the node into account!
	 * @return `true` if the receiver is a leaf.
	 */
	private val primIsLeaf get() = super.isLeaf()

	/**
	 * Primitive to return the number of child nodes of the receiver.
	 * Does not take the dynamic state of the node into account!
	 * @return the number of child nodes of the receiver.
	 */
	private val primChildCount: Int get() = super.getChildCount()

	init {
		this.state = if (hasChildren) UNINITIALIZED else INITIALIZED
	}

	/** ---- TreeNode */

	/**
	 * Check if this node might have children.
	 * @return `true` always.
	 */
	override fun getAllowsChildren(): Boolean {
		return true
	}

	/**
	 * Check if this node is a leaf node.
	 * @return `true` if this dynamic node has been loaded and
	 * does not have any children, `false` either if the node
	 * has not been loaded yet or if it has children.
	 */
	@Synchronized
	override fun isLeaf(): Boolean {
		return this.state.isLeaf(this)
	}

	/**
	 * Returns the number of children of this node.
	 */
	@Synchronized
	override fun getChildCount(): Int {
		return this.state.getChildCount(this)
	}

	/**
	 * Returns the child node at the specified index.
	 * @throws ArrayIndexOutOfBoundsException if there is no child at this index.
	 */
	@Synchronized
	override fun getChildAt(index: Int): TreeNode? {
		return this.state.getChildAt(this, index)
	}

	/**
	 * Returns the children of the receiver as an enumeration.
	 */
	@Synchronized
	override fun children(): Enumeration<TreeNode> {
		return this.state.getChildren(this)
	}

	/** ---- [DynamicReceiver] */

	/**
	 * Factory method to dynamically add children to this node.
	 *
	 * Actual modification is performed on dispatch thread.
	 * @param values the values of the children to add.
	 */
	@Synchronized
	override fun addChildren(values: Array<DynamicTreeNodeValue>) {
		if (UiUtil.eventQueueInvoker.invoke { addChildren(values) }) {
			return
		}
		if (this.state === INITIALIZED) {
			throw IllegalStateException("Node is already initialized.")
		}
		this.state = INITIALIZED
		for (i in values.indices) {
			super.add(
				createChild(values[i].value, this.initializer, this.notifier, values[i].hasChildren())
			)
		}
		this.notifier.notifyNodeStructureChanged(this)
	}

	override fun addChildren(children: List<MutableTreeNode>) {
		if (UiUtil.eventQueueInvoker.invoke { addChildren(children) }) {
			return
		}
		if (this.state === INITIALIZED) {
			throw IllegalStateException("Node is already initialized.")
		}
		this.state = INITIALIZED
		for (node in children) {
			super.add(node)
		}
		this.notifier.notifyNodeStructureChanged(this)
	}

	/** ---- [DynamicTreeNode] */

	fun updateNodeValue(value: Any) {
		userObject = value
		notifier.notifyNodeChanged(this)
	}

	private fun initialize() {
		if (this.state != UNINITIALIZED) {
			return;
		}
		this.state = INITIALIZING;
		primInitialize();
	}

	/**
	 * Primitive to perform the actual dynamic tree node initialization.
	 * Actual initialization is performed on dispatch thread.
	 */
	private fun primInitialize() {
		if (UiUtil.eventQueueInvoker.invoke { primInitialize() }) {
			return
		}

		this.initializer.initialize(getUserObject(), this)
		this.notifier.notifyNodeStructureChanged(this)
	}

	/**
	 * Primitive to return the child node at the specified index.
	 * @param index the index of the child node to return.
	 * @return the child node at the specified index.
	 * @throws ArrayIndexOutOfBoundsException if there is no child at this index.
	 */
	private fun primGetChildAt(index: Int): TreeNode {
		return super.getChildAt(index)
	}

	/**
	 * Primitive to return the children of the receiver as an enumeration.
	 * Does not take the dynamic state of the node into account!
	 * @return the children of the receiver as an enumeration.
	 */
	private fun primGetChildren(): Enumeration<TreeNode> {
		return super.children() as Enumeration<TreeNode>
	}

	@Synchronized
	fun addChildValue(value: Any, hasChildren: Boolean) {
		if (this.state === INITIALIZED) {
			val node = createChild(value, this.initializer, this.notifier, hasChildren)
			super.add(node)
			this.notifier.notifyNodeAdded(this, this.childCount - 1)
		}
	}

	@Synchronized
	fun removeChild(child: DynamicTreeNode) {
		if (this.state === INITIALIZED) {
			val index = this.getIndex(child)
			if (index >= 0) {
				super.remove(index)
				this.notifier.notifyNodeRemoved(this, index, child)
			}
		}
	}

	protected open fun createChild(value: Any, initializer: DynamicInitializer, notifier: DynamicNotifier, hasChildren: Boolean): DynamicTreeNode {
		return DynamicTreeNode(value, initializer, notifier, hasChildren)
	}

	companion object {

		abstract class State {
			abstract fun isInitialized(): Boolean
			abstract fun isLeaf(node: DynamicTreeNode): Boolean
			abstract fun getChildCount(node: DynamicTreeNode): Int
			abstract fun getChildAt(node: DynamicTreeNode, index: Int): TreeNode
			abstract fun getChildren(node: DynamicTreeNode): Enumeration<TreeNode>
		}

		/** Base strategy used while the node is not yet initialized. */
		private open class InitializingState : State() {

			override fun isInitialized(): Boolean = false

			override fun isLeaf(node: DynamicTreeNode): Boolean = false

			override fun getChildCount(node: DynamicTreeNode): Int = 1

			override fun getChildAt(node: DynamicTreeNode, index: Int): TreeNode {
				if (index != 0) {
					throw ArrayIndexOutOfBoundsException("Node only has one child while initializing.")
				}
				return node.initializerTreeNode
			}

			override fun getChildren(node: DynamicTreeNode): Enumeration<TreeNode> {
				return object : Enumeration<TreeNode> {
					private var read: Boolean = false
					override fun hasMoreElements(): Boolean {
						return !this.read
					}

					override fun nextElement(): TreeNode {
						if (this.read) {
							throw NoSuchElementException("Node only has one child while initializing.")
						}
						this.read = true
						return node.initializerTreeNode
					}
				}
			}
		}

		private val UNINITIALIZED = object : InitializingState() {

			override fun toString(): String {
				return "Uninitialized"
			}

			override fun getChildCount(node: DynamicTreeNode): Int {
				node.initialize();
				// The node might have changed to the INITIALIZED state
				return node.state.getChildCount(node)
			}

			override fun getChildAt(node: DynamicTreeNode, index: Int): TreeNode {
				node.initialize();
				return super.getChildAt(node, index);
			}

			override fun getChildren(node: DynamicTreeNode): Enumeration<TreeNode> {
				node.initialize();
				return super.getChildren(node);
			}
		}

		/** State used while a node is currently initializing.*/
		private val INITIALIZING = object : InitializingState() {
			override fun toString(): String = "Initializing"
		}

		private val INITIALIZED = object : State() {
			override fun toString(): String = "Initialized"

			override fun isInitialized(): Boolean = true

			override fun isLeaf(node: DynamicTreeNode): Boolean {
				return node.primIsLeaf;
			}

			override fun getChildCount(node: DynamicTreeNode): Int {
				return node.primChildCount;
			}

			override fun getChildAt(node: DynamicTreeNode, index: Int): TreeNode {
				return node.primGetChildAt(index);
			}

			override fun getChildren(node: DynamicTreeNode): Enumeration<TreeNode> {
				return node.primGetChildren();
			}
		}
	}
}