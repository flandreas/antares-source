package ch.scorpion.jabbah.app

import ch.scorpion.jabbah.app.action.*
import ch.scorpion.jabbah.base.ActionWrapperFx
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.view.*
import ch.scorpion.jabbah.edit.app.*
import javafx.scene.control.*

class MenuBarBuilderFx(
    private val application: DesktopApplication,
    private val eventBus: EventBus = BaseModule.eventBus
) {

    val menuBar = MenuBar()
    private val fileMenu = Menu(Translations.getString("application.menu.file"))
    private val editMenu = Menu(Translations.getString("application.menu.edit"))
    private val viewMenu = Menu(Translations.getString("application.menu.view"))
	private val openRecentMenu = Menu(Translations.getString("file.action.openRecent.name"))

    init {
        fillFileMenu(fileMenu)
        fillEditMenu(editMenu)
        fillViewMenu(viewMenu)
        fillMenuBar(menuBar)

	    eventBus.register(SavableHistoryEvent::class, { updateOpenRecentMenu() })
    }

    protected open fun fillMenuBar(menuBar: MenuBar) {
        menuBar.menus.add(fileMenu)
        menuBar.menus.add(editMenu)
        menuBar.menus.add(viewMenu)
    }

    protected open fun fillFileMenu(menu: Menu) {
        menu.items.add(ActionWrapperFx.wrap(MenuItem(), NewFileAction(application)))
	    menu.items.add(ActionWrapperFx.wrap(MenuItem(), OpenFileAction(application)))
	    menu.items.add(openRecentMenu)
	    menu.items.add(ActionWrapperFx.wrap(MenuItem(), SaveFileAction(application)))
	    menu.items.add(ActionWrapperFx.wrap(MenuItem(), SaveFileAsAction(application)))
	    menu.items.add(SeparatorMenuItem())
	    menu.items.add(ActionWrapperFx.wrap(MenuItem(), QuitApplicationAction(application)))
    }

    protected open fun fillEditMenu(menu: Menu) {
        menu.items.add(ActionWrapperFx.wrap(MenuItem(), UndoAction()))
        menu.items.add(ActionWrapperFx.wrap(MenuItem(), RedoAction()))
        menu.items.add(SeparatorMenuItem())
	    menu.items.add(ActionWrapperFx.wrap(MenuItem(), CutAction()))
	    menu.items.add(ActionWrapperFx.wrap(MenuItem(), CopyAction()))
	    menu.items.add(ActionWrapperFx.wrap(MenuItem(), PasteAction()))
	    menu.items.add(SeparatorMenuItem())
        menu.items.add(ActionWrapperFx.wrap(MenuItem(), DeleteAction()))
        menu.items.add(ActionWrapperFx.wrap(MenuItem(), RotateAction()))
	    menu.items.add(ActionWrapperFx.wrap(MenuItem(), GroupComponentsAction()))
	    menu.items.add(ActionWrapperFx.wrap(MenuItem(), UngroupComponentsAction()))
        menu.items.add(SeparatorMenuItem())
        menu.items.add(ActionWrapperFx.wrap(MenuItem(), SelectAllAction()))
        menu.items.add(SeparatorMenuItem())
        val arrangeMenu = Menu(Translations.getString("edit.action.stackingOrder.name"))
        arrangeMenu.items.add(ActionWrapperFx.wrap(MenuItem(), ToFrontAction()))
        arrangeMenu.items.add(ActionWrapperFx.wrap(MenuItem(), OneUpAction()))
        arrangeMenu.items.add(ActionWrapperFx.wrap(MenuItem(), OneDownAction()))
        arrangeMenu.items.add(ActionWrapperFx.wrap(MenuItem(), ToBackAction()))
        menu.items.add(arrangeMenu)
    }

    protected open fun fillViewMenu(menu: Menu) {
        menu.items.add(ActionWrapperFx.wrap(MenuItem(), ZoomInAction()))
        menu.items.add(ActionWrapperFx.wrap(MenuItem(), ZoomNormalAction()))
        menu.items.add(ActionWrapperFx.wrap(MenuItem(), ZoomOutAction()))
        menu.items.add(ActionWrapperFx.wrap(MenuItem(), ZoomCenterAction()))
        menu.items.add(ActionWrapperFx.wrap(MenuItem(), ZoomFitAction()))
        menu.items.add(SeparatorMenuItem())
        menu.items.add(ActionWrapperFx.wrap(CheckMenuItem(), GridAction()))
    }

	private fun updateOpenRecentMenu() {
		openRecentMenu.items.clear()
		application.mostRecentSavables.savables.forEach {
			openRecentMenu.items.add(ActionWrapperFx.wrap(MenuItem(), OpenRecentFileAction(it, application)))
		}
		openRecentMenu.isDisable = application.mostRecentSavables.size == 0
	}
}