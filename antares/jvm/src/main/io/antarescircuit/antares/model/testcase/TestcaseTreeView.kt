package io.antarescircuit.antares.model.testcase

import io.antarescircuit.antares.model.DigitalGraph
import io.antarescircuit.antares.model.testcase.TestcaseTreeView.TestcaseTransferable.Companion.FLAVOR
import io.antarescircuit.jabbah.base.ActionWrapperSwing
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.event.EventHandler
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.base.swing.JTreeUtil
import io.antarescircuit.jabbah.base.ui.UIBasics.PROP_TREE_SHOW_ROOT_HANDLES
import io.antarescircuit.jabbah.draw.graphics.Graphics2DJvm
import io.antarescircuit.jabbah.edit.model.text.description.NameChangedEvent
import io.antarescircuit.jabbah.execution.scheduler.SchedulerActivationStateEvent
import io.antarescircuit.jabbah.graph.model.Graph
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import java.awt.datatransfer.UnsupportedFlavorException
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.TreeSelectionModel

/**
 * Displays the tree of [Testcase]s of a [DigitalGraph].
 * TODO Similar to UsecaseTreeView (and ScenarioTreeView): Extract commonalities?
 */
class TestcaseTreeView(
	val controller: TestcaseViewController,
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

	private val testcaseMovedHandler: EventHandler<TestcaseMovedEvent> = {
		if (it.graph === this.graph) {
			testcaseTreeModel.moveTestcase(it.testcase, it.index)
		}
	}

	private val schedulerActivationStateHandler: EventHandler<SchedulerActivationStateEvent> = {
		if (it.scheduler === controller.applicationContextHolder.scheduler) {
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

	init {
		setShowsRootHandles(BaseModule.properties.getBoolean(PROP_TREE_SHOW_ROOT_HANDLES))
		selectionModel.selectionMode = TreeSelectionModel.SINGLE_TREE_SELECTION
		addMouseListener(rightMouseListener)

		setCellRenderer(TestcaseTreeRenderer())
		setRowHeight(24)

		dragEnabled = true
		dropMode = DropMode.INSERT
		transferHandler = TestcaseDndTransferHandler()

		eventBus.register(TestcaseAddedEvent::class, testcaseAddedHandler)
		eventBus.register(TestcaseRemovedEvent::class, testcaseRemovedHandler)
		eventBus.register(TestcaseMovedEvent::class, testcaseMovedHandler)
		eventBus.register(SchedulerActivationStateEvent::class, schedulerActivationStateHandler)
		eventBus.register(NameChangedEvent::class, nameChangeHandler)

		graphPopupMenu.add(ActionWrapperSwing(controller.addAction))
		graphPopupMenu.add(ActionWrapperSwing(controller.runAllTestcasesAction))

		testcasePopupMenu.add(ActionWrapperSwing(controller.deleteAction))
		testcasePopupMenu.add(ActionWrapperSwing(controller.runSelectedTestcaseAction))
		testcasePopupMenu.add(ActionWrapperSwing(controller.duplicateAction))
	}

	fun dispose() {
		eventBus.unregister(testcaseAddedHandler)
		eventBus.unregister(testcaseRemovedHandler)
		eventBus.unregister(testcaseMovedHandler)
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

	private class TestcaseTransferable(val node: DefaultMutableTreeNode) : Transferable {

		companion object {
			val FLAVOR = DataFlavor("${DataFlavor.javaJVMLocalObjectMimeType};class=\"${String::class.java.name}\"")
		}

		override fun getTransferData(flavor: DataFlavor?): Any {
			if (flavor != FLAVOR) {
				throw UnsupportedFlavorException(flavor)
			}
			return node
		}

		override fun isDataFlavorSupported(flavor: DataFlavor?): Boolean = flavor == FLAVOR

		override fun getTransferDataFlavors(): Array<DataFlavor> = arrayOf(FLAVOR)
	}

	private inner class TestcaseDndTransferHandler : TransferHandler() {

		override fun getSourceActions(c: JComponent?): Int = MOVE

		override fun createTransferable(c: JComponent?): Transferable? {
			val tree = c as JTree
			val treeNode = tree.selectionPath.lastPathComponent as DefaultMutableTreeNode

			return if (treeNode.userObject is Testcase) {
				TestcaseTransferable(treeNode)
			} else {
				null
			}
		}

		override fun canImport(support: TransferSupport?): Boolean {
			if (support == null || !support.isDataFlavorSupported(FLAVOR)) {
				return false
			}
			if (!support.isDrop) {
				return false
			}

			val node = support.transferable.getTransferData(FLAVOR) as DefaultMutableTreeNode
			val dropLoc = support.dropLocation as JTree.DropLocation

			if (node.userObject is Testcase) {
				if (dropLoc.path == null || dropLoc.childIndex < 0) {
					return false
				}
			}
			return true
		}

		override fun importData(support: TransferSupport?): Boolean {
			if (support == null || !support.isDataFlavorSupported(FLAVOR)) {
				return false
			}
			if (!support.isDrop) {
				return false
			}

			val node = support.transferable.getTransferData(FLAVOR) as DefaultMutableTreeNode
			val dropLoc = support.dropLocation as JTree.DropLocation

			if (node.userObject is Testcase) {
				controller.service.moveTestcase(
					controller.applicationDataHolder,
					(node.userObject as Testcase).id,
					dropLoc.childIndex
				)
				return true
			}

			return false
		}
	}
}