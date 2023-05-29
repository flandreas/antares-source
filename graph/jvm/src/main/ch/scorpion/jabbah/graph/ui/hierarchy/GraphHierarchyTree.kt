package ch.scorpion.jabbah.graph.ui.hierarchy

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.invocation.InvocationHandler
import ch.scorpion.jabbah.base.swing.dynamictree.DynamicInitializer
import ch.scorpion.jabbah.base.swing.dynamictree.DynamicReceiver
import ch.scorpion.jabbah.base.swing.dynamictree.DynamicTreeNodeValue
import ch.scorpion.jabbah.base.swing.dynamictree.InitializerTreeNode
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeView
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