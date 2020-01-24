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
import ch.scorpion.jabbah.draw.style.Themes
import ch.scorpion.jabbah.draw.view.DrawViewModule
import ch.scorpion.jabbah.execution.ExecutionDepthAction
import ch.scorpion.jabbah.execution.SimulationTimeStatusEnabledAction
import ch.scorpion.jabbah.execution.StepExecutionAction
import ch.scorpion.jabbah.execution.StopOnIssueAction
import ch.scorpion.jabbah.graph.container.EditSubGraphVerticeViewAction
import ch.scorpion.jabbah.graph.library.*
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

	protected val graphFrame: GraphFrameSwing get() = frame as GraphFrameSwing
	private val libraryTreeView: LibraryTreeView get() = graphFrame.graphPanel.libraryPanel.libraryTreeView

	override fun fillMenuBar(menuBar: JMenuBar) {
		super.fillMenuBar(menuBar)
		menuBar.add(fillLibraryMenu(JMenu(Translations.getString("application.menu.desktop"))))
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
		super.fillViewMenu(menu)
		val themesMenu = JMenu(Translations.getString("graph.action.themes.name"))
		for (theme in Themes.allThemes()) {
			themesMenu.add(JCheckBoxMenuItem(ActionWrapperSwing(ThemeAction(theme))))
		}
		menu.add(themesMenu)
		menu.add(JCheckBoxMenuItem(ActionWrapperSwing(OscilloscopeAction(DrawViewModule.viewManager, eventBus))))
	}

	protected open fun fillExecutionMenu(menu: JMenu): JMenu {
		menu.add(JCheckBoxMenuItem(ActionWrapperSwing(ToggleApplicationModeAction())))
		menu.add(JMenuItem(ActionWrapperSwing(StepExecutionAction())))
		menu.add(JCheckBoxMenuItem(ActionWrapperSwing(ExecutionDepthAction())))
		menu.add(JCheckBoxMenuItem(ActionWrapperSwing(StopOnIssueAction())))
		menu.add(JCheckBoxMenuItem(ActionWrapperSwing(SimulationTimeStatusEnabledAction())))
		return menu
	}

	protected open fun fillLibraryMenu(menu: JMenu): JMenu {
		menu.add(JMenuItem(ActionWrapperSwing(NewGraphAction(libraryTreeView))))
		menu.add(JMenuItem(ActionWrapperSwing(AddLibraryFolderAction(libraryTreeView))))
		menu.add(JMenuItem(ActionWrapperSwing(OpenContainerLibraryElementAction(frame.application, libraryTreeView, eventBus))))
		menu.add(JMenuItem(ActionWrapperSwing(DeleteLibraryElementAction(libraryTreeView, eventBus))))
		menu.add(JMenuItem(ActionWrapperSwing(DuplicateGraphAction(libraryTreeView))))
		return menu
	}

	protected open fun fillScenariosMenu(menu: JMenu): JMenu {
		menu.add(JMenuItem(ActionWrapperSwing(AddScenarioAction())))
		menu.add(JMenuItem(ActionWrapperSwing(AddScenarioStepAction())))
		menu.add(JMenuItem(ActionWrapperSwing(DeleteScenarioAction())))
		menu.add(JMenuItem(ActionWrapperSwing(DeleteScenarioStepAction())))
		return menu
	}

	protected open fun fillUsecasesMenu(menu: JMenu): JMenu {
		menu.add(JMenuItem(ActionWrapperSwing(AddUsecaseAction())))
		menu.add(JMenuItem(ActionWrapperSwing(DeleteUsecaseAction())))
		menu.addSeparator()
		menu.add(JMenuItem(ActionWrapperSwing(RunUsecaseAction())))
		menu.add(JMenuItem(ActionWrapperSwing(RunSingleUsecaseTestAction())))
		menu.add(JMenuItem(ActionWrapperSwing(RunAllTestsAction())))
		return menu
	}
}