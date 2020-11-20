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
import ch.scorpion.jabbah.graph.ui.scenario.ScenarioViewController
import ch.scorpion.jabbah.graph.ui.scenario.ScenarioViewSwing
import ch.scorpion.jabbah.graph.ui.usecase.UsecaseViewController
import ch.scorpion.jabbah.graph.ui.usecase.UsecaseViewSwing
import ch.scorpion.jabbah.graph.view.GraphView
import java.awt.BorderLayout
import javax.swing.JPanel

/**
 * A [JPanel] for editing a root [GraphView].
 *
 * Consists of a [GraphNavigationViewSwing] at the left side and a [SidebarPane] at the right side that allows
 * to display a [ScenarioViewSwing] and a [UsecaseViewSwing].
 */
class GraphEditPanel(
	application: Application,
	editor: Editor,
	viewManager: ViewManager,
	propertySheetFactory: PropertySheetPanelFactory,
	eventBus: EventBus = BaseModule.eventBus
) : JPanel() {

	private val graphNavigationViewController = GraphNavigationViewController(
		isRoot = true,
		drawingView = editor.view as DrawingView<GraphView>,
		eventBus = eventBus)

	val graphNavigationView = GraphNavigationViewSwing(
		controller = graphNavigationViewController,
		drawingView = editor.view as DrawingView<GraphView>,
		viewManager = viewManager,
		contextBorderColor = null)

	private val scenarioViewController = ScenarioViewController(eventBus)
	private val scenarioView = ScenarioViewSwing(scenarioViewController, application, editor, eventBus, propertySheetFactory)

	private val usecaseViewController = UsecaseViewController(eventBus)
	private val usecasePanel = UsecaseViewSwing(usecaseViewController, application, editor, eventBus, propertySheetFactory)

	private val sidebarSplitPane = SidebarSplitPane(
		location = SidebarPane.Location.Right,
		mainContent = graphNavigationView,
		settingBaseName = "graphPanel.rightSidebar",
		contents = listOf(
			SidebarPaneContentImpl(
				Translations.getString("graph.scenarios.title"),
				UiUtil.themedIcon("/img/scenarios-16.png"),
				scenarioView),
			SidebarPaneContentImpl(
				Translations.getString("graph.usecases.title"),
				UiUtil.themedIcon("/img/usecase-16.png"),
				usecasePanel)
		)) {
		scenarioView.clearSelection()
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
		graphNavigationViewController.dispose()
		usecaseViewController.dispose()
	}

	fun setGraphView(newGraphView: GraphView, editable: Boolean, applyZoomStrategy: Boolean = true) {
		graphNavigationViewController.setRootGraphView(newGraphView, editable, applyZoomStrategy)
		scenarioViewController.graphView = newGraphView
		usecaseViewController.graphView = newGraphView
	}

	private fun buildUI() {
		layout = BorderLayout()
		add(sidebarSplitPane, BorderLayout.CENTER)
	}
}