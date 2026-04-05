package io.antarescircuit.jabbah.graph.ui.scenario

import io.antarescircuit.jabbah.base.Action
import io.antarescircuit.jabbah.base.StringUtils
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.help.HelpId
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.base.ui.HelpAction
import io.antarescircuit.jabbah.edit.module.EditModuleJvm
import io.antarescircuit.jabbah.edit.properties.PropertySheetPanelFactory
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.Scenario
import io.antarescircuit.jabbah.graph.view.ScenarioStep
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Frame
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JSplitPane

/**
 * A [javax.swing] implementation of a [ScenarioView] using a [ScenarioTreeView]
 * for displaying the [Scenario]s and [ScenarioStep]s of a [GraphView].
 */
class ScenarioViewSwing(
	private val controller: ScenarioViewController,
	private val eventBus: EventBus = BaseModule.eventBus,
	sheetFactory: PropertySheetPanelFactory = EditModuleJvm.propertySheetPanelFactory
) : JPanel(), ScenarioView {

	companion object {
		val HELP_ID = HelpId("scenariosView")
		val helpAction: Action = HelpAction.withSmallImage(HELP_ID)
	}

	private val splitPane = JSplitPane(JSplitPane.VERTICAL_SPLIT)

	private val treeView = ScenarioTreeView(controller)

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

		buildUI()
	}

	override fun dispose() {
		BaseModule.settings.set("scenarioPanel.splitPos", splitPane.dividerLocation)
		treeView.dispose()
		propertyPanel.dispose()
	}

	/** ---- [ScenarioView] */

	override fun getNewScenarioName(): String? {
		val name = JOptionPane.showInputDialog(
			Frame.getFrames()[0],
			Translations.getString("scenarios.action.addScenario.question"),
			Translations.getString("scenarios.action.addScenario.name"),
			JOptionPane.QUESTION_MESSAGE
		)
		if (StringUtils.isEmpty(name)) {
			return null
		}
		return name
	}

	override fun getNewScenarioStepName(): String? {
		val name = JOptionPane.showInputDialog(
			Frame.getFrames()[0],
			Translations.getString("scenarios.action.addScenarioStep.question"),
			Translations.getString("scenarios.action.addScenarioStep.name"),
			JOptionPane.QUESTION_MESSAGE
		)
		if (StringUtils.isEmpty(name)) {
			return null
		}
		return name
	}

	override fun confirmDeleteScenario(): Boolean =
		JOptionPane.showConfirmDialog(
			Frame.getFrames()[0],
			Translations.getString("scenarios.action.deleteScenario.question", controller.scenario!!.name.value),
			Translations.getString("scenarios.action.deleteScenario.name"),
			JOptionPane.YES_NO_OPTION,
			JOptionPane.QUESTION_MESSAGE) == JOptionPane.OK_OPTION

	override fun confirmDeleteScenarioStep(): Boolean =
		JOptionPane.showConfirmDialog(
			Frame.getFrames()[0],
			Translations.getString("scenarios.action.deleteScenarioStep.question", controller.scenarioStep!!.name, controller.scenario!!.name),
			Translations.getString("scenarios.action.deleteScenarioStep.name"),
			JOptionPane.YES_NO_OPTION,
			JOptionPane.QUESTION_MESSAGE) == JOptionPane.OK_OPTION

	/** ---- [ScenarioViewSwing] */

	fun clearSelection() {
		treeView.selectionModel.clearSelection()
	}

	private fun buildUI() {
		layout = BorderLayout()

		val treeViewScrollPane = JScrollPane(
			treeView,
			JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
			JScrollPane.HORIZONTAL_SCROLLBAR_NEVER)

		treeViewScrollPane.preferredSize = Dimension(300, treeView.preferredSize.height)
		propertyPanel.preferredSize = Dimension(300, propertyPanel.preferredSize.height)

		splitPane.border = null
		splitPane.add(treeViewScrollPane)
		splitPane.add(propertyPanel)
		splitPane.dividerLocation = BaseModule.settings.getInt("scenarioPanel.splitPos", 400)

		add(splitPane, BorderLayout.CENTER)
	}
}
