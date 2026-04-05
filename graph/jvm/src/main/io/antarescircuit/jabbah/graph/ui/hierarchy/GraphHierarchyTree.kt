package io.antarescircuit.jabbah.graph.ui.hierarchy

import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.invocation.InvocationHandler
import io.antarescircuit.jabbah.base.swing.dynamictree.*
import io.antarescircuit.jabbah.draw.drawable.RichTextDrawable
import io.antarescircuit.jabbah.draw.graphics.Font
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.vertice.SubGraphVerticeView
import javax.swing.tree.TreeNode

/**
 * Dynamically loads the inner [SubGraphVerticeViews][SubGraphVerticeView]
 * of an expanded [SubGraphVerticeView] node.
 */
class GraphHierarchyTree : DynamicInitializer {

	/** ---- [DynamicInitializer] */

	override fun createInitializerTreeNode(parent: TreeNode): TreeNode =
		InitializerTreeNode(parent, Translations.getString("graph.action.loading.desc"))

	override fun initialize(value: Any, receiver: DynamicReceiver) {
		InvocationHandler.invoke {
			when (value) {
				is SubGraphVerticeView<*> -> {
					if (value.model.getGraphIfNotBroken() != null) {
						val subGraphView = value.createSubGraphView(null)
						receiver.addChildren(createGraphViewNodes(subGraphView))
					}
				}
				is GraphView -> {
					receiver.addChildren(createGraphViewNodes(value))
				}
				else -> {
					receiver.addChildren(listOf())
				}
			}
		}
	}

	private fun createGraphViewNodes(graphView: GraphView): Array<DynamicTreeNodeValue> {
		return graphView
			.getSubGraphVerticeViews()
			.filter { it.model.getGraphIfNotBroken() != null }
			.sortedBy { it.describingName }
			.map { DynamicTreeNodeValue(it, true) }
			.toTypedArray()
	}
}

class GraphHierarchyTreeNode(
	value: Any,
	initializer: DynamicInitializer,
	notifier: DynamicNotifier,
	hasChildren: Boolean,
	private val font: Font
) : DynamicTreeNode(value, initializer, notifier, hasChildren) {

	var richTextDrawable: RichTextDrawable? = null
		private set

	init {
		if (value is GraphView) {
			richTextDrawable = RichTextDrawable.of(value.name.getTranslation(), font)
		} else if (value is SubGraphVerticeView<*>) {
			richTextDrawable = RichTextDrawable.of(value.describingName, font)
		}
	}

	override fun createChild(
		value: Any,
		initializer: DynamicInitializer,
		notifier: DynamicNotifier,
		hasChildren: Boolean
	): DynamicTreeNode {
		return GraphHierarchyTreeNode(value, initializer, notifier, hasChildren, font)
	}
}

class GraphHierarchyTreeModel(private val font: Font) : DynamicTreeModel() {
	override fun createNode(
		value: Any,
		initializer: DynamicInitializer,
		notifier: DynamicNotifier,
		hasChildren: Boolean
	): DynamicTreeNode {
		return GraphHierarchyTreeNode(value, initializer, notifier, hasChildren, font)
	}
}
