package ch.scorpion.jabbah.graph.ui.scenario

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.swing.JTreeUtil
import ch.scorpion.jabbah.execution.scheduler.SchedulerActivationStateEvent
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.ui.ContainerLibraryElementIcon
import ch.scorpion.jabbah.graph.view.Scenario
import ch.scorpion.jabbah.graph.view.*
import java.awt.Component
import javax.swing.ImageIcon
import javax.swing.JLabel
import javax.swing.JPopupMenu
import javax.swing.JTree
import javax.swing.tree.*

/**
 * Displays the [Scenario] tree of a [GraphView].
 */
class ScenarioTreeView(eventBus: EventBus) : JTree() {
    @Suppress("unused") constructor(): this(BaseModule.eventBus)

    private val graphViewPopupMenu = JPopupMenu()

    private val scenarioPopupMenu = JPopupMenu()

    private val scenarioStepPopupMenu = JPopupMenu()

    init {

        selectionModel.selectionMode = TreeSelectionModel.SINGLE_TREE_SELECTION
        selectionModel.addTreeSelectionListener { setupPopupMenu(it.newLeadSelectionPath) }

        setCellRenderer(ScenarioTreeRenderer())
        setRowHeight(24)

        // Adds a [Scenario] to this [ScenarioTreeView]
        eventBus.register(ScenarioAddedEvent::class, {
            if (it.graphView === this.graphView) {
                val scenarioNode = scenarioTreeModel.addScenario(it.scenario)
                selectionPath = JTreeUtil.getPath(scenarioNode)
            }
        })

        // Removes the [TreeNode] that represents a removed [Scenario]
        eventBus.register(ScenarioRemovedEvent::class, {
            if (it.graphView === this.graphView) {
                scenarioTreeModel.removeScenario(it.scenario)
            }
        })

        // Adds an added [ScenarioStep] to this [ScenarioTreeView]
        eventBus.register(ScenarioStepAddedEvent::class, {
            if (it.graphView === this.graphView) {
                val stepNode = scenarioTreeModel.addScenarioStep(it.scenario, it.scenarioStep)
                selectionPath = JTreeUtil.getPath(stepNode)
            }
        })

        // Removes the [TreeNode] that represents a removed [ScenarioStep]
        eventBus.register(ScenarioStepRemovedEvent::class, {
            if (it.graphView === this.graphView) {
                scenarioTreeModel.removeScenarioStep(it.scenario, it.scenarioStep)
            }
        })

        // Disables this [ScenarioTreeView] when the [Scheduler] is active.
        eventBus.register(SchedulerActivationStateEvent::class, {
            if (it.scheduler.isActive) {
                selectionModel.clearSelection()
            }
            isEnabled = !it.scheduler.isActive
        })

        graphViewPopupMenu.add(AddScenarioAction())

        scenarioPopupMenu.add(AddScenarioStepAction())
        scenarioPopupMenu.add(DeleteScenarioAction())

        scenarioStepPopupMenu.add(DeleteScenarioStepAction())
    }

    /** Holds the [GraphView] whose [Graph] is the source of the [Scenario] tree.*/
    var graphView: GraphView<*>? = null
        set(value) {
            if (field != value) {
                field = value
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
    private val scenarioTreeModel: ScenarioTreeModel get() = model!! as ScenarioTreeModel

    /** Setup the popup menu according to the currently selected [TreeNode]' user object.*/
    private fun setupPopupMenu(newSelectionPath: TreePath?) {
        if (newSelectionPath == null) {
            componentPopupMenu = null
            return
        }

        componentPopupMenu = when ((newSelectionPath.lastPathComponent as DefaultMutableTreeNode).userObject) {
            is GraphView<*> -> graphViewPopupMenu
            is Scenario -> scenarioPopupMenu
            is ScenarioStep -> scenarioStepPopupMenu
            else -> null
        }
    }

    /** Extends [DefaultTreeModel] to add custom model manipulation methods. */
    private class ScenarioTreeModel(graphView: GraphView<*>) : DefaultTreeModel(DefaultMutableTreeNode(graphView)) {

        private val graphViewNode: DefaultMutableTreeNode get() = root as DefaultMutableTreeNode

        init {
            graphView.scenarios.getScenarios().forEach { addScenario(it) }
            nodeStructureChanged(root)
        }

        fun addScenario(scenario:Scenario): TreeNode {
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
            val index = getScenarioIndex(scenario)
            if (index >= 0) {
                val child = graphViewNode.remove(index)
                nodesWereRemoved(graphViewNode, intArrayOf(index), arrayOf(child))
            }
        }

        fun removeScenarioStep(scenario: Scenario, scenarioStep: ScenarioStep) {
            val scenarioNode = findScenarioNode(scenario)
            val index = getScenarioStepIndex(scenarioNode!!, scenarioStep)
            if (index >= 0) {
                val child = scenarioNode.remove(index)
                nodesWereRemoved(scenarioNode, intArrayOf(index), arrayOf(child))
            }
        }

        private fun findScenarioNode(scenario: Scenario): DefaultMutableTreeNode? {
            for (e in graphViewNode.depthFirstEnumeration()) {
                if ((e as DefaultMutableTreeNode).userObject == scenario) {
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
            private val scenarioIcon = ImageIcon(ScenarioTreeView::class.java.getResource("/img/scenario-20.png"))
            private val stepIcon = ImageIcon(ScenarioTreeView::class.java.getResource("/img/step-20.png"))
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
                is GraphView<*> -> {
                    component.icon = elementIcon
                    component.disabledIcon = elementIcon
                }
            }

            return component
        }
    }
}