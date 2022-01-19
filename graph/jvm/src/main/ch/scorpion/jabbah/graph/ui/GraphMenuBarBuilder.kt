package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.app.MenuBarBuilder
import ch.scorpion.jabbah.app.action.AboutAction
import ch.scorpion.jabbah.app.action.ExportLogfileAction
import ch.scorpion.jabbah.app.action.QuitApplicationAction
import ch.scorpion.jabbah.app.action.SaveFileAction
import ch.scorpion.jabbah.base.ActionWrapperSwing
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.auth0.LoginLogoutAction
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.preferences.PreferencesAction
import ch.scorpion.jabbah.draw.svg.ExportSvgAction
import ch.scorpion.jabbah.draw.view.DrawViewModule
import ch.scorpion.jabbah.edit.app.CopyAction
import ch.scorpion.jabbah.edit.app.CutAction
import ch.scorpion.jabbah.edit.app.PasteAction
import ch.scorpion.jabbah.edit.auth.EditAuthModule
import ch.scorpion.jabbah.execution.*
import ch.scorpion.jabbah.execution.scheduler.Scheduler
import ch.scorpion.jabbah.graph.app.ToggleApplicationModeAction
import ch.scorpion.jabbah.graph.container.EditSubGraphVerticeViewAction
import ch.scorpion.jabbah.graph.library.ShowLibrariesDialogAction
import ch.scorpion.jabbah.graph.model.net.SignalConflictBehaviourMenu
import ch.scorpion.jabbah.graph.project.ShowProjectsDialogAction
import ch.scorpion.jabbah.graph.ui.scenario.AddScenarioAction
import ch.scorpion.jabbah.graph.ui.scenario.AddScenarioStepAction
import ch.scorpion.jabbah.graph.ui.scenario.DeleteScenarioAction
import ch.scorpion.jabbah.graph.ui.scenario.DeleteScenarioStepAction
import ch.scorpion.jabbah.graph.ui.usecase.*
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
		menu.add(JMenuItem(ActionWrapperSwing(ShowProjectsDialogAction(graphFrame.controller.applicationModeHolder, frame))))
		menu.add(JMenuItem(ActionWrapperSwing(ShowLibrariesDialogAction(graphFrame.controller.applicationModeHolder, frame))))
		menu.add(openRecentMenu)
		menu.add(JMenuItem(ActionWrapperSwing(SaveFileAction(frame.application))))
		menu.addSeparator()
		if (SystemUtils.IS_OS_MAC) {
			menu.add(JMenuItem(ActionWrapperSwing(ExportSvgAction())))
			menu.addSeparator()
		}
		if (EditAuthModule.userHolder.user.isDeveloper) {
			// Public available not before server has been released
			menu.add(JMenuItem(ActionWrapperSwing(LoginLogoutAction())))
		}
		menu.add(JMenuItem(ActionWrapperSwing(GraphStatisticsAction())))
		menu.add(JMenuItem(ActionWrapperSwing(ExportLogfileAction(frame.application))))
		if (!SystemUtils.IS_OS_MAC) {
			menu.add(JMenuItem(ActionWrapperSwing(PreferencesAction())))
			menu.addSeparator()
			menu.add(JMenuItem(ActionWrapperSwing(QuitApplicationAction(frame.application))))
		}
	}

	override fun fillEditMenu(menu: JMenu) {
		super.fillEditMenu(menu)
		menu.addSeparator()
		menu.add(JMenuItem(ActionWrapperSwing(CutAction())))
		menu.add(JMenuItem(ActionWrapperSwing(CopyAction())))
		menu.add(JMenuItem(ActionWrapperSwing(PasteAction())))
		menu.addSeparator()
		menu.add(JMenuItem(ActionWrapperSwing(OpenGraphNavigationAction(DrawViewModule.viewManager, eventBus))))
		menu.add(JMenuItem(ActionWrapperSwing(EditSubGraphVerticeViewAction((frame as GraphFrameSwing).controller.applicationContextHolder))))
	}

	override fun fillViewMenu(menu: JMenu) {
		menu.add(JCheckBoxMenuItem(ActionWrapperSwing(graphFrame.actions.viewDesktopAction)))
		menu.add(JCheckBoxMenuItem(ActionWrapperSwing(graphFrame.actions.viewContainerAction)))
		menu.addSeparator()
		super.fillViewMenu(menu)
		menu.add(JCheckBoxMenuItem(ActionWrapperSwing(OscilloscopeAction(DrawViewModule.viewManager, eventBus))))
	}

	protected open fun fillExecutionMenu(menu: JMenu): JMenu {
		menu.add(JCheckBoxMenuItem(ActionWrapperSwing(ToggleApplicationModeAction(frame.application.controller, graphFrame.controller.applicationModeHolder))))
		menu.add(JMenuItem(ActionWrapperSwing(PauseOrResumeAction(scheduler))))
		menu.add(JCheckBoxMenuItem(ActionWrapperSwing(ExecutionDepthAction(scheduler))))
		menu.add(JCheckBoxMenuItem(ActionWrapperSwing(StopOnIssueAction(scheduler))))
		menu.add(JCheckBoxMenuItem(ActionWrapperSwing(SimulationTimeStatusEnabledAction(scheduler))))
		menu.add(JCheckBoxMenuItem(ActionWrapperSwing(EnableSoftBreakpointsAction(scheduler))))
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