package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.swing.SidebarPane
import ch.scorpion.jabbah.base.swing.SidebarSplitPane
import ch.scorpion.jabbah.draw.view.ViewManager
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.PropertySheetPanelFactory
import ch.scorpion.jabbah.execution.scheduler.Scheduler
import ch.scorpion.jabbah.graph.ui.scenario.ScenarioPanel
import ch.scorpion.jabbah.graph.view.GraphElementView
import ch.scorpion.jabbah.graph.view.GraphView
import java.awt.BorderLayout
import javax.swing.BorderFactory
import javax.swing.JLabel
import javax.swing.JPanel

/**
 * A [JPanel] for editing a root [GraphView].
 *
 * Consists of a [GraphNavigationPanel] at the left side and a [SidebarPane] at the right side that allows
 * to display a [ScenarioPanel] and (in the future) a use cases panel.
 */
class GraphEditPanel(
	editor: Editor,
	scheduler: Scheduler,
	viewManager: ViewManager,
	graphNavigationPanelFactory: GraphNavigationPanelFactory,
	propertySheetFactory: PropertySheetPanelFactory,
	closeHandler: (GraphNavigationPanel) -> Unit,
	eventBus: EventBus = BaseModule.eventBus
) : JPanel() {


	val graphNavigationPanel = graphNavigationPanelFactory.create(
		isRoot = true,
		drawingView = editor.view as DrawingView<GraphView<GraphElementView<*>>>,
		viewManager = viewManager,
		closeHandler = closeHandler,
		contextColor = null,
		scheduler = scheduler)

	private val scenarioPanel = ScenarioPanel(editor, eventBus, propertySheetFactory)

	private val usecasesDummy = JLabel(Translations.getString("application.notYetImplemented.text"))

	private val sidebarSplitPane = SidebarSplitPane(
		location = SidebarPane.Location.Right,
		mainContent = graphNavigationPanel,
		settingBaseName = "graphPanel.rightSidebar",
		contents = listOf(
			SidebarPane.Content(Translations.getString("graph.scenarios.title"), "/img/scenarios-16.png", scenarioPanel),
			SidebarPane.Content(Translations.getString("graph.usecases.title"), "/img/usecase-16.png", usecasesDummy)
		)) {
			scenarioPanel.clearSelection()
			revalidate()
			repaint()
		}

	init {
		buildUI()
	}

	fun dispose() {
		sidebarSplitPane.dispose()
	}

	fun setGraphView(newGraphView: GraphView<GraphElementView<*>>) {
		graphNavigationPanel.setRootGraphView(newGraphView)
		scenarioPanel.graphView = newGraphView
	}

	private fun buildUI() {
		layout = BorderLayout()

		usecasesDummy.border = BorderFactory.createEmptyBorder(5, 5, 5, 5)
		usecasesDummy.verticalAlignment = JLabel.TOP

		add(sidebarSplitPane, BorderLayout.CENTER)
	}
}