package io.antarescircuit.jabbah.graph.ui

import io.antarescircuit.jabbah.app.MenuBarBuilder
import io.antarescircuit.jabbah.app.action.AboutAction
import io.antarescircuit.jabbah.app.action.ExportLogfileAction
import io.antarescircuit.jabbah.app.action.QuitApplicationAction
import io.antarescircuit.jabbah.app.rating.RatingAction
import io.antarescircuit.jabbah.app.workspace.OpenWorkspaceAction
import io.antarescircuit.jabbah.base.ActionWrapperSwing
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.preferences.PreferencesAction
import io.antarescircuit.jabbah.draw.rasterimg.ExportRasterImageAction
import io.antarescircuit.jabbah.draw.svg.ExportSvgAction
import io.antarescircuit.jabbah.draw.view.DrawViewModule
import io.antarescircuit.jabbah.edit.app.CopyAction
import io.antarescircuit.jabbah.edit.app.CutAction
import io.antarescircuit.jabbah.edit.app.DuplicateAction
import io.antarescircuit.jabbah.edit.app.PasteAction
import io.antarescircuit.jabbah.execution.scheduler.Scheduler
import io.antarescircuit.jabbah.graph.container.editsubgraph.EditSubGraphVerticeViewAction
import io.antarescircuit.jabbah.graph.library.NewGraphAction
import io.antarescircuit.jabbah.graph.library.ShowLibrariesDialogAction
import io.antarescircuit.jabbah.graph.model.net.SignalConflictBehaviourMenu
import io.antarescircuit.jabbah.graph.module.GraphModuleJvm
import io.antarescircuit.jabbah.graph.project.ShowProjectsDialogAction
import io.antarescircuit.jabbah.graph.ui.documentation.OpenDocumentationAction
import io.antarescircuit.jabbah.graph.ui.portrenaming.GraphPortRenamingAction
import io.antarescircuit.jabbah.graph.ui.usecase.*
import io.antarescircuit.jabbah.graph.view.oscilloscope.OscilloscopeVisibilityAction
import io.antarescircuit.jabbah.graph.view.scenario.ScenarioBreakpointAction
import io.antarescircuit.jabbah.graph.view.scenario.ScenarioModeMenu
import org.apache.commons.lang3.SystemUtils
import javax.swing.JCheckBoxMenuItem
import javax.swing.JMenu
import javax.swing.JMenuBar
import javax.swing.JMenuItem

/**
 * Adds [io.antarescircuit.jabbah.graph] related menus to [MenuBarBuilder].
 */
