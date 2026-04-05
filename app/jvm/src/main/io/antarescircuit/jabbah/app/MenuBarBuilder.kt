package io.antarescircuit.jabbah.app

import io.antarescircuit.jabbah.app.action.*
import io.antarescircuit.jabbah.base.ActionWrapperSwing
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.preferences.PreferencesAction
import io.antarescircuit.jabbah.draw.view.*
import io.antarescircuit.jabbah.edit.app.*
import io.antarescircuit.jabbah.draw.view.find.FindAction
import org.apache.commons.lang3.SystemUtils
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

	companion object {
		fun createFileMenu(): JMenu = JMenu(Translations.getString("application.menu.file"))
		fun createEditMenu(): JMenu = JMenu(Translations.getString("application.menu.edit"))
		fun createViewMenu(): JMenu = JMenu(Translations.getString("application.menu.view"))
		fun createHelpMenu(): JMenu = JMenu(Translations.getString("application.menu.help"))

		fun addZoomActions(menu:JMenu) {
			menu.add(JMenuItem(ActionWrapperSwing(ZoomPanActions.zoomInAction)))
			menu.add(JMenuItem(ActionWrapperSwing(ZoomPanActions.zoomNormalAction)))
			menu.add(JMenuItem(ActionWrapperSwing(ZoomPanActions.zoomOutAction)))
			menu.add(JCheckBoxMenuItem(ActionWrapperSwing(ZoomPanActions.zoomCenterAction)))
			menu.add(JCheckBoxMenuItem(ActionWrapperSwing(ZoomPanActions.zoomFitAction)))
			menu.add(JCheckBoxMenuItem(ActionWrapperSwing(ZoomPanActions.zoomFitMaxNormalAction)))
		}
	}

    val menuBar = JMenuBar()
    private val fileMenu = createFileMenu()
    private val editMenu = createEditMenu()
    private val viewMenu = createViewMenu()
	private val helpMenu = createHelpMenu()
    protected val openRecentMenu by lazy { createOpenRecentMenu() }

    init {
        fillFileMenu(fileMenu)
        fillEditMenu(editMenu)

        fillViewMenu(viewMenu)
	    viewMenu.addSeparator()
	    viewMenu.add(JMenuItem(ActionWrapperSwing(CloseActiveViewAction())))

	    fillHelpMenu(helpMenu)
        fillMenuBar(menuBar)

	    // Make sure that "Help" menu is always the last one
	    menuBar.add(helpMenu)
    }

    protected open fun fillMenuBar(menuBar: JMenuBar) {
        menuBar.add(fileMenu)
        menuBar.add(editMenu)
        menuBar.add(viewMenu)
    }

    protected open fun fillFileMenu(menu: JMenu) {
	    if (!SystemUtils.IS_OS_MAC) {
		    menu.add(JMenuItem(ActionWrapperSwing(AboutAction(frame.application))))
		    menu.addSeparator()
	    }
	    menu.add(JMenuItem(ActionWrapperSwing(NewFileAction(frame.application))))
	    menu.add(JMenuItem(ActionWrapperSwing(OpenFileAction(frame.application))))
        menu.add(openRecentMenu)
        menu.add(JMenuItem(ActionWrapperSwing(frame.application.controller.saveAction)))
	    menu.add(JMenuItem(ActionWrapperSwing(SaveFileAsAction(frame.application))))
        menu.add(JMenuItem(ActionWrapperSwing(CloseFileAction(frame.application))))
	    menu.addSeparator()
	    menu.add(JMenuItem(ActionWrapperSwing(ExportLogfileAction(frame.application))))
	    if (!SystemUtils.IS_OS_MAC) {
		    menu.add(JMenuItem(ActionWrapperSwing(PreferencesAction())))
	        menu.addSeparator()
		    menu.add(JMenuItem(ActionWrapperSwing(QuitApplicationAction(frame.application))))
	    }
    }

    protected open fun fillEditMenu(menu: JMenu) {
        menu.add(JMenuItem(ActionWrapperSwing(UndoAction())))
        menu.add(JMenuItem(ActionWrapperSwing(RedoAction())))
        menu.addSeparator()
	    menu.add(JMenuItem(ActionWrapperSwing(FindAction())))
	    menu.addSeparator()
        menu.add(JMenuItem(ActionWrapperSwing(DeleteAction())))
        menu.add(JMenuItem(ActionWrapperSwing(RotateAction())))
        menu.add(JMenuItem(ActionWrapperSwing(RotateAction(clockwise = true))))
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
	    addZoomActions(menu)
        menu.addSeparator()
        menu.add(JCheckBoxMenuItem(ActionWrapperSwing(GridAction())))
    }

	protected open fun fillHelpMenu(menu: JMenu) {
		menu.add(JMenuItem(ActionWrapperSwing(HelpComponentAction("edit.action.helpSelected"))))
		menu.add(JMenuItem(ActionWrapperSwing(DocumentationAction(frame.application))))
		menu.add(JMenuItem(ActionWrapperSwing(YouTubeChannelAction(frame.application))))
		menu.add(JMenuItem(ActionWrapperSwing(IssuesAction(frame.application))))
	}

	protected open fun createOpenRecentMenu(): JMenu = OpenRecentMenu(frame.application, eventBus)
}