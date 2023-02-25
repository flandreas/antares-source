package ch.scorpion.jabbah.graph.ui.hierarchy

import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.swing.JTreeUtil
import ch.scorpion.jabbah.base.swing.dynamictree.DynamicTreeModel
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.ui.MetaGraphIconProvider
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeView
import java.awt.Component
import java.awt.Font
import javax.swing.JLabel
import javax.swing.JTree
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeCellRenderer
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreeNode

/**
 * Displays a [GraphHierarchyTree] as a dynamically loaded tree.
 * Nodes representing a [SubGraphVerticeView] whose [Graph] is scripted
 * are rendered using an italic font.
 */
class GraphHierarchyTreeView : JTree(DefaultTreeModel(DefaultMutableTreeNode("Empty"))) {

	private var graphHierarchyTree: GraphHierarchyTree? = null
	private val scriptedFont = font.deriveFont(Font.ITALIC)

	init {
		rootVisible = true
		showsRootHandles = true
		setCellRenderer(Renderer())
	}

	fun refresh(graphView: GraphView?) {
		graphHierarchyTree = graphView?.let { GraphHierarchyTree() }
		model = graphHierarchyTree?.let { DynamicTreeModel(graphView!!, it, true) }
	}

	fun remove(subGraphVerticeView: SubGraphVerticeView<*>) {
		model?.root?.let { root ->
			JTreeUtil.findTreeNode(root as TreeNode) {
				(it as DefaultMutableTreeNode).userObject === subGraphVerticeView
			}?.let {
				val index = (root as DefaultMutableTreeNode).getIndex(it)
				root.remove(it as DefaultMutableTreeNode)
				(model as DefaultTreeModel).nodesWereRemoved(model!!.root as TreeNode, intArrayOf(index), arrayOf(it))
			}
		}
	}

	private inner class Renderer : DefaultTreeCellRenderer() {
		override fun getTreeCellRendererComponent(tree: JTree?, value: Any?, sel: Boolean, expanded: Boolean, leaf: Boolean, row: Int, hasFocus: Boolean): Component {
			val label = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus) as JLabel
			label.font = this@GraphHierarchyTreeView.font

			if (value is DefaultMutableTreeNode && value.userObject is SubGraphVerticeView<*>) {
				val subGraphVV = value.userObject as SubGraphVerticeView<*>
				label.text = subGraphVV.describingName
				label.icon = subGraphVV.model.getGraphIfPresent()?.type?.let {
					MetaGraphIconProvider.provideIcon(it, false)
				}
				if (StringUtils.isNotBlank(subGraphVV.subGraphVertice?.getGraphIfPresent()?.script)) {
					label.font = scriptedFont
				}
			}

			return label
		}
	}
}