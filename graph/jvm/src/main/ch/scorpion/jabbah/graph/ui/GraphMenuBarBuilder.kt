package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.app.MenuBarBuilder
import ch.scorpion.jabbah.app.action.AboutAction
import ch.scorpion.jabbah.app.action.ExportLogfileAction
import ch.scorpion.jabbah.app.action.QuitApplicationAction
import ch.scorpion.jabbah.app.rating.RatingAction
import ch.scorpion.jabbah.base.ActionWrapperSwing
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.preferences.PreferencesAction
import ch.scorpion.jabbah.draw.svg.ExportSvgAction
import ch.scorpion.jabbah.draw.view.DrawViewModule
import ch.scorpion.jabbah.edit.app.CopyAction
import ch.scorpion.jabbah.edit.app.CutAction
import ch.scorpion.jabbah.edit.app.DuplicateAction
import ch.scorpion.jabbah.edit.app.PasteAction
import ch.scorpion.jabbah.execution.scheduler.Scheduler
import ch.scorpion.jabbah.graph.container.editsubgraph.EditSubGraphVerticeViewAction
import ch.scorpion.jabbah.graph.library.NewGraphAction
import ch.scorpion.jabbah.graph.library.ShowLibrariesDialogAction
import ch.scorpion.jabbah.graph.model.net.SignalConflictBehaviourMenu
import ch.scorpion.jabbah.graph.module.GraphModuleJvm
import ch.scorpion.jabbah.graph.project.ShowProjectsDialogAction
import ch.scorpion.jabbah.graph.ui.scenario.AddScenarioAction
import ch.scorpion.jabbah.graph.ui.scenario.AddScenarioStepAction
import ch.scorpion.jabbah.graph.ui.scenario.DeleteScenarioAction
import ch.scorpion.jabbah.graph.ui.scenario.DeleteScenarioStepAction
import ch.scorpion.jabbah.graph.ui.usecase.*
import ch.scorpion.jabbah.graph.view.oscilloscope.OscilloscopeVisibilityAction
import org.apache.commons.lang3.SystemUtils
import javax.swing.JCheckBoxMenuItem
import javax.swing.JMenu
import javax.swing.JMenuBar
import javax.swing.JMenuItem

/**
 * Adds [ch.scorpion.jabbah.graph] related menus to [MenuBarBuilder].
 */