open class GraphMenuBarBuilder(
	frame: GraphFrameSwing,
	eventBus: EventBus
) : MenuBarBuilder(frame = frame, eventBus = eventBus) {

	companion object {
		fun createExecutionMenu() = JMenu(Translations.getString("application.menu.simulation"))
		fun createScenariosMenu() = JMenu(Translations.getString("application.menu.scenarios"))
		fun createUsecasesMenu() = JMenu(Translations.getString("application.menu.usecases"))
		fun createExportMenu() = JMenu(Translations.getString("application.menu.file.export"))
	}

	protected val graphFrame: GraphFrameSwing get() = frame as GraphFrameSwing

	private val scheduler: Scheduler get() = graphFrame.controller.applicationContextHolder.scheduler

	override fun fillMenuBar(menuBar: JMenuBar) {
		super.fillMenuBar(menuBar)
		menuBar.add(fillExecutionMenu(createExecutionMenu()))
		menuBar.add(fillScenariosMenu(createScenariosMenu()))
		menuBar.add(fillUsecasesMenu(createUsecasesMenu()))
	}

	override fun fillFileMenu(menu: JMenu) {
		if (!SystemUtils.IS_OS_MAC) {
			menu.add(JMenuItem(ActionWrapperSwing(AboutAction(frame.application))))
			menu.addSeparator()
		}
		menu.add(JMenuItem(ActionWrapperSwing(NewGraphAction(graphFrame.controller.graphPanelViewController.libraryPanelController.libraryTreeViewController))))
		menu.add(openRecentMenu)
		menu.add(JMenuItem(ActionWrapperSwing(frame.application.controller.saveAction)))
		menu.addSeparator()

		menu.add(JMenuItem(ActionWrapperSwing(OpenWorkspaceAction(frame.application))))
		menu.add(JMenuItem(ActionWrapperSwing(ShowProjectsDialogAction(graphFrame.controller.applicationModeHolder, frame))))
		menu.add(JMenuItem(ActionWrapperSwing(ShowLibrariesDialogAction(graphFrame.controller.applicationModeHolder, frame))))
		menu.addSeparator()

		val exportMenu = createExportMenu()
		fillExportMenu(exportMenu)
		menu.add(exportMenu)

		if (GraphModuleJvm.supportWeb) {
			// Public available not before server has been released
			menu.add(JMenuItem(ActionWrapperSwing(graphFrame.loginLogoutAction)))
		}
		menu.add(JMenuItem(ActionWrapperSwing(GraphStatisticsAction())))
		if (!SystemUtils.IS_OS_MAC) {
			menu.add(JMenuItem(ActionWrapperSwing(PreferencesAction())))
			menu.addSeparator()
			menu.add(JMenuItem(ActionWrapperSwing(QuitApplicationAction(frame.application))))
		}
		menu.add(JMenuItem(ActionWrapperSwing(RatingAction(frame.application))))
	}

	open fun fillExportMenu(menu: JMenu) {
		menu.add(JMenuItem(ActionWrapperSwing(ExportSvgAction())))
		menu.add(JMenuItem(ActionWrapperSwing(ExportRasterImageAction())))
		menu.add(JMenuItem(ActionWrapperSwing(ExportLogfileAction(frame.application))))
	}

	override fun fillEditMenu(menu: JMenu) {
		super.fillEditMenu(menu)
		menu.addSeparator()
		menu.add(JMenuItem(ActionWrapperSwing(CutAction())))
		menu.add(JMenuItem(ActionWrapperSwing(CopyAction())))
		menu.add(JMenuItem(ActionWrapperSwing(PasteAction())))
		menu.add(JMenuItem(ActionWrapperSwing(DuplicateAction())))
		menu.addSeparator()
		menu.add(JMenuItem(ActionWrapperSwing(OpenGraphNavigationAction(DrawViewModule.viewManager, eventBus))))
		menu.add(JMenuItem(ActionWrapperSwing(EditSubGraphVerticeViewAction(graphFrame.controller.applicationContextHolder, graphFrame.application.controller))))
		menu.add(JMenuItem(ActionWrapperSwing(ExtractMetaGraphAction(graphFrame.application.controller))))
		menu.add(JMenuItem(ActionWrapperSwing(GraphPortRenamingAction(graphFrame.editor))))
		menu.add(JMenuItem(ActionWrapperSwing(CalculatePropagationDelayAction(graphFrame.application.controller, graphFrame.controller.applicationModeHolder))))
	}

	override fun fillViewMenu(menu: JMenu) {
		menu.add(JCheckBoxMenuItem(ActionWrapperSwing(graphFrame.actions.viewDesktopAction)))
		menu.add(JCheckBoxMenuItem(ActionWrapperSwing(graphFrame.actions.viewContainerAction)))
		menu.addSeparator()
		super.fillViewMenu(menu)
		menu.add(JCheckBoxMenuItem(ActionWrapperSwing(OscilloscopeVisibilityAction(graphFrame.controller.applicationContextHolder, DrawViewModule.viewManager, eventBus))))
		menu.add(JCheckBoxMenuItem(ActionWrapperSwing(OpenDocumentationAction(DrawViewModule.viewManager, eventBus))))
	}

	protected open fun fillExecutionMenu(menu: JMenu): JMenu {
		graphFrame.controller.graphPanelViewController.also {
			menu.add(JCheckBoxMenuItem(ActionWrapperSwing(it.toggleApplicationModeAction)))
			menu.add(JMenuItem(ActionWrapperSwing(it.pauseOrResumeAction)))
			menu.add(JCheckBoxMenuItem(ActionWrapperSwing(it.singleStepModeAction)))
			menu.add(JCheckBoxMenuItem(ActionWrapperSwing(it.executionDepthAction)))
			menu.add(JCheckBoxMenuItem(ActionWrapperSwing(it.stopOnIssueAction)))
			menu.add(JCheckBoxMenuItem(ActionWrapperSwing(it.enableSoftBreakpointsAction)))
		}
		menu.add(SignalConflictBehaviourMenu())
		menu.add(simulationAnalysisMenu())
		menu.add(ScenarioModeMenu())

		return menu
	}

	private fun simulationAnalysisMenu(): JMenu {
		val analysisMenu = JMenu(Translations.getString("execution.menu.analysisTools"))
		analysisMenu.add(JMenuItem(ActionWrapperSwing(graphFrame.graphPanel.resetExecutionTimeAction)))
		analysisMenu.add(JCheckBoxMenuItem(ActionWrapperSwing(graphFrame.controller.graphPanelViewController.simulationTimeStatusEnabledAction)))
		analysisMenu.add(JCheckBoxMenuItem(ActionWrapperSwing(graphFrame.controller.graphPanelViewController.enableInteractivePropagationDelayAction)))
		return analysisMenu
	}

	protected open fun fillScenariosMenu(menu: JMenu): JMenu {
		with(graphFrame.controller.graphPanelViewController.editViewController.scenarioViewController) {
			menu.add(JMenuItem(ActionWrapperSwing(addScenarioAction)))
			menu.add(JMenuItem(ActionWrapperSwing(addScenarioStepAction)))
			menu.add(JMenuItem(ActionWrapperSwing(deleteScenarioAction)))
			menu.add(JMenuItem(ActionWrapperSwing(deleteScenarioStepAction)))
		}
		menu.add(JCheckBoxMenuItem(ActionWrapperSwing(ScenarioBreakpointAction(graphFrame.controller.applicationContextHolder.scenarioBreakpoints))))
		return menu
	}

	protected open fun fillUsecasesMenu(menu: JMenu): JMenu {
		with(graphFrame.controller.graphPanelViewController.editViewController) {
			menu.add(JMenuItem(ActionWrapperSwing(usecaseViewController.addUsecaseAction)))
			menu.add(JMenuItem(ActionWrapperSwing(DeleteUsecaseAction(usecaseViewController))))
			menu.add(JMenuItem(ActionWrapperSwing(DuplicateUsecaseAction(usecaseViewController))))
			menu.add(JMenuItem(ActionWrapperSwing(RecordUsecaseAction(usecaseViewController))))
			menu.addSeparator()
			menu.add(JMenuItem(ActionWrapperSwing(RunUsecaseAction(usecaseViewController))))
			menu.add(JMenuItem(ActionWrapperSwing(RunSingleUsecaseTestAction(usecaseViewController))))
			menu.add(JMenuItem(ActionWrapperSwing(RunAllTestsAction(usecaseViewController))))
		}
		return menu
	}

	override fun createOpenRecentMenu(): JMenu =
		OpenRecentGraphMenu(frame.application, graphFrame.controller.applicationModeHolder)
}