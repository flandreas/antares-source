package ch.scorpion.jabbah.graph.ui.scenario

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.execution.scheduler.SchedulerActivationStateEvent
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.view.Scenario
import ch.scorpion.jabbah.graph.view.*
import javax.swing.JTree
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreeModel

/**
 * Displays the [Scenario] tree of a [Graph].
 */
class ScenarioTreeView(eventBus: EventBus) : JTree() {
    @Suppress("unused") constructor(): this(BaseModule.eventBus)

    init {

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
                model = createTreeModel(graphView!!)
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
                model = createTreeModel(graphView!!)
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
        return scenarioNode
    }

    private fun addScenarioStep(scenarioStep: ScenarioStep, scenarioNode: DefaultMutableTreeNode): DefaultMutableTreeNode {
        val scenarioStepNode = DefaultMutableTreeNode(scenarioStep)
        scenarioNode.add(scenarioStepNode)
        return scenarioNode
    }

    private fun findScenarioNode(scenario: Scenario): DefaultMutableTreeNode? {
        for (e in (model.root as DefaultMutableTreeNode).depthFirstEnumeration()) {
            if ((e as DefaultMutableTreeNode).userObject == scenario) {
                return e
            }
        }
        return null
    }
}