open class GraphMenuBarBuilder(
	frame: GraphFrameSwing,
	eventBus: EventBus
) : MenuBarBuilder(frame = frame, eventBus = eventBus) {

	companion object {
		fun createExecutionMenu() = JMenu(Translations.getString("application.menu.simulation"))
		fun createScenariosMenu() = JMenu(Translations.getString("application.menu.scenarios"))
		fun createUsecasesMenu() = JMenu(Translations.getString("application.menu.usecases"))
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

		menu.add(JMenuItem(ActionWrapperSwing(ShowProjectsDialogAction(graphFrame.controller.applicationModeHolder, frame))))
		menu.add(JMenuItem(ActionWrapperSwing(ShowLibrariesDialogAction(graphFrame.controller.applicationModeHolder, frame))))
		menu.addSeparator()

		menu.add(JMenuItem(ActionWrapperSwing(ExportSvgAction())))
		menu.add(JMenuItem(ActionWrapperSwing(ExportLogfileAction(frame.application))))
		menu.addSeparator()

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

	override fun fillEditMenu(menu: JMenu) {
		super.fillEditMenu(menu)
		menu.addSeparator()
		menu.add(JMenuItem(ActionWrapperSwing(CutAction())))
		menu.add(JMenuItem(ActionWrapperSwing(CopyAction())))
		menu.add(JMenuItem(ActionWrapperSwing(PasteAction())))
		menu.add(JMenuItem(ActionWrapperSwing(DuplicateAction())))
		menu.addSeparator()
		menu.add(JMenuItem(ActionWrapperSwing(OpenGraphNavigationAction(DrawViewModule.viewManager, eventBus))))
		menu.add(JMenuItem(ActionWrapperSwing(EditSubGraphVerticeViewAction(graphFrame.controller.applicationContextHolder))))
		menu.add(JMenuItem(ActionWrapperSwing(ExtractMetaGraphAction(graphFrame.application.controller))))
	}

	override fun fillViewMenu(menu: JMenu) {
		menu.add(JCheckBoxMenuItem(ActionWrapperSwing(graphFrame.actions.viewDesktopAction)))
		menu.add(JCheckBoxMenuItem(ActionWrapperSwing(graphFrame.actions.viewContainerAction)))
		menu.addSeparator()
		super.fillViewMenu(menu)
		menu.add(JCheckBoxMenuItem(ActionWrapperSwing(OscilloscopeVisibilityAction(DrawViewModule.viewManager, eventBus))))
	}

	protected open fun fillExecutionMenu(menu: JMenu): JMenu {
		graphFrame.controller.graphPanelViewController.also {
			menu.add(JCheckBoxMenuItem(ActionWrapperSwing(it.toggleApplicationModeAction)))
			menu.add(JMenuItem(ActionWrapperSwing(it.pauseOrResumeAction)))
			menu.add(JCheckBoxMenuItem(ActionWrapperSwing(it.singleStepModeAction)))
			menu.add(JCheckBoxMenuItem(ActionWrapperSwing(it.executionDepthAction)))
			menu.add(JCheckBoxMenuItem(ActionWrapperSwing(it.stopOnIssueAction)))
			menu.add(JCheckBoxMenuItem(ActionWrapperSwing(it.simulationTimeStatusEnabledAction)))
			menu.add(JCheckBoxMenuItem(ActionWrapperSwing(it.enableSoftBreakpointsAction)))
		}
		menu.add(SignalConflictBehaviourMenu())
		return menu
	}

	protected open fun fillScenariosMenu(menu: JMenu): JMenu {
		menu.add(JMenuItem(ActionWrapperSwing(AddScenarioAction(frame.application, graphFrame.controller.applicationModeHolder))))
		menu.add(JMenuItem(ActionWrapperSwing(AddScenarioStepAction(frame.application, graphFrame.controller.applicationModeHolder))))
		menu.add(JMenuItem(ActionWrapperSwing(DeleteScenarioAction(frame.application, graphFrame.controller.applicationModeHolder))))
		menu.add(JMenuItem(ActionWrapperSwing(DeleteScenarioStepAction(frame.application, graphFrame.controller.applicationModeHolder))))
		return menu
	}

	protected open fun fillUsecasesMenu(menu: JMenu): JMenu {
		val applicationModeHolder = graphFrame.controller.applicationModeHolder
		menu.add(JMenuItem(ActionWrapperSwing(AddUsecaseAction(frame.application, graphFrame.controller.applicationModeHolder))))
		menu.add(JMenuItem(ActionWrapperSwing(DeleteUsecaseAction(frame.application, graphFrame.controller.applicationModeHolder))))
		menu.add(JMenuItem(ActionWrapperSwing(DuplicateUsecaseAction(frame.application, graphFrame.controller.applicationModeHolder))))
		menu.addSeparator()
		menu.add(JMenuItem(ActionWrapperSwing(RunUsecaseAction(frame.application, applicationModeHolder = applicationModeHolder, scheduler = scheduler))))
		menu.add(JMenuItem(ActionWrapperSwing(RunSingleUsecaseTestAction(frame.application, applicationModeHolder = applicationModeHolder, scheduler = scheduler))))
		menu.add(JMenuItem(ActionWrapperSwing(RunAllTestsAction(frame.application, applicationModeHolder = applicationModeHolder, scheduler = scheduler))))
		return menu
	}

	override fun createOpenRecentMenu(): JMenu =
		OpenRecentGraphMenu(frame.application, graphFrame.controller.applicationModeHolder)
}