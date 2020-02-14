package ch.scorpion.jabbah.graph.ui.scenario

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.PropertySheetPanelFactory
import ch.scorpion.jabbah.edit.module.EditModuleJvm
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.Scenario
import ch.scorpion.jabbah.graph.view.ScenarioStep
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JSplitPane

/**
 * Allows to inspect and manipulate the [Scenario]s and [ScenarioStep]s of a [Graph]
 * by displaying a [ScenarioTreeView] and a [ScenarioPropertyPanel].
 *
 * Updates the [GraphView]'s [Scenario] and [ScenarioStep] whenever the user changes the
 * selection in the [ScenarioTreeView], and posts a [ScenarioSelectionEvent] on the provided
 * [EventBus].
 */
class ScenarioPanel(
    editor: Editor,
    private val eventBus: EventBus = BaseModule.eventBus,
    sheetFactory: PropertySheetPanelFactory = EditModuleJvm.propertySheetPanelFactory
) : JPanel() {

	private val splitPane = JSplitPane(JSplitPane.VERTICAL_SPLIT)

    private val treeView = ScenarioTreeView(eventBus)

    private val propertyPanel = ScenarioPropertyPanel(editor, sheetFactory, eventBus)

    var graphView: GraphView<*> = editor.drawing as GraphView<*>
        set(value) {
            field = value
            treeView.graphView = value
        }


    init {
        treeView.addTreeSelectionListener {
            val scenario = treeView.selectedScenario
            val scenarioStep = treeView.selectedScenarioStep
            graphView.currentScenario = scenario
            graphView.currentScenarioStep = scenarioStep
            eventBus.post(ScenarioSelectionEvent(graphView, scenario, scenarioStep))

        }
        treeView.preferredSize = Dimension(300, treeView.preferredSize.height)
        propertyPanel.preferredSize = Dimension(300, propertyPanel.preferredSize.height)

        buildUI()
    }

	fun dispose() {
		BaseModule.settings.set("scenarioPanel.splitPos", splitPane.dividerLocation)
	}

	fun clearSelection() {
		treeView.selectionModel.clearSelection()
	}

    private fun buildUI() {
        layout = BorderLayout()

        val treeViewScrollPane = JScrollPane(
            treeView,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER)

	    splitPane.border = null
	    splitPane.add(treeViewScrollPane)
	    splitPane.add(propertyPanel)
	    splitPane.dividerLocation = BaseModule.settings.getInt("scenarioPanel.splitPos", 400)

	    add(splitPane, BorderLayout.CENTER)
    }
}

data class ScenarioSelectionEvent(
    val graphView: GraphView<*>,
    val scenario: Scenario?,
    val scenarioStep: ScenarioStep?)