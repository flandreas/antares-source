package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.app.Application
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.swing.SidebarPane
import ch.scorpion.jabbah.base.swing.SidebarPaneContentImpl
import ch.scorpion.jabbah.base.swing.SidebarSplitPane
import ch.scorpion.jabbah.base.swing.UiUtil
import ch.scorpion.jabbah.draw.view.ViewManager
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.properties.PropertySheetPanelFactory
import ch.scorpion.jabbah.graph.ui.scenario.ScenarioPanel
import ch.scorpion.jabbah.graph.ui.usecase.UsecasePanel
import ch.scorpion.jabbah.graph.view.GraphView
import java.awt.BorderLayout
import javax.swing.JPanel

/**
 * A [JPanel] for editing a root [GraphView].
 *
 * Consists of a [GraphNavigationPanel] at the left side and a [SidebarPane] at the right side that allows
 * to display a [ScenarioPanel] and a [UsecasePanel].
 */
class GraphEditPanel(
	application: Application,
	editor: Editor,
	viewManager: ViewManager,
	propertySheetFactory: PropertySheetPanelFactory,
	eventBus: EventBus = BaseModule.eventBus
) : JPanel() {

	private val graphNavigationController = GraphNavigationViewController(
		isRoot = true,
		drawingView = editor.view as DrawingView<GraphView>,
		eventBus = eventBus)

	val graphNavigationPanel = GraphNavigationPanel(
		controller = graphNavigationController,
		drawingView = editor.view as DrawingView<GraphView>,
		viewManager = viewManager,
		contextBorderColor = null)

	private val scenarioPanel = ScenarioPanel(application, editor, eventBus, propertySheetFactory)

	private val usecasePanel = UsecasePanel(application, editor, eventBus, propertySheetFactory)

	private val sidebarSplitPane = SidebarSplitPane(
		location = SidebarPane.Location.Right,
		mainContent = graphNavigationPanel,
		settingBaseName = "graphPanel.rightSidebar",
		contents = listOf(
			SidebarPaneContentImpl(
				Translations.getString("graph.scenarios.title"),
				UiUtil.themedIcon("/img/scenarios-16.png"),
				scenarioPanel),
			SidebarPaneContentImpl(
				Translations.getString("graph.usecases.title"),
				UiUtil.themedIcon("/img/usecase-16.png"),
				usecasePanel)
		)) {
		scenarioPanel.clearSelection()
		usecasePanel.clearSelection()
		revalidate()
		repaint()
	}

	init {
		buildUI()

		setGraphView(editor.drawing as GraphView, true)
	}

	fun dispose() {
		sidebarSplitPane.dispose()
		scenarioPanel.dispose()
		usecasePanel.dispose()
	}

	fun setGraphView(newGraphView: GraphView, editable: Boolean, applyZoomStrategy: Boolean = true) {
		graphNavigationController.setRootGraphView(newGraphView, editable, applyZoomStrategy)
		scenarioPanel.graphView = newGraphView
		usecasePanel.graphView = newGraphView
	}

	private fun buildUI() {
		layout = BorderLayout()
		add(sidebarSplitPane, BorderLayout.CENTER)
	}
}