package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.swing.*
import ch.scorpion.jabbah.draw.view.ContentViewManager
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.properties.PropertySheetPanelFactory
import ch.scorpion.jabbah.graph.ui.desktop.GraphDesktopViewItem
import ch.scorpion.jabbah.graph.ui.scenario.ScenarioViewSwing
import ch.scorpion.jabbah.graph.ui.usecase.UsecaseViewSwing
import ch.scorpion.jabbah.graph.view.GraphView
import java.awt.BorderLayout
import javax.swing.JPanel

/**
 * A [javax.swing] implementation of [GraphEditView] using a [SidebarPane] for displaying
 * a [ScenarioViewSwing] and a [UsecaseViewSwing] to the right side of the view.
 */
class GraphEditViewSwing(
	controller: GraphEditViewController,
	viewManager: ContentViewManager,
	propertySheetFactory: PropertySheetPanelFactory,
	eventBus: EventBus = BaseModule.eventBus,
	override val graphNavigationView: GraphNavigationView = GraphNavigationViewSwing(
		controller = controller.graphNavigationViewController,
		drawingView = controller.editor.view as DrawingView<GraphView>,
		viewManager = viewManager,
		reusable = true,
		contextBorderColor = null)
) : JPanel(), GraphEditView, GraphDesktopViewItem by graphNavigationView {

	private val scenarioView = ScenarioViewSwing(
		controller.scenarioViewController,
		eventBus,
		propertySheetFactory)

	private val usecaseView = UsecaseViewSwing(
		controller.usecaseViewController,
		eventBus,
		propertySheetFactory)

	private val sidebarSplitPane = SidebarSplitPane(
		location = SidebarPane.Location.Right,
		mainContent = graphNavigationView as GraphNavigationViewSwing,
		settingBaseName = "graphPanel.rightSidebar",
		contents = listOf(
			SidebarPaneContentImpl(
				Translations.getString("graph.scenarios.title"),
				UiUtil.themedIcon("/img/scenarios-16.png"),
				scenarioView,
				listOf(controller.scenarioViewController.metaAddAction, ScenarioViewSwing.helpAction)),
			SidebarPaneContentImpl(
				Translations.getString("graph.usecases.title"),
				UiUtil.themedIcon("/img/usecase-16.png"),
				usecaseView,
				listOf(controller.usecaseViewController.metaAddAction, UsecaseViewSwing.helpAction))
		)) {
		scenarioView.clearSelection()
		usecaseView.clearSelection()
		revalidate()
		repaint()
	}

	init {
		controller.view = this
		buildUI()
	}

	override fun dispose() {
		sidebarSplitPane.dispose()
	}

	private fun buildUI() {
		layout = BorderLayout()
		add(sidebarSplitPane, BorderLayout.CENTER)
	}

	fun add(content: SidebarPaneContent) {
		sidebarSplitPane.add(content)
		// TODO Call clearSelection() for added content
	}
}