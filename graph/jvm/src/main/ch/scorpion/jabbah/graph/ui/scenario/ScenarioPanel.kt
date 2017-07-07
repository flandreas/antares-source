package ch.scorpion.jabbah.graph.ui.scenario

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.PropertySheetPanelFactory
import ch.scorpion.jabbah.edit.module.EditModuleJvm
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.Scenario
import ch.scorpion.jabbah.graph.view.ScenarioStep
import java.awt.BorderLayout
import javax.swing.JPanel
import javax.swing.JScrollPane

/**
 * Allows to inspect and manipulate the [Scenario]s and [ScenarioStep]s of a [Graph]
 * by displaying a [ScenarioTreeView] and a [PropertyPanel].
 *
 * Updates the [GraphView]'s [Scenario] and [ScenarioStep] whenever the user changes the
 * selection in the [ScenarioTreeView], and posts a [ScenarioSelectionEvent] on the provided
 * [EventBus].
 */
class ScenarioPanel(
    editor: Editor,
    private val eventBus: EventBus,
    sheetFactory: PropertySheetPanelFactory
) : JPanel() {

    constructor(editor: Editor): this(editor, BaseModule.eventBus, EditModuleJvm.propertySheetPanelFactory)

    private val treeView = ScenarioTreeView(eventBus)
    private val propertyPanel = ScenarioPropertyPanel(editor, sheetFactory, eventBus)

    var graphView: GraphView<*>? = null
        set(value) {
            field = value
            treeView.graphView = value
        }


    init {
        treeView.addTreeSelectionListener {
            val scenario = treeView.selectedScenario
            val scenarioStep = treeView.selectedSenarioStep
            graphView?.currentScenario = scenario
            graphView?.currentScenarioStep = scenarioStep
            eventBus.post(ScenarioSelectionEvent(graphView!!, scenario, scenarioStep))

        }
        buildUI()
    }

    private fun buildUI() {
        layout = BorderLayout()
        val treeViewScrollPane = JScrollPane(
            treeView,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER)
        add(treeViewScrollPane, BorderLayout.CENTER)
        add(propertyPanel, BorderLayout.SOUTH)
    }
}

data class ScenarioSelectionEvent(
    val graphView: GraphView<*>,
    val scenario: Scenario?,
    val scenarioStep: ScenarioStep?)