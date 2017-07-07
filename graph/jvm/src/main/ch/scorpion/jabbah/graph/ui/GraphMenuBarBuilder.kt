package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.app.DesktopApplication
import ch.scorpion.jabbah.app.MenuBarBuilder
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.draw.view.DrawViewModule
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.execution.ExecutionDepthAction
import ch.scorpion.jabbah.graph.container.EditSubGraphVerticeViewAction
import ch.scorpion.jabbah.graph.library.AddGraphToLibraryAction
import ch.scorpion.jabbah.graph.library.AddLibraryFolderAction
import ch.scorpion.jabbah.graph.library.DeleteContainerLibraryElementAction
import ch.scorpion.jabbah.graph.library.EditContainerLibraryElementAction
import ch.scorpion.jabbah.graph.ui.scenario.AddScenarioAction
import javax.swing.JCheckBoxMenuItem
import javax.swing.JMenu
import javax.swing.JMenuBar
import javax.swing.JMenuItem

/**
 * Adds [ch.scorpion.jabbah.graph] related menus to [MenuBarBuilder].
 */
open class GraphMenuBarBuilder(application: DesktopApplication, eventBus: EventBus) : MenuBarBuilder(application, eventBus) {

    override fun fillMenuBar(menuBar: JMenuBar) {
        super.fillMenuBar(menuBar)
        menuBar.add(fillLibraryMenu(JMenu(Translations.getString("application.menu.library"))))
        menuBar.add(fillScenariosMenu(JMenu(Translations.getString("application.menu.scenarios"))))
        menuBar.add(fillExecutionMenu(JMenu(Translations.getString("application.menu.simulation"))))
    }

    override fun fillEditMenu(menu: JMenu) {
        super.fillEditMenu(menu)
        menu.addSeparator()
        menu.add(JMenuItem(CutAction()))
        menu.add(JMenuItem(CopyAction()))
        menu.add(JMenuItem(PasteAction()))
        menu.addSeparator()
        menu.add(JMenuItem(OpenGraphNavigationPanelAction(DrawViewModule.viewManager, eventBus)))
        menu.add(JMenuItem(EditSubGraphVerticeViewAction()))
    }

    protected open fun fillExecutionMenu(menu: JMenu): JMenu {
        menu.add(JCheckBoxMenuItem(ExecutionDepthAction()))
        return menu
    }

    protected open fun fillLibraryMenu(menu: JMenu): JMenu {
        menu.add(JMenuItem(AddLibraryFolderAction()))
        menu.add(JMenuItem(AddGraphToLibraryAction()))
        menu.add(JMenuItem(EditContainerLibraryElementAction(application, eventBus)))
        menu.add(JMenuItem(DeleteContainerLibraryElementAction(eventBus)))
        return menu
    }

    protected open fun fillScenariosMenu(menu: JMenu): JMenu {
        menu.add(JMenuItem(AddScenarioAction(eventBus, EditModule.commandManager)))
        return menu
    }
}