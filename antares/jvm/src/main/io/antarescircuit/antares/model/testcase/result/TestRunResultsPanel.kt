package io.antarescircuit.antares.model.testcase.result

import io.antarescircuit.antares.model.DigitalGraph
import io.antarescircuit.antares.model.testcase.CombinedTestRunResult
import io.antarescircuit.antares.model.testcase.DisplayTestRunResults
import io.antarescircuit.antares.model.testcase.TestRunResult
import io.antarescircuit.jabbah.base.AbstractAction
import io.antarescircuit.jabbah.base.Action
import io.antarescircuit.jabbah.base.ActionWrapperSwing
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.event.ActionEvent
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.event.EventHandler
import io.antarescircuit.jabbah.base.invocation.InvocationHandler
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.base.swing.JTreeUtil
import io.antarescircuit.jabbah.base.swing.PopupMenuButton
import io.antarescircuit.jabbah.base.swing.ShowSidebarPaneContentRequest
import io.antarescircuit.jabbah.base.swing.UiUtil
import io.antarescircuit.jabbah.base.ui.UIBasics.PROP_TREE_SHOW_ROOT_HANDLES
import io.antarescircuit.jabbah.draw.richtext.RichTextLabel
import io.antarescircuit.jabbah.edit.model.text.NamableTreeNode
import io.antarescircuit.jabbah.graph.library.CurrentLibraryEvent
import io.antarescircuit.jabbah.graph.library.LibraryHolder
import io.antarescircuit.jabbah.graph.library.LibraryModule
import io.antarescircuit.jabbah.graph.library.OpenContainerLibraryElementRequest
import io.antarescircuit.jabbah.graph.ui.MetaGraphIconProvider
import io.antarescircuit.jabbah.graph.ui.graphpanel.EditedGraphViewEvent
import java.awt.BorderLayout
import java.awt.Component
import java.awt.event.MouseEvent
import javax.swing.*
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreeModel
import javax.swing.tree.TreeNode
import kotlin.math.max

/**
 * Shows a tree of [TestRunResult] on the left and the details of the selected [CombinedTestRunResult]
 * in a [CombinedTestRunResultPanelSwing] on the right.
 */
