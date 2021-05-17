package ch.scorpion.jabbah.graph.ui.scenario

import ch.scorpion.jabbah.app.Application
import ch.scorpion.jabbah.base.ActionWrapperSwing
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.swing.JTreeUtil
import ch.scorpion.jabbah.base.swing.UiUtil
import ch.scorpion.jabbah.edit.model.text.description.NameChangedEvent
import ch.scorpion.jabbah.execution.scheduler.SchedulerActivationStateEvent
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.ui.ContainerLibraryElementIcon
import ch.scorpion.jabbah.graph.view.*
import ch.scorpion.jabbah.graph.view.app.ScenarioAppService
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import java.awt.Component
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import java.awt.datatransfer.UnsupportedFlavorException
import javax.swing.*
import javax.swing.tree.*

/**
 * Displays the [Scenario] and [ScenarioStep] tree of a [GraphView].
 *
 * Draws custom icons for the [TreeNode]s that indicate the type of custom object associated with the [TreeNode].
 * Installs a [JPopupMenu] on every [TreeNode] that allows the user to add new objects, or to remove existing ones.
 * Supports moving [ScenarioStep]s within the same [Scenario] using drag & drop.
 */
class ScenarioTreeView(
	private val application: Application,
	private val service: ScenarioAppService = GraphViewModule.scenarioAppService,
	eventBus: EventBus = BaseModule.eventBus
) : JTree() {

	companion object {
		private val LOG by logger(ScenarioTreeView::class)
	}

	/** The [JPopupMenu] to be displayed for the [GraphView] node.*/
	private val graphViewPopupMenu = JPopupMenu()

	/** The [JPopupMenu] to be displayed for a [Scenario] node.*/
	private val scenarioPopupMenu = JPopupMenu()

	/** The [JPopupMenu] to be displayed for a [ScenarioStep] node.*/
	private val scenarioStepPopupMenu = JPopupMenu()

	init {

		selectionModel.selectionMode = TreeSelectionModel.SINGLE_TREE_SELECTION
		selectionModel.addTreeSelectionListener { setupPopupMenu(it.newLeadSelectionPath) }

		setCellRenderer(ScenarioTreeRenderer())
		setRowHeight(24)

		dragEnabled = true
		dropMode = DropMode.INSERT
		transferHandler = ScenarioDndTransferHandler()

		// Adds a [Scenario] to this [ScenarioTreeView]
		eventBus.register(ScenarioAddedEvent::class) {
			if (it.graphView === this.graphView) {
				val scenarioNode = scenarioTreeModel.addScenario(it.scenario)
				selectionPath = JTreeUtil.getPath(scenarioNode)
			}
		}

		// Removes the [TreeNode] that represents a removed [Scenario]
		eventBus.register(ScenarioRemovedEvent::class) {
			if (it.graphView === this.graphView) {
				scenarioTreeModel.removeScenario(it.scenario)
			}
		}

		// Adds an added [ScenarioStep] to this [ScenarioTreeView]
		eventBus.register(ScenarioStepAddedEvent::class) {
			if (it.graphView === this.graphView) {
				val stepNode = scenarioTreeModel.addScenarioStep(it.scenario, it.scenarioStep)
				selectionPath = JTreeUtil.getPath(stepNode)
			}
		}

		// Removes the [TreeNode] that represents a removed [ScenarioStep]
		eventBus.register(ScenarioStepRemovedEvent::class) {
			if (it.graphView === this.graphView) {
				scenarioTreeModel.removeScenarioStep(it.scenario, it.scenarioStep)
			}
		}

		// Moves a [ScenarioStep] to another position within the same [Scenario]
		eventBus.register(ScenarioStepMovedEvent::class) {
			if (it.graphView === this.graphView) {
				val stepNode = scenarioTreeModel.moveScenarioStep(it.scenario, it.scenarioStep, it.index)
				selectionPath = JTreeUtil.getPath(stepNode)
			}
		}

		// Disables this [ScenarioTreeView] when the [Scheduler] is active.
		eventBus.register(SchedulerActivationStateEvent::class) {
			if (it.scheduler.isActive) {
				selectionModel.clearSelection()
			}
			isEnabled = !it.scheduler.isActive
		}

		eventBus.register(NameChangedEvent::class) {
			if (this.graphView != null && it.owner === this.graphView!!.graph) {
				scenarioTreeModel.updateGraphName()
			}
		}

		graphViewPopupMenu.add(ActionWrapperSwing(AddScenarioAction(application)))

		scenarioPopupMenu.add(ActionWrapperSwing(AddScenarioStepAction(application)))
		scenarioPopupMenu.add(ActionWrapperSwing(DeleteScenarioAction(application)))

		scenarioStepPopupMenu.add(ActionWrapperSwing(DeleteScenarioStepAction(application)))
	}

	/** Holds the [GraphView] whose [Graph] is the source of the [Scenario] tree.*/
	var graphView: GraphView? = null
		set(value) {
			if (field != value) {
				field = value
				// TODO How about null?
				model = ScenarioTreeModel(field!!)
			}
		}

	/**
	 * Returns the currently selected [Scenario] or the [Scenario] of the currently selected
	 * [ScenarioStep].
	 */
	val selectedScenario: Scenario?
		get() {
			val path = selectionPath ?: return null
			val selectedObj = (path.lastPathComponent as DefaultMutableTreeNode).userObject
			if (selectedObj is Scenario) {
				return selectedObj
			} else if (selectedObj is ScenarioStep) {
				return (path.getPathComponent(path.pathCount - 2) as DefaultMutableTreeNode).userObject as Scenario
			}
			return null
		}

	/** Returns the currently selected [ScenarioStep], if any.*/
	val selectedScenarioStep: ScenarioStep?
		get() {
			val path = selectionPath ?: return null
			val selectedObj = (path.lastPathComponent as DefaultMutableTreeNode).userObject
			if (selectedObj is ScenarioStep) {
				return selectedObj
			}
			return null
		}

	/** Casts the generic model property to [ScenarioTreeModel]. */
	private val scenarioTreeModel: ScenarioTreeModel get() = model as ScenarioTreeModel

	/** Setup the popup menu according to the currently selected [TreeNode]' user object.*/
	private fun setupPopupMenu(newSelectionPath: TreePath?) {
		if (newSelectionPath == null) {
			componentPopupMenu = null
			return
		}

		componentPopupMenu = when ((newSelectionPath.lastPathComponent as DefaultMutableTreeNode).userObject) {
			is GraphView -> graphViewPopupMenu
			is Scenario -> scenarioPopupMenu
			is ScenarioStep -> scenarioStepPopupMenu
			else -> null
		}
	}

	private class ScenarioTransferable(val node: DefaultMutableTreeNode) : Transferable {

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

	/** Implements drag & drop behaviour for [ScenarioTreeView].*/
	private inner class ScenarioDndTransferHandler : TransferHandler() {

		override fun getSourceActions(c: JComponent?): Int = COPY

		override fun canImport(support: TransferSupport?): Boolean {
			if (support == null || !support.isDataFlavorSupported(ScenarioTransferable.FLAVOR)) {
				return false
			}
			if (!support.isDrop) {
				return false
			}

			val scenarioStepNode = support.transferable.getTransferData(ScenarioTransferable.FLAVOR) as DefaultMutableTreeNode
			val dropLoc = support.dropLocation as JTree.DropLocation
			LOG.trace("ScenarioTreeView dropLoc: $dropLoc")

			// ScenarioStep can only be moved within its Scenario
			if (dropLoc.path == null || scenarioStepNode.parent != dropLoc.path.lastPathComponent || dropLoc.childIndex < 0) {
				return false
			}

			return true
		}

		override fun importData(support: TransferSupport?): Boolean {
			if (support == null || !support.isDataFlavorSupported(ScenarioTransferable.FLAVOR)) {
				return false
			}
			if (!support.isDrop) {
				return false
			}

			LOG.trace("importData")
			val scenarioStepNode = support.transferable.getTransferData(ScenarioTransferable.FLAVOR) as DefaultMutableTreeNode
			val dropLoc = support.dropLocation as JTree.DropLocation

			service.moveScenarioStep(
				application.controller,
				((dropLoc.path.lastPathComponent as DefaultMutableTreeNode).userObject as Scenario).id,
				(scenarioStepNode.userObject as ScenarioStep).id,
				dropLoc.childIndex
			)

			return true
		}

		override fun createTransferable(c: JComponent?): Transferable? {
			val tree = c as JTree
			val treeNode = tree.selectionPath.lastPathComponent as DefaultMutableTreeNode
			if (treeNode.userObject !is ScenarioStep) {
				return null
			}
			return ScenarioTransferable(treeNode)
		}
	}

	/** Extends [DefaultTreeModel] to add custom model manipulation methods. */
	private class ScenarioTreeModel(graphView: GraphView) : DefaultTreeModel(DefaultMutableTreeNode(graphView)) {

		private val graphViewNode: DefaultMutableTreeNode get() = root as DefaultMutableTreeNode

		init {
			graphView.scenarios.getScenarios().forEach { addScenario(it) }
			nodeStructureChanged(root)
		}

		fun updateGraphName() {
			nodeChanged(graphViewNode)
		}

		fun addScenario(scenario: Scenario): TreeNode {
			val scenarioNode = DefaultMutableTreeNode(scenario)
			graphViewNode.add(scenarioNode)
			scenario.getScenarioSteps().forEach { addScenarioStep(it, scenarioNode) }
			nodesWereInserted(graphViewNode, intArrayOf(graphViewNode.childCount - 1))
			return scenarioNode
		}

		fun addScenarioStep(scenario: Scenario, step: ScenarioStep): TreeNode {
			return addScenarioStep(step, findScenarioNode(scenario)!!)
		}

		fun addScenarioStep(step: ScenarioStep, scenarioNode: DefaultMutableTreeNode): TreeNode {
			val newNode = DefaultMutableTreeNode(step)
			scenarioNode.add(newNode)
			nodesWereInserted(scenarioNode, intArrayOf(scenarioNode.childCount - 1))
			return newNode
		}

		fun removeScenario(scenario: Scenario) {
			val scenarioNode = findScenarioNode(scenario)
			val index = getScenarioIndex(scenario)
			if (index >= 0) {
				graphViewNode.remove(index)
				nodesWereRemoved(graphViewNode, intArrayOf(index), arrayOf(scenarioNode))
			}
		}

		fun removeScenarioStep(scenario: Scenario, scenarioStep: ScenarioStep) {
			val scenarioNode = findScenarioNode(scenario)
			val index = getScenarioStepIndex(scenarioNode!!, scenarioStep)
			if (index >= 0) {
				scenarioNode.remove(index)
				nodeStructureChanged(scenarioNode)
			}
		}

		fun moveScenarioStep(scenario: Scenario, scenarioStep: ScenarioStep, index: Int): TreeNode {
			val scenarioNode = findScenarioNode(scenario)
			val scenarioStepNode = findScenarioStepNode(scenarioStep)
			val oldIndex = getScenarioStepIndex(scenarioNode!!, scenarioStep)
			if (index >= 0) {
				scenarioNode.remove(oldIndex)
				scenarioNode.insert(scenarioStepNode, index)
				nodeStructureChanged(scenarioNode)
			}
			return scenarioStepNode!!
		}

		private fun findScenarioNode(scenario: Scenario): DefaultMutableTreeNode? {
			for (e in graphViewNode.depthFirstEnumeration()) {
				if ((e as DefaultMutableTreeNode).userObject == scenario) {
					return e
				}
			}
			return null
		}

		private fun findScenarioStepNode(scenarioStep: ScenarioStep): DefaultMutableTreeNode? {
			for (e in graphViewNode.depthFirstEnumeration()) {
				if ((e as DefaultMutableTreeNode).userObject == scenarioStep) {
					return e
				}
			}
			return null
		}

		private fun getScenarioIndex(scenario: Scenario): Int {
			for (index in 0 until graphViewNode.childCount) {
				val item = (graphViewNode.getChildAt(index) as DefaultMutableTreeNode).userObject as Scenario
				if (item == scenario) {
					return index
				}
			}
			return -1
		}

		private fun getScenarioStepIndex(scenarioNode: DefaultMutableTreeNode, scenarioStep: ScenarioStep): Int {
			for (index in 0 until scenarioNode.childCount) {
				val item = (scenarioNode.getChildAt(index) as DefaultMutableTreeNode).userObject as ScenarioStep
				if (item == scenarioStep) {
					return index
				}
			}
			return -1
		}

	}

	/** Adds custom icons to the tree nodes.*/
	private class ScenarioTreeRenderer : DefaultTreeCellRenderer() {

		companion object {
			private val elementIcon = ContainerLibraryElementIcon()
			private val scenarioIcon = UiUtil.themedIcon("/img/scenario-20.png")
			private val stepIcon = UiUtil.themedIcon("/img/step-20.png")
		}

		override fun getTreeCellRendererComponent(tree: JTree?, value: Any?, sel: Boolean, expanded: Boolean, leaf: Boolean, row: Int, hasFocus: Boolean): Component {
			val component = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus) as JLabel

			val userObject = (value as DefaultMutableTreeNode).userObject
			when (userObject) {
				is Scenario -> {
					component.icon = scenarioIcon
					component.disabledIcon = scenarioIcon
				}
				is ScenarioStep -> {
					component.icon = stepIcon
					component.disabledIcon = stepIcon
				}
				is GraphView -> {
					component.icon = elementIcon
					component.disabledIcon = elementIcon
				}
			}

			return component
		}
	}
}