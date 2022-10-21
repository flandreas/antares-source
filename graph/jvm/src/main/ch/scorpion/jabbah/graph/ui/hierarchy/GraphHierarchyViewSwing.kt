package ch.scorpion.jabbah.graph.ui.hierarchy

import ch.scorpion.jabbah.app.ToolBar
import ch.scorpion.jabbah.base.swing.UiUtil
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeView
import java.awt.BorderLayout
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.tree.DefaultMutableTreeNode

class GraphHierarchyViewSwing(
	private val controller: GraphHierarchyController
) : JPanel(), GraphHierarchyView {

	private val treeView = GraphHierarchyTreeView()

	private val toolbar = ToolBar()

	init {
		controller.view = this
		buildUI()

		treeView.selectionModel.addTreeSelectionListener {
			controller.selectedSubGraphVerticeView = selectedSubGraphVerticeView
		}
	}

	override fun dispose() { }

	override fun refresh() {
		treeView.refresh(controller.rootGraphView)
	}

	private val selectedSubGraphVerticeView: SubGraphVerticeView<*>? get() {
		val path = treeView.selectionPath ?: return null
		if ((path.lastPathComponent as DefaultMutableTreeNode).userObject is SubGraphVerticeView<*>) {
			return (path.lastPathComponent as DefaultMutableTreeNode).userObject as SubGraphVerticeView<*>
		}
		return null
	}

	private fun buildUI() {
		layout = BorderLayout()

		toolbar.add(UiUtil.createToolBarButton(controller.refreshAction))
		toolbar.add(UiUtil.createToolBarButton(controller.openAction))

		add(toolbar, BorderLayout.NORTH)

		val scrollPane = JScrollPane(treeView)
		scrollPane.horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
		scrollPane.verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
		add(scrollPane, BorderLayout.CENTER)
	}
}