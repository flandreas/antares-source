package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.app.AbstractApplicationFrame
import ch.scorpion.jabbah.app.MenuBarBuilder
import ch.scorpion.jabbah.app.action.CloseFileAction
import ch.scorpion.jabbah.app.action.QuitApplicationAction
import ch.scorpion.jabbah.app.action.SaveFileAction
import ch.scorpion.jabbah.base.ActionWrapperSwing
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.preferences.PreferencesAction
import ch.scorpion.jabbah.draw.style.Themes
import ch.scorpion.jabbah.draw.view.DrawViewModule
import ch.scorpion.jabbah.execution.ExecutionDepthAction
import ch.scorpion.jabbah.execution.StopOnIssueAction
import ch.scorpion.jabbah.graph.container.EditSubGraphVerticeViewAction
import ch.scorpion.jabbah.graph.library.*
import ch.scorpion.jabbah.graph.project.ShowProjectsDialogAction
import ch.scorpion.jabbah.graph.ui.scenario.AddScenarioAction
import ch.scorpion.jabbah.graph.ui.scenario.AddScenarioStepAction
import ch.scorpion.jabbah.graph.ui.scenario.DeleteScenarioAction
import ch.scorpion.jabbah.graph.ui.scenario.DeleteScenarioStepAction
import javax.swing.JCheckBoxMenuItem
import javax.swing.JMenu
import javax.swing.JMenuBar
import javax.swing.JMenuItem

/**
 * Adds [ch.scorpion.jabbah.graph] related menus to [MenuBarBuilder].
 */
open class GraphMenuBarBuilder(
	frame: AbstractApplicationFrame,
	eventBus: EventBus
) : MenuBarBuilder(frame = frame, eventBus = eventBus) {

    override fun fillMenuBar(menuBar: JMenuBar) {
        super.fillMenuBar(menuBar)
        menuBar.add(fillLibraryMenu(JMenu(Translations.getString("application.menu.library"))))
        menuBar.add(fillScenariosMenu(JMenu(Translations.getString("application.menu.scenarios"))))
        menuBar.add(fillExecutionMenu(JMenu(Translations.getString("application.menu.simulation"))))
    }

	override fun fillFileMenu(menu: JMenu) {
		menu.add(JMenuItem(ActionWrapperSwing(ShowProjectsDialogAction(frame))))
		menu.add(JMenuItem(ActionWrapperSwing(ShowLibrariesDialogAction(frame))))
		menu.add(openRecentMenu)
		menu.add(JMenuItem(ActionWrapperSwing(SaveFileAction(frame.application))))
		menu.addSeparator()
		menu.add(JMenuItem(ActionWrapperSwing(GraphInfoAction())))
		menu.add(JMenuItem(ActionWrapperSwing(PreferencesAction())))
		menu.addSeparator()
		menu.add(JMenuItem(ActionWrapperSwing(CloseFileAction(frame.application))))
		menu.add(JMenuItem(ActionWrapperSwing(QuitApplicationAction(frame.application))))
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
        menu.add(JCheckBoxMenuItem(ActionWrapperSwing(ExecutionDepthAction())))
        menu.add(JCheckBoxMenuItem(ActionWrapperSwing(StopOnIssueAction())))
        return menu
    }

    protected open fun fillLibraryMenu(menu: JMenu): JMenu {
        menu.add(JMenuItem(ActionWrapperSwing(AddLibraryFolderAction())))
        menu.add(JMenuItem(ActionWrapperSwing(NewGraphAction())))
        menu.add(JMenuItem(ActionWrapperSwing(EditContainerLibraryElementAction(frame.application, eventBus))))
        menu.add(JMenuItem(ActionWrapperSwing(DeleteLibraryElementAction(eventBus))))
	    menu.add(JMenuItem(ActionWrapperSwing(DuplicateGraphAction())))
        return menu
    }

    protected open fun fillScenariosMenu(menu: JMenu): JMenu {
        menu.add(JMenuItem(ActionWrapperSwing(AddScenarioAction())))
        menu.add(JMenuItem(ActionWrapperSwing(AddScenarioStepAction())))
        menu.add(JMenuItem(ActionWrapperSwing(DeleteScenarioAction())))
        menu.add(JMenuItem(ActionWrapperSwing(DeleteScenarioStepAction())))
        return menu
    }
}