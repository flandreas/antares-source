package ch.scorpion.jabbah.graph.ui.hierarchy

import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.swing.JTreeUtil
import ch.scorpion.jabbah.base.ui.UIBasics.PROP_TREE_SHOW_ROOT_HANDLES
import ch.scorpion.jabbah.draw.graphics.Graphics2DJvm
import ch.scorpion.jabbah.draw.richtext.RichTextLabel
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.ui.MetaGraphIconProvider
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeView
import java.awt.Component
import java.awt.Font
import javax.swing.JTree
import javax.swing.tree.DefaultMutableTreeNode
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
		setShowsRootHandles(BaseModule.properties.getBoolean(PROP_TREE_SHOW_ROOT_HANDLES))
		setCellRenderer(Renderer())
	}

	fun refresh(graphView: GraphView?) {
		graphHierarchyTree = graphView?.let { GraphHierarchyTree() }
		model = graphHierarchyTree?.let { GraphHierarchyTreeModel(Graphics2DJvm.fromAwtFont(font)).apply { initDynamicRoot(graphView!!, it, true) } }
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

	private inner class Renderer : RichTextLabel() {
		override fun getTreeCellRendererComponent(tree: JTree?, value: Any?, sel: Boolean, expanded: Boolean, leaf: Boolean, row: Int, hasFocus: Boolean): Component {
			val label = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus) as RichTextLabel
			label.font = this@GraphHierarchyTreeView.font

			if (value is GraphHierarchyTreeNode) {
				label.richText = value.richTextDrawable
				if (value.userObject is SubGraphVerticeView<*>) {
					val subGraphVV = value.userObject as SubGraphVerticeView<*>
					configureIcon(label, subGraphVV.model.getGraphIfPresent())
				} else if (value.userObject is GraphView) {
					val graphView = value.userObject as GraphView
					configureIcon(label, graphView.graph)
				}
			}

			return label
		}

		private fun configureIcon(label: RichTextLabel, graph: Graph?) {
			label.icon = graph?.type?.let {
				MetaGraphIconProvider.provideIcon(it, false, StringUtils.isNotBlank(graph.script))
			}
		}
	}
}