class TestRunResultsPanel(
	private val eventBus: EventBus = BaseModule.eventBus,
	private val libraryHolder: LibraryHolder = LibraryModule.libraryHolder
) : JPanel() {

	private val displayTestRunHandler: EventHandler<DisplayTestRunResults> = { update(it.results) }

	private val editedGraphViewHandler: EventHandler<EditedGraphViewEvent> = {
		if (it.oldGraphView != null) {
			// Don't update for snapshot replays of the same Graph
			if (it.newGraphView == null || it.newGraphView!!.graph?.uuid != it.oldGraphView!!.graph?.uuid) {
				update(listOf() )
			}
		}
	}

	private val currentLibraryHandler: EventHandler<CurrentLibraryEvent> = {
		if (libraryHolder.l == null) {
			clear()
		}
	}

	private val toolBar = JToolBar()
	private val splitPane = JSplitPane(JSplitPane.HORIZONTAL_SPLIT)
	private val tree = JTree()
	private val detailsPanel = CombinedTestRunResultPanelSwing(null)
	private val popupMenu = JPopupMenu()

	private var results: List<CombinedTestRunResult> = emptyList()

	private var statistics: Statistics = Statistics(0, 0)

	private val expandAllAction: Action = ExpandAllAction()

	private val filterActions = listOf(
		TestResultFilterAction(this, TestResultFilter.All),
		TestResultFilterAction(this, TestResultFilter.Failed),
		TestResultFilterAction(this, TestResultFilter.Passed)
	)

	private val filterMenuItems = filterActions.map { JRadioButtonMenuItem(ActionWrapperSwing(it)) }

	private val showFilterMenuAction: Action = ShowFilterMenuAction()
	private val showFilterButton = UiUtil.createToolBarButton(showFilterMenuAction, toggle = true)

	private val currentFilter: TestResultFilter get() = filterActions.first { it.selected }.filter

	val clearAction: Action = ClearAction()

	init {
		ButtonGroup().also { bg ->
			filterMenuItems.forEach { bg.add(it) }
		}

		tree.cellRenderer = TreeRenderer()
		tree.rowHeight = 24
		tree.setShowsRootHandles(BaseModule.properties.getBoolean(PROP_TREE_SHOW_ROOT_HANDLES))
		tree.addTreeSelectionListener { handleTreeSelection() }
		tree.addMouseListener(MouseListener())

		popupMenu.add(ActionWrapperSwing(expandAllAction))

		buildUI()

		eventBus.register(DisplayTestRunResults::class, displayTestRunHandler)
		eventBus.register(EditedGraphViewEvent::class, editedGraphViewHandler)
		eventBus.register(CurrentLibraryEvent::class, currentLibraryHandler)

		update(emptyList())

		PopupMenuButton.install(showFilterButton, filterMenuItems)
	}

	fun dispose() {
		BaseModule.settings.set("testRunResultsPanel.splitPos", splitPane.dividerLocation)
		eventBus.unregister(displayTestRunHandler)
		eventBus.unregister(editedGraphViewHandler)
		eventBus.unregister(currentLibraryHandler)
	}

	private fun buildUI() {
		layout = BorderLayout()
		buildToolBar()

		val treeScroll = JScrollPane(tree, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED)

		val leftPanel = JPanel(BorderLayout())
		leftPanel.add(toolBar, BorderLayout.WEST)
		leftPanel.add(treeScroll, BorderLayout.CENTER)

		splitPane.add(leftPanel)
		splitPane.add(detailsPanel)
		splitPane.dividerLocation = max(100, BaseModule.settings.getInt("testRunResultsPanel.splitPos", 200))
		add(splitPane, BorderLayout.CENTER)
	}

	private fun buildToolBar() {
		toolBar.layout = BoxLayout(toolBar, BoxLayout.Y_AXIS)
		// TODO Same as in SidebarPanel
		toolBar.border = BorderFactory.createEmptyBorder(2, 5, 2, 5)
		toolBar.add(showFilterButton)
		toolBar.add(Box.createVerticalGlue())
	}

	private fun update(results: List<CombinedTestRunResult>) {
		this.results = results
		filterActions.first().selected = true

		statistics = calculateStatistics(results)
		display(results)
	}

	fun applyFilter(filter: TestResultFilter) {
		display(results.filter { filter.accept(it) })
		showFilterButton.isSelected = filter != TestResultFilter.All
	}

	private fun display(results: List<CombinedTestRunResult>) {
		tree.model = createTreeModel(results)

		JTreeUtil.findTreeNode(tree.model.root as TreeNode) {
			it is DefaultMutableTreeNode && it.userObject is CombinedTestRunResult
		}?.let {
			SwingUtilities.invokeLater {
				val path = JTreeUtil.getPath(it)
				JTreeUtil.expandAll(tree)
				tree.selectionPath = path
				eventBus.post(ShowSidebarPaneContentRequest(this))
			}
		}
	}

	private fun createTreeModel(results: List<CombinedTestRunResult>): TreeModel {
		val rootName = if (statistics.failedTestVectorCount == 0) {
			Translations.getString("antares.testcase.results.total.success", statistics.totalTestVectorCount)
		} else {
			Translations.getString("antares.testcase.results.total.failed", statistics.failedTestVectorCount, statistics.totalTestVectorCount)
		}
		val root = DefaultMutableTreeNode(rootName)
		val map = mutableMapOf<DigitalGraph, DefaultMutableTreeNode>()

		for (result in results) {
			val circuitNode = map.computeIfAbsent(result.testcase.graph!!) {
				NamableTreeNode(result.testcase.graph!!).also { root.add(it) }
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

	private fun clear() {
		update(emptyList())
		detailsPanel.setResults(null)
	}

	private fun showTreePopupMenu(e: MouseEvent) {
		tree.getPathForLocation(e.x, e.y)?.let { path ->
			tree.selectionPath = path
			if (path.lastPathComponent === tree.model.root) {
				requestFocusInWindow()
				popupMenu.show(this@TestRunResultsPanel, e.x, e.y)
			}
		}
	}

	private fun calculateStatistics(results: List<CombinedTestRunResult>): Statistics =
		Statistics(
			results.filter { !it.ignored }.size,
			results.filter { it.failed }.size
		)

	private data class Statistics(
		val totalTestVectorCount: Int,
		val failedTestVectorCount: Int
	)

	private inner class TreeRenderer : RichTextLabel() {
		override fun getTreeCellRendererComponent(tree: JTree?, value: Any?, sel: Boolean, expanded: Boolean, leaf: Boolean, row: Int, hasFocus: Boolean): Component {
			val label = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus) as RichTextLabel

			label.richText = null
			if ((value as DefaultMutableTreeNode).userObject is DigitalGraph) {
				val circuit = value.userObject as DigitalGraph
				label.richText = (value as NamableTreeNode).richTextName.value
				label.icon = MetaGraphIconProvider.provideIcon(circuit.type, false, circuit.script != null)
			} else if (value.userObject is CombinedTestRunResult) {
				val results = value.userObject as CombinedTestRunResult
				label.text = results.testcase.name.getTranslation()
				label.icon = if (results.ignored) {
					CombinedTestRunResultPanelSwing.IGNORED_ICON
				} else if (results.totalFailedCount == 0 && results.error == null) {
					CombinedTestRunResultPanelSwing.PASSED_ICON
				} else {
					CombinedTestRunResultPanelSwing.FAILED_ICON
				}
			} else {
				// Root
				label.icon = if (statistics.failedTestVectorCount == 0) {
					CombinedTestRunResultPanelSwing.PASSED_ICON
				} else {
					CombinedTestRunResultPanelSwing.FAILED_ICON
				}
			}

			return label
		}
	}

	private fun openSelectedGraph() {
		val obj = (tree.selectionPath?.lastPathComponent as DefaultMutableTreeNode?)?.userObject
		if (obj is CombinedTestRunResult) {
			libraryHolder.l?.getContainerLibraryElement(obj.source.uuid)?.let {
				InvocationHandler.invoke {
					eventBus.post(OpenContainerLibraryElementRequest(it))
				}
			}
		}
	}

	private inner class MouseListener : java.awt.event.MouseAdapter() {
		override fun mousePressed(e: MouseEvent?) {
			when (e?.button) {
				MouseEvent.BUTTON3 -> showTreePopupMenu(e)
				MouseEvent.BUTTON1 -> {
					if (e.clickCount == 2) {
						openSelectedGraph()
					}
				}
			}
		}
	}

	private inner class ClearAction : AbstractAction(
		"antares.testcase.results.action.clear",
		"/img/trash-16.png"
	) {
		init {
			description = name
		}

		override fun execute(event: ActionEvent) {
			clear()
		}
	}

	private inner class ExpandAllAction : AbstractAction(
		"library.action.expandAll"
	) {
		override fun execute(event: ActionEvent) {
			tree.selectionPath?.let { JTreeUtil.expandAll(tree, it) }
		}
	}

	private inner class ShowFilterMenuAction : AbstractAction(
		"antares.testcase.results.action.filter",
		"/img/filter-16.png"
	) {
		override fun execute(event: ActionEvent) {
			// Showing popup menu is done by PopupMenuButton object
			showFilterButton.isSelected = currentFilter != TestResultFilter.All
		}
	}
}