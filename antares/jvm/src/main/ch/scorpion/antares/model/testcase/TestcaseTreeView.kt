package ch.scorpion.antares.model.testcase

import ch.scorpion.antares.model.DigitalGraph
import ch.scorpion.jabbah.app.Application
import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.ActionWrapperSwing
import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.swing.JTreeUtil
import ch.scorpion.jabbah.base.swing.UiUtil
import ch.scorpion.jabbah.draw.graphics.Font
import ch.scorpion.jabbah.draw.graphics.Graphics2DJvm
import ch.scorpion.jabbah.draw.richtext.RichTextLabel
import ch.scorpion.jabbah.edit.model.text.NamableTreeNode
import ch.scorpion.jabbah.edit.model.text.description.NameChangedEvent
import ch.scorpion.jabbah.execution.scheduler.SchedulerActivationStateEvent
import ch.scorpion.jabbah.graph.GraphApplicationContextHolder
import ch.scorpion.jabbah.graph.app.ApplicationModeHolder
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.ui.MetaGraphIconProvider
import java.awt.Component
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JLabel
import javax.swing.JPopupMenu
import javax.swing.JTree
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreeNode
import javax.swing.tree.TreeSelectionModel

/**
 * Displays the tree of [Testcase]s of a [DigitalGraph].
 * TODO Similarities with UsecaseTreeView (and ScenarioTreeView): Extract commonalities?
 */
class TestcaseTreeView(
	application: Application,
	applicationModeHolder: ApplicationModeHolder,
	applicationContextHolder: GraphApplicationContextHolder,
	private val eventBus: EventBus = BaseModule.eventBus
) : JTree(TestcaseTreeModel(null, null)) {

	/** The [JPopupMenu] to be displayed for the [Graph] node.*/
	private val graphPopupMenu = JPopupMenu()

	private val testcasePopupMenu = JPopupMenu()

	private val rightMouseListener = RightMouseListener()

	private val testcaseTreeModel: TestcaseTreeModel get() = model!! as TestcaseTreeModel

	private val testcaseAddedHandler: EventHandler<TestcaseAddedEvent> = {
		if (it.graph === this.graph) {
			val testcaseNode = testcaseTreeModel.addTestcase(it.testcase)
			selectionPath = JTreeUtil.getPath(testcaseNode)
		}
	}

	private val testcaseRemovedHandler: EventHandler<TestcaseRemovedEvent> = {
		if (it.graph === this.graph) {
			testcaseTreeModel.removeTestcase(it.testcase)
		}
	}

	private val schedulerActivationStateHandler: EventHandler<SchedulerActivationStateEvent> = {
		if (it.scheduler === applicationContextHolder.scheduler) {
			if (it.scheduler.isActive) {
				selectionModel.clearSelection()
			}
			isEnabled = !it.scheduler.isActive
		}
	}

	private val nameChangeHandler: EventHandler<NameChangedEvent> = {
		if (this.graph != null && it.owner === this.graph) {
			testcaseTreeModel.updateGraphName()
		} else if (it.owner is Testcase) {
			testcaseTreeModel.updateTestcaseName(it.owner as Testcase)
		}
	}

	/** Holds the [DigitalGraph] whose [Testcases] are displayed by this [TestcaseTreeView]. */
	var graph: DigitalGraph? = null
		set(value) {
			if (field != value) {
				field = value
				model = TestcaseTreeModel(field, Graphics2DJvm.fromAwtFont(font))
			}
		}

	val selectedTestcase: Testcase?
		get() {
			val path = selectionPath ?: return null
			val selectedObj = (path.lastPathComponent as DefaultMutableTreeNode).userObject
			if (selectedObj is Testcase) {
				return selectedObj
			}
			return null
		}

	val runSelectedTestcaseAction: Action = RunTestcaseAction(application, applicationModeHolder)
	val runAllTestcasesAction: Action = RunCircuitTestcasesAction(application, applicationModeHolder)

	init {
		selectionModel.selectionMode = TreeSelectionModel.SINGLE_TREE_SELECTION
		addMouseListener(rightMouseListener)

		setCellRenderer(TestcaseTreeRenderer())
		setRowHeight(24)

		eventBus.register(TestcaseAddedEvent::class, testcaseAddedHandler)
		eventBus.register(TestcaseRemovedEvent::class, testcaseRemovedHandler)
		eventBus.register(SchedulerActivationStateEvent::class, schedulerActivationStateHandler)
		eventBus.register(NameChangedEvent::class, nameChangeHandler)

		graphPopupMenu.add(ActionWrapperSwing(AddTestcaseAction(application, applicationModeHolder)))
		graphPopupMenu.add(ActionWrapperSwing(runAllTestcasesAction))

		testcasePopupMenu.add(ActionWrapperSwing(DeleteTestcaseAction(application, applicationModeHolder)))
		testcasePopupMenu.add(ActionWrapperSwing(runSelectedTestcaseAction))
	}

	fun dispose() {
		eventBus.unregister(testcaseAddedHandler)
		eventBus.unregister(testcaseRemovedHandler)
		eventBus.unregister(schedulerActivationStateHandler)
		eventBus.unregister(nameChangeHandler)
	}

	private inner class RightMouseListener : MouseAdapter() {
		override fun mousePressed(e: MouseEvent?) {
			if (e?.button == MouseEvent.BUTTON3) {
				getPathForLocation(e.x, e.y)?.let { path ->
					requestFocusInWindow()
					selectionPath = path

					val menu = when ((path.lastPathComponent as DefaultMutableTreeNode).userObject) {
						is Graph -> graphPopupMenu
						is Testcase -> testcasePopupMenu
						else -> null
					}

					menu?.let {
						it.show(this@TestcaseTreeView, e.x, e.y)
					}
				}
			}
		}
	}

	private class TestcaseTreeModel(
		graph: DigitalGraph?,
		font: Font?
	) : DefaultTreeModel(graph?.let { NamableTreeNode(graph, font!!) } ?: DefaultMutableTreeNode()) {

		private val graphNode: DefaultMutableTreeNode get() = root as DefaultMutableTreeNode

		init {
			graph?.testcases?.testcases?.forEach { addTestcase(it) }
			nodeStructureChanged(root)
		}

		fun updateGraphName() {
			nodeChanged(graphNode)
		}

		fun updateTestcaseName(testcase: Testcase) {
			findTestcaseNode(testcase)?.let { nodeChanged(it) }
		}

		fun addTestcase(testcase: Testcase): TreeNode {
			val node = DefaultMutableTreeNode(testcase)
			graphNode.add(node)
			nodesWereInserted(graphNode, intArrayOf(graphNode.childCount - 1))
			return node
		}

		fun removeTestcase(testcase: Testcase) {
			val testcaseNode = findTestcaseNode(testcase)
			val index = getTestcaseIndex(testcase)
			if (index >= 0) {
				graphNode.remove(index)
				nodesWereRemoved(graphNode, intArrayOf(index), arrayOf(testcaseNode))
			}
		}

		private fun findTestcaseNode(testcase: Testcase): TreeNode? {
			for (e in graphNode.depthFirstEnumeration()) {
				if ((e as DefaultMutableTreeNode).userObject == testcase) {
					return e
				}
			}
			return null
		}

		private fun getTestcaseIndex(testcase: Testcase): Int {
			for (index in 0 until graphNode.childCount) {
				val item = (graphNode.getChildAt(index) as DefaultMutableTreeNode).userObject as Testcase
				if (item == testcase) {
					return index
				}
			}
			return -1
		}
	}

	private class TestcaseTreeRenderer : RichTextLabel() {

		companion object {
			private val testcaseIcon = UiUtil.themedIcon("/img/testcase.png")
		}

		override fun getTreeCellRendererComponent(tree: JTree?, value: Any?, sel: Boolean, expanded: Boolean, leaf: Boolean, row: Int, hasFocus: Boolean): Component {
			val component = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus) as RichTextLabel
			component.richText = null
			component.verticalAlignment = JLabel.CENTER

			when ((value as DefaultMutableTreeNode).userObject) {
				is Testcase -> {
					component.icon = testcaseIcon
					component.disabledIcon = testcaseIcon
				}
				is DigitalGraph -> {
					val icon = (value.userObject as Graph).let {
						MetaGraphIconProvider.provideIcon(it.type, false, StringUtils.isNotBlank(it.script))
					}
					component.richText = (value as NamableTreeNode).richTextName.value
					component.icon = icon
					component.disabledIcon = icon
				}
			}

			return component
		}
	}
}