package ch.scorpion.jabbah.graph.ui.scenario

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.execution.scheduler.SchedulerActivationStateEvent
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.ui.ContainerLibraryElementIcon
import ch.scorpion.jabbah.graph.view.Scenario
import ch.scorpion.jabbah.graph.view.*
import java.awt.Component
import javax.swing.ImageIcon
import javax.swing.JLabel
import javax.swing.JTree
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeCellRenderer
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreeModel

/**
 * Displays the [Scenario] tree of a [Graph].
 */
class ScenarioTreeView(eventBus: EventBus) : JTree() {
    @Suppress("unused") constructor(): this(BaseModule.eventBus)

    init {

        setCellRenderer(Renderer())
        setRowHeight(24)

        // [Scenario] to this [ScenarioTreeView]
        eventBus.register(ScenarioAddedEvent::class, {
            if (it.graphView === this.graphView) {
                addScenario(it.scenario, model.root as DefaultMutableTreeNode)
                (model as DefaultTreeModel).reload()
            }
        })

        // Removes the [TreeNode] that represents a removed [Scenario]
        eventBus.register(ScenarioRemovedEvent::class, {
            if (it.graphView === this.graphView) {
                removeScenario(it.scenario)
            }
        })

        // Adds an added [ScenarioStep] to this [ScenarioTreeView]
        eventBus.register(ScenarioStepAddedEvent::class, {
            if (it.graphView === this.graphView) {
                val scenarioNode = findScenarioNode(it.scenario)
                addScenarioStep(it.scenarioStep, scenarioNode!!)
                (model as DefaultTreeModel).reload()
            }
        })

        // Removes the [TreeNode] that represents a removed [ScenarioStep]
        eventBus.register(ScenarioStepRemovedEvent::class, {
            if (it.graphView === this.graphView) {
                removeScenarioStep(it.scenario, it.scenarioStep)
            }
        })

        eventBus.register(SchedulerActivationStateEvent::class, {
            if (it.scheduler.isActive) {
                selectionModel.clearSelection()
                isEnabled = false
            } else {
                isEnabled = true
            }
        })
    }

    /** Holds the [GraphView] whose [Graph] is the source of the [Scenario] tree.*/
    var graphView: GraphView<*>? = null
        set(value) {
            if (field != value) {
                field = value
                model = createScenarioTreeModel()
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

    private fun createScenarioTreeModel(): TreeModel {
        val rootNode = DefaultMutableTreeNode(graphView)
        for (scenario in graphView!!.scenarios.getScenarios()) {
            val scenarioNode = addScenario(scenario, rootNode)
            for (step in scenario.getScenarioSteps()) {
                addScenarioStep(step, scenarioNode)
            }
        }
        return DefaultTreeModel(rootNode)
    }

    private fun addScenario(scenario:Scenario, rootNode: DefaultMutableTreeNode): DefaultMutableTreeNode {
        val scenarioNode = DefaultMutableTreeNode(scenario)
        rootNode.add(scenarioNode)
        for (step in scenario.getScenarioSteps()) {
            addScenarioStep(step, scenarioNode)
        }
        return scenarioNode
    }

    private fun addScenarioStep(scenarioStep: ScenarioStep, scenarioNode: DefaultMutableTreeNode): DefaultMutableTreeNode {
        val scenarioStepNode = DefaultMutableTreeNode(scenarioStep)
        scenarioNode.add(scenarioStepNode)
        return scenarioNode
    }

    private fun removeScenario(scenario: Scenario) {
        val rootNode = model!!.root as DefaultMutableTreeNode
        val index = getScenarioIndex(scenario)
        if (index >= 0) {
            val child = rootNode.remove(index)
            (model as DefaultTreeModel).nodesWereRemoved(rootNode, intArrayOf(index), arrayOf(child))
        }
    }

    private fun removeScenarioStep(scenario: Scenario, scenarioStep: ScenarioStep) {
        val scenarioNode = findScenarioNode(scenario)
        val index = getScenarioStepIndex(scenarioNode!!, scenarioStep)
        if (index >= 0) {
            val child = scenarioNode.remove(index)
            (model as DefaultTreeModel).nodesWereRemoved(scenarioNode, intArrayOf(index), arrayOf(child))
        }
    }

    private fun findScenarioNode(scenario: Scenario): DefaultMutableTreeNode? {
        for (e in (model.root as DefaultMutableTreeNode).depthFirstEnumeration()) {
            if ((e as DefaultMutableTreeNode).userObject == scenario) {
                return e
            }
        }
        return null
    }

    private fun getScenarioIndex(scenario: Scenario): Int {
        val rootNode = model!!.root as DefaultMutableTreeNode
        for (index in 0 until rootNode.childCount) {
            val item = (rootNode.getChildAt(index) as DefaultMutableTreeNode).userObject as Scenario
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

    private class Renderer : DefaultTreeCellRenderer() {

        companion object {
            private val elementIcon = ContainerLibraryElementIcon()
            private val scenarioIcon = ImageIcon(ScenarioTreeView::class.java.getResource("/img/scenario-20.png"))
            private val stepIcon = ImageIcon(ScenarioTreeView::class.java.getResource("/img/step-20.png"))
        }

        override fun getTreeCellRendererComponent(tree: JTree?, value: Any?, sel: Boolean, expanded: Boolean, leaf: Boolean, row: Int, hasFocus: Boolean): Component {
            val component = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus) as JLabel

            val userObject = (value as DefaultMutableTreeNode).userObject
            if (userObject is Scenario) {
                component.icon = scenarioIcon
                component.disabledIcon = scenarioIcon
            } else if (userObject is ScenarioStep) {
                component.icon = stepIcon
                component.disabledIcon = stepIcon
            } else if (userObject is GraphView<*>) {
                component.icon = elementIcon
                component.disabledIcon = elementIcon
            }

            return component
        }
    }
}