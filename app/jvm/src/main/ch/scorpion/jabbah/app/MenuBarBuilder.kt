package ch.scorpion.jabbah.app

import ch.scorpion.jabbah.app.action.*
import ch.scorpion.jabbah.base.ActionWrapperSwing
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.preferences.PreferencesAction
import ch.scorpion.jabbah.draw.view.*
import ch.scorpion.jabbah.edit.app.*
import javax.swing.JCheckBoxMenuItem
import javax.swing.JFrame
import javax.swing.JMenu
import javax.swing.JMenuBar
import javax.swing.JMenuItem

/**
 * Builds and fills a [JMenuBar] for the main [JFrame] of a [DesktopApplication].
 */
open class MenuBarBuilder(
	val frame: AbstractApplicationFrame,
	val eventBus: EventBus
) {

    val menuBar = JMenuBar()
    private val fileMenu = JMenu(Translations.getString("application.menu.file"))
    private val editMenu = JMenu(Translations.getString("application.menu.edit"))
    private val viewMenu = JMenu(Translations.getString("application.menu.view"))
    protected val openRecentMenu = JMenu(Translations.getString("file.action.openRecent.name"))

    init {
        fillFileMenu(fileMenu)
        fillEditMenu(editMenu)
        fillViewMenu(viewMenu)
        fillMenuBar(menuBar)

        eventBus.register(SavableHistoryEvent::class, { updateOpenRecentMenu() })
	    eventBus.register(CurrentSavableEvent::class) {
		    // Make a [Savable] that has just been closed visible in the menu
		    if (it.savable == null) {
			    updateOpenRecentMenu()
		    }
	    }
    }

    protected open fun fillMenuBar(menuBar: JMenuBar) {
        menuBar.add(fileMenu)
        menuBar.add(editMenu)
        menuBar.add(viewMenu)
    }

    protected open fun fillFileMenu(menu: JMenu) {
	    menu.add(JMenuItem(ActionWrapperSwing(AboutAction(frame.application))))
	    menu.addSeparator()
	    menu.add(JMenuItem(ActionWrapperSwing(NewFileAction(frame.application))))
	    menu.add(JMenuItem(ActionWrapperSwing(OpenFileAction(frame.application))))
        menu.add(openRecentMenu)
        menu.add(JMenuItem(ActionWrapperSwing(SaveFileAction(frame.application))))
	    menu.add(JMenuItem(ActionWrapperSwing(SaveFileAsAction(frame.application))))
	    menu.addSeparator()
	    menu.add(JMenuItem(ActionWrapperSwing(PreferencesAction())))
        menu.addSeparator()
        menu.add(JMenuItem(ActionWrapperSwing(CloseFileAction(frame.application))))
        menu.add(JMenuItem(ActionWrapperSwing(QuitApplicationAction(frame.application))))
    }

    protected open fun fillEditMenu(menu: JMenu) {
        menu.add(JMenuItem(ActionWrapperSwing(UndoAction())))
        menu.add(JMenuItem(ActionWrapperSwing(RedoAction())))
        menu.addSeparator()
        menu.add(JMenuItem(ActionWrapperSwing(DeleteAction())))
        menu.add(JMenuItem(ActionWrapperSwing(RotateAction())))
        menu.add(JMenuItem(ActionWrapperSwing(GroupComponentsAction())))
        menu.add(JMenuItem(ActionWrapperSwing(UngroupComponentsAction())))
        menu.addSeparator()
        menu.add(JMenuItem(ActionWrapperSwing(SelectAllAction())))
	    menu.add(JMenuItem(ActionWrapperSwing(SelectNextAction())))
	    menu.add(JMenuItem(ActionWrapperSwing(SelectPreviousAction())))
        menu.addSeparator()
        val arrangeMenu = JMenu(Translations.getString("edit.action.stackingOrder.name"))
        arrangeMenu.add(JMenuItem(ActionWrapperSwing(ToFrontAction())))
        arrangeMenu.add(JMenuItem(ActionWrapperSwing(OneUpAction())))
        arrangeMenu.add(JMenuItem(ActionWrapperSwing(OneDownAction())))
        arrangeMenu.add(JMenuItem(ActionWrapperSwing(ToBackAction())))
        menu.add(arrangeMenu)
    }

    protected open fun fillViewMenu(menu: JMenu) {
	    menu.add(JMenuItem(ActionWrapperSwing(CloseViewAction())))
	    menu.addSeparator()
        menu.add(JMenuItem(ActionWrapperSwing(ZoomInAction())))
        menu.add(JMenuItem(ActionWrapperSwing(ZoomNormalAction())))
        menu.add(JMenuItem(ActionWrapperSwing(ZoomOutAction())))
        menu.add(JMenuItem(ActionWrapperSwing(ZoomCenterAction())))
        menu.add(JMenuItem(ActionWrapperSwing(ZoomFitAction())))
        menu.addSeparator()
        menu.add(JCheckBoxMenuItem(ActionWrapperSwing(GridAction())))
    }

    private fun updateOpenRecentMenu() {
        openRecentMenu.removeAll()
        frame.application.mostRecentSavables.savables.forEach {
            if (it != frame.application.savable) {
	            openRecentMenu.add(JMenuItem(ActionWrapperSwing(OpenRecentFileAction(it, frame.application))))
            }
        }
        openRecentMenu.isEnabled = frame.application.mostRecentSavables.size > 0
    }
}