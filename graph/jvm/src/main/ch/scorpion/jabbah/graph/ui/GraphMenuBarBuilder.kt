package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.app.MenuBarBuilder
import ch.scorpion.jabbah.app.action.AboutAction
import ch.scorpion.jabbah.app.action.ExportLogfileAction
import ch.scorpion.jabbah.app.action.QuitApplicationAction
import ch.scorpion.jabbah.app.action.SaveFileAction
import ch.scorpion.jabbah.base.ActionWrapperSwing
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.preferences.PreferencesAction
import ch.scorpion.jabbah.draw.view.DrawViewModule
import ch.scorpion.jabbah.edit.app.CopyAction
import ch.scorpion.jabbah.edit.app.CutAction
import ch.scorpion.jabbah.edit.app.PasteAction
import ch.scorpion.jabbah.execution.*
import ch.scorpion.jabbah.graph.container.EditSubGraphVerticeViewAction
import ch.scorpion.jabbah.graph.library.ShowLibrariesDialogAction
import ch.scorpion.jabbah.graph.project.ShowProjectsDialogAction
import ch.scorpion.jabbah.graph.app.ToggleApplicationModeAction
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

	private val graphFrame: GraphFrameSwing get() = frame as GraphFrameSwing

	override fun fillMenuBar(menuBar: JMenuBar) {
		super.fillMenuBar(menuBar)
		menuBar.add(fillScenariosMenu(JMenu(Translations.getString("application.menu.scenarios"))))
		menuBar.add(fillUsecasesMenu(JMenu(Translations.getString("application.menu.usecases"))))
		menuBar.add(fillExecutionMenu(JMenu(Translations.getString("application.menu.simulation"))))
	}

	override fun fillFileMenu(menu: JMenu) {
		if (!SystemUtils.IS_OS_MAC) {
			menu.add(JMenuItem(ActionWrapperSwing(AboutAction(frame.application))))
			menu.addSeparator()
		}
		menu.add(JMenuItem(ActionWrapperSwing(ShowProjectsDialogAction(frame))))
		menu.add(JMenuItem(ActionWrapperSwing(ShowLibrariesDialogAction(frame))))
		menu.add(openRecentMenu)
		menu.add(JMenuItem(ActionWrapperSwing(SaveFileAction(frame.application))))
		menu.addSeparator()
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
		menu.add(JMenuItem(ActionWrapperSwing(OpenGraphNavigationPanelAction(DrawViewModule.viewManager, eventBus))))
		menu.add(JMenuItem(ActionWrapperSwing(EditSubGraphVerticeViewAction())))
	}

	override fun fillViewMenu(menu: JMenu) {
		menu.add(JCheckBoxMenuItem(ActionWrapperSwing(graphFrame.actions.viewDesktopAction)))
		menu.add(JCheckBoxMenuItem(ActionWrapperSwing(graphFrame.actions.viewContainerAction)))
		menu.addSeparator()
		super.fillViewMenu(menu)
		menu.add(JCheckBoxMenuItem(ActionWrapperSwing(OscilloscopeAction(DrawViewModule.viewManager, eventBus))))
	}

	protected open fun fillExecutionMenu(menu: JMenu): JMenu {
		menu.add(JCheckBoxMenuItem(ActionWrapperSwing(ToggleApplicationModeAction(frame.application.controller))))
		menu.add(JMenuItem(ActionWrapperSwing(ResumeExecutionAction())))
		menu.add(JCheckBoxMenuItem(ActionWrapperSwing(ExecutionDepthAction())))
		menu.add(JCheckBoxMenuItem(ActionWrapperSwing(StopOnIssueAction())))
		menu.add(JCheckBoxMenuItem(ActionWrapperSwing(SimulationTimeStatusEnabledAction())))
		menu.add(JCheckBoxMenuItem(ActionWrapperSwing(EnableSoftBreakpointsAction())))
		return menu
	}

	protected open fun fillScenariosMenu(menu: JMenu): JMenu {
		menu.add(JMenuItem(ActionWrapperSwing(AddScenarioAction(frame.application))))
		menu.add(JMenuItem(ActionWrapperSwing(AddScenarioStepAction(frame.application))))
		menu.add(JMenuItem(ActionWrapperSwing(DeleteScenarioAction(frame.application))))
		menu.add(JMenuItem(ActionWrapperSwing(DeleteScenarioStepAction(frame.application))))
		return menu
	}

	protected open fun fillUsecasesMenu(menu: JMenu): JMenu {
		menu.add(JMenuItem(ActionWrapperSwing(AddUsecaseAction(frame.application))))
		menu.add(JMenuItem(ActionWrapperSwing(DeleteUsecaseAction(frame.application))))
		menu.addSeparator()
		menu.add(JMenuItem(ActionWrapperSwing(RunUsecaseAction(frame.application))))
		menu.add(JMenuItem(ActionWrapperSwing(RunSingleUsecaseTestAction(frame.application))))
		menu.add(JMenuItem(ActionWrapperSwing(RunAllTestsAction(frame.application))))
		return menu
	}
}