package ch.scorpion.antares.model.testcase.result

import ch.scorpion.antares.model.DigitalGraph
import ch.scorpion.antares.model.testcase.CombinedTestRunResult
import ch.scorpion.antares.model.testcase.DisplayTestRunResults
import ch.scorpion.antares.model.testcase.TestRunResult
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.swing.JTreeUtil
import ch.scorpion.jabbah.base.swing.ShowSidebarPaneContentRequest
import ch.scorpion.jabbah.base.swing.UiUtil
import ch.scorpion.jabbah.draw.richtext.RichTextLabel
import ch.scorpion.jabbah.graph.ui.MetaGraphIconProvider
import ch.scorpion.jabbah.graph.ui.graphpanel.EditedGraphViewEvent
import java.awt.BorderLayout
import java.awt.Component
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JSplitPane
import javax.swing.JTree
import javax.swing.SwingUtilities
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreeModel
import javax.swing.tree.TreeNode
import kotlin.math.max

/**
 * Shows a tree of [TestRunResult].
 */
class TestRunResultsPanel(
	private val eventBus: EventBus = BaseModule.eventBus
) : JPanel() {

	private val displayTestRunHandler: EventHandler<DisplayTestRunResults> = { update(it.results) }
	private val editedGraphViewHandler: EventHandler<EditedGraphViewEvent> = { update(listOf() )}

	private val splitPane = JSplitPane(JSplitPane.HORIZONTAL_SPLIT)
	private val tree = JTree(createTreeModel(listOf()))
	private val detailsPanel = CombinedTestRunResultPanelSwing(null)

	init {
		tree.cellRenderer = TreeRenderer()
		tree.rowHeight = 24
		tree.showsRootHandles = true
		tree.addTreeSelectionListener { handleTreeSelection() }

		buildUI()

		eventBus.register(DisplayTestRunResults::class, displayTestRunHandler)
		eventBus.register(EditedGraphViewEvent::class, editedGraphViewHandler)
	}

	fun dispose() {
		BaseModule.settings.set("testRunResultsPanel.splitPos", splitPane.dividerLocation)
		eventBus.unregister(displayTestRunHandler)
		eventBus.unregister(editedGraphViewHandler)
	}

	private fun buildUI() {
		layout = BorderLayout()
		val treeScroll = JScrollPane(tree, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED)
		splitPane.add(treeScroll)
		splitPane.add(detailsPanel)
		splitPane.dividerLocation = max(100, BaseModule.settings.getInt("testRunResultsPanel.splitPos", 200))
		add(splitPane, BorderLayout.CENTER)
	}

	private fun update(results: List<CombinedTestRunResult>) {
		tree.model = createTreeModel(results)

		JTreeUtil.findTreeNode(tree.model.root as TreeNode) {
			it is DefaultMutableTreeNode && it.userObject is CombinedTestRunResult
		}?.let {
			SwingUtilities.invokeLater {
				val path = JTreeUtil.getPath(it)
				tree.expandPath(path)
				tree.selectionPath = path
				eventBus.post(ShowSidebarPaneContentRequest(this))
			}
		}
	}

	private fun createTreeModel(results: List<CombinedTestRunResult>): TreeModel {
		val root = DefaultMutableTreeNode(Translations.getString("antares.testcase.results.title"))
		val map = mutableMapOf<DigitalGraph, DefaultMutableTreeNode>()

		for (result in results) {
			val circuitNode = map.computeIfAbsent(result.testcase.graph!!) {
				DefaultMutableTreeNode(result.testcase.graph!!).also { root.add(it) }
			}
			circuitNode.add(DefaultMutableTreeNode(result))
		}

		return DefaultTreeModel(root)
	}

	private fun handleTreeSelection() {
		val sel = (tree.selectionPath?.lastPathComponent as DefaultMutableTreeNode?)?.userObject
		if (sel is CombinedTestRunResult) {
			detailsPanel.setResults(sel)
		} else {
			detailsPanel.setResults(null)
		}
	}


	private class TreeRenderer : RichTextLabel() {

		// TODO Icon by Janis
		private val testcaseIcon = UiUtil.themedIcon("/img/usecase-16.png")

		override fun getTreeCellRendererComponent(tree: JTree?, value: Any?, sel: Boolean, expanded: Boolean, leaf: Boolean, row: Int, hasFocus: Boolean): Component {
			val label = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus) as RichTextLabel

			label.richText = null
			if ((value as DefaultMutableTreeNode).userObject is DigitalGraph) {
				val circuit = value.userObject as DigitalGraph
				label.text = circuit.name.getTranslation()
				label.icon = MetaGraphIconProvider.provideIcon(circuit.type, false, circuit.script != null)
			} else if (value.userObject is CombinedTestRunResult) {
				val results = value.userObject as CombinedTestRunResult
				label.text = results.testcase.name.getTranslation()
				label.icon = if (results.totalFailedCount == 0 && results.error == null) {
					CombinedTestRunResultPanelSwing.PASSED_ICON
				} else {
					CombinedTestRunResultPanelSwing.FAILED_ICON
				}
			} else {
				// Root
				label.icon = testcaseIcon
			}

			return label
		}
	}
}