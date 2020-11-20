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
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.properties.PropertySheetPanelFactory
import ch.scorpion.jabbah.graph.ui.scenario.ScenarioViewSwing
import ch.scorpion.jabbah.graph.ui.usecase.UsecaseViewSwing
import java.awt.BorderLayout
import javax.swing.JPanel

/**
 * A [javax.swing] implementation of [GraphEditView] using a [SidebarPane] for displaying
 * a [ScenarioViewSwing] and a [UsecaseViewSwing] at the right side of the view.
 */
class GraphEditViewSwing(
	controller: GraphEditViewController,
	application: Application,
	editor: Editor,
	viewManager: ViewManager,
	propertySheetFactory: PropertySheetPanelFactory,
	eventBus: EventBus = BaseModule.eventBus
) : JPanel(), GraphEditView {

	val graphNavigationView = GraphNavigationViewSwing(
		controller = controller.graphNavigationViewController,
		drawingView = controller.drawingView,
		viewManager = viewManager,
		contextBorderColor = null)

	private val scenarioView = ScenarioViewSwing(controller.scenarioViewController, application, editor, eventBus, propertySheetFactory)

	private val usecaseView = UsecaseViewSwing(controller.usecaseViewController, application, editor, eventBus, propertySheetFactory)

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
				usecaseView)
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
}