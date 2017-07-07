package ch.scorpion.jabbah.app

import ch.scorpion.jabbah.app.action.*
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.*
import ch.scorpion.jabbah.draw.view.*
import ch.scorpion.jabbah.edit.*
import ch.scorpion.jabbah.edit.model.OneDownAction
import ch.scorpion.jabbah.edit.model.OneUpAction
import ch.scorpion.jabbah.edit.model.SelectAllAction
import ch.scorpion.jabbah.edit.model.ToBackAction
import javax.swing.JCheckBoxMenuItem
import javax.swing.JMenu
import javax.swing.JMenuBar
import javax.swing.JMenuItem

/**
 * Builds and fills a [JMenuBar] for the main [JFrame] of a [DesktopApplication].
 */
open class MenuBarBuilder(val application: DesktopApplication, val eventBus: EventBus) {

    val menuBar = JMenuBar()
    val fileMenu = JMenu(Translations.getString("application.menu.file"))
    val editMenu = JMenu(Translations.getString("application.menu.edit"))
    val viewMenu = JMenu(Translations.getString("application.menu.view"))

    init {
        fillFileMenu(fileMenu)
        fillEditMenu(editMenu)
        fillViewMenu(viewMenu)
        fillMenuBar(menuBar)
    }

    protected open fun fillMenuBar(menuBar: JMenuBar) {
        menuBar.add(fileMenu)
        menuBar.add(editMenu)
        menuBar.add(viewMenu)
    }

    protected open fun fillFileMenu(menu: JMenu) {
        menu.add(JMenuItem(NewFileAction(application)))
        menu.add(JMenuItem(OpenFileAction(application)))
        menu.add(JMenuItem(SaveFileAction(application)))
        menu.add(JMenuItem(SaveFileAsAction(application)))
        menu.addSeparator();
        menu.add(JMenuItem(QuitApplicationAction(application)))
    }

    protected open fun fillEditMenu(menu: JMenu) {
        menu.add(JMenuItem(UndoAction(BaseModule.eventBus, application.mainFrame.editor.commandManager)))
        menu.add(JMenuItem(RedoAction(BaseModule.eventBus, application.mainFrame.editor.commandManager)))
        menu.addSeparator()
        menu.add(JMenuItem(DeleteAction(DrawViewModule.viewManager, application.mainFrame.editor.commandManager)))
        menu.add(JMenuItem(RotateAction(DrawViewModule.viewManager, application.mainFrame.editor.commandManager, BaseModule.eventBus)))
        menu.addSeparator()
        menu.add(JMenuItem(SelectAllAction(eventBus, DrawViewModule.viewManager)))
        menu.addSeparator()
        val arrangeMenu = JMenu(Translations.getString("edit.action.stackingOrder.name"))
        arrangeMenu.add(JMenuItem(ToFrontAction(DrawViewModule.viewManager, application.mainFrame.editor.commandManager, BaseModule.eventBus)))
        arrangeMenu.add(JMenuItem(OneUpAction(DrawViewModule.viewManager, application.mainFrame.editor.commandManager, BaseModule.eventBus)))
        arrangeMenu.add(JMenuItem(OneDownAction(DrawViewModule.viewManager, application.mainFrame.editor.commandManager, BaseModule.eventBus)))
        arrangeMenu.add(JMenuItem(ToBackAction(DrawViewModule.viewManager, application.mainFrame.editor.commandManager, BaseModule.eventBus)))
        menu.add(arrangeMenu)
    }

    protected open fun fillViewMenu(menu: JMenu) {
        menu.add(JMenuItem(ZoomInAction(DrawViewModule.viewManager, BaseModule.eventBus)))
        menu.add(JMenuItem(ZoomNormalAction(DrawViewModule.viewManager, BaseModule.eventBus)))
        menu.add(JMenuItem(ZoomOutAction(DrawViewModule.viewManager, BaseModule.eventBus)))
        menu.add(JMenuItem(ZoomCenterAction(DrawViewModule.viewManager, BaseModule.eventBus)))
        menu.add(JMenuItem(ZoomFitAction(DrawViewModule.viewManager, BaseModule.eventBus)))
        menu.addSeparator()
        menu.add(JCheckBoxMenuItem(GridAction(DrawViewModule.viewManager, BaseModule.eventBus)))
    }
}