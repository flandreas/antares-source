package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.swing.SidebarPane
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
import javax.swing.JSplitPane

/**
 * A [JPanel] for editing a root [GraphView].
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
	private val eventBus: EventBus = BaseModule.eventBus
) : JPanel(){

	companion object {
		private val LOG by logger(GraphEditPanel::class)
		private const val DEF_SIDEBAR_SIZE = 200
	}
	
	val graphNavigationPanel = graphNavigationPanelFactory.create(
		isRoot = true,
		drawingView = editor.view as DrawingView<GraphView<GraphElementView<*>>>,
		viewManager = viewManager,
		closeHandler = closeHandler,
		contextColor = null,
		scheduler = scheduler)

	private val scenarioPanel = ScenarioPanel(editor, eventBus, propertySheetFactory)

	private val sidebarPane = SidebarPane(SidebarPane.Orientation.Vertical, { sidebarPaneChanged() })

	private val sidebarSplitPane = JSplitPane(JSplitPane.HORIZONTAL_SPLIT)

	/** Holds the location of [sidebarSplitPane]'s divider for re-establishing it the next time it opens.*/
	private var sidebarDividerLocation: Int = BaseModule.settings.getInt("graphPanel.rightSidebarSplitPos", -1)

	init {
		buildUI()
	}

	fun dispose() {
		BaseModule.settings.set("graphPanel.sidebarSplitPos", sidebarSplitPane.dividerLocation)
	}

	fun setGraphView(newGraphView: GraphView<GraphElementView<*>>?) {
		val oldGraphView = graphNavigationPanel.drawingView.drawing
		if (newGraphView != null) {
			graphNavigationPanel.setRootGraphView(newGraphView)
			scenarioPanel.graphView = newGraphView
		}
		eventBus.post(EditedGraphViewEvent(oldGraphView, newGraphView))
	}

	private fun buildUI() {
		layout = BorderLayout()

		sidebarSplitPane.border = null
		sidebarSplitPane.resizeWeight = 1.0

		val usecasesDummy = JLabel(Translations.getString("application.notYetImplemented.text"))
		usecasesDummy.border = BorderFactory.createEmptyBorder(5, 5, 5, 5)
		usecasesDummy.verticalAlignment = JLabel.TOP

		sidebarPane.add(Translations.getString("graph.scenarios.title"), "/img/scenarios-16.png", scenarioPanel)
		sidebarPane.add(Translations.getString("graph.usecases.title"), "/img/usecase-16.png", usecasesDummy)

		add(graphNavigationPanel, BorderLayout.CENTER)
		add(sidebarPane, BorderLayout.EAST)
	}

	/** Handles changes of the ´isOpen´ property of the [sidebarPane]. */
	private fun sidebarPaneChanged() {
		if (sidebarPane.isOpen) {
			removeAll()
			sidebarSplitPane.remove(sidebarPane)
			sidebarSplitPane.remove(graphNavigationPanel)
			sidebarSplitPane.add(graphNavigationPanel)
			sidebarSplitPane.add(sidebarPane)
			sidebarSplitPane.dividerLocation = if (sidebarDividerLocation > 0) sidebarDividerLocation else graphNavigationPanel.width - DEF_SIDEBAR_SIZE
			sidebarDividerLocation = sidebarSplitPane.dividerLocation
			add(sidebarSplitPane, BorderLayout.CENTER)
		} else {
			sidebarDividerLocation = sidebarSplitPane.dividerLocation
			removeAll()
			sidebarSplitPane.remove(sidebarPane)
			sidebarSplitPane.remove(graphNavigationPanel)
			add(graphNavigationPanel, BorderLayout.CENTER)
			add(sidebarPane, BorderLayout.EAST)
		}
		revalidate()
		repaint()
	}
}