package ch.scorpion.jabbah.graph.ui.scenario

import ch.scorpion.jabbah.app.Application
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.module.EditModuleJvm
import ch.scorpion.jabbah.edit.properties.PropertySheetPanelFactory
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.Scenario
import ch.scorpion.jabbah.graph.view.ScenarioStep
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JSplitPane

/**
 * A [javax.swing] implementation of a [ScenarioView] using a [ScenarioTreeView]
 * for displaying the [Scenario]s and [ScenarioStep]s of a [GraphView].
 */
class ScenarioViewSwing(
	controller: ScenarioViewController,
	application: Application,
	private val eventBus: EventBus = BaseModule.eventBus,
	sheetFactory: PropertySheetPanelFactory = EditModuleJvm.propertySheetPanelFactory
) : JPanel(), ScenarioView {

	private val splitPane = JSplitPane(JSplitPane.VERTICAL_SPLIT)

	private val treeView = ScenarioTreeView(application, controller.applicationContextHolder, controller.applicationModeHolder)

	private val propertyPanel = ScenarioPropertyPanelSwing(controller.propertyPanelController, sheetFactory)

	override var graphView: GraphView? = null
		set(value) {
			field = value
			treeView.graphView = value
		}

	init {
		controller.view = this

		treeView.addTreeSelectionListener {
			eventBus.post(ScenarioSelectionEvent(
				graphView!!,
				treeView.selectedScenario,
				treeView.selectedScenarioStep))

		}
		treeView.preferredSize = Dimension(300, treeView.preferredSize.height)
		propertyPanel.preferredSize = Dimension(300, propertyPanel.preferredSize.height)

		buildUI()
	}

	override fun dispose() {
		BaseModule.settings.set("scenarioPanel.splitPos", splitPane.dividerLocation)
		treeView.dispose()
		propertyPanel.dispose()
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
