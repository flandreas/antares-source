package ch.scorpion.jabbah.graph.container.editsubgraph

import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.ActionWrapperSwing
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.draw.view.*
import ch.scorpion.jabbah.edit.app.*
import javax.swing.JCheckBoxMenuItem
import javax.swing.JMenu
import javax.swing.JMenuBar
import javax.swing.JMenuItem

class EditSubGraphVerticeViewMenu : JMenuBar() {

	private val actions = mutableListOf<Action>()

	init {
		fillMenuBar()
	}

	fun dispose() {
		actions.forEach { it.dispose() }
	}

	private fun fillMenuBar() {
		val editMenu = JMenu(Translations.getString("application.menu.edit"))
		editMenu.add(JMenuItem(ActionWrapperSwing(register(UndoAction()))))
		editMenu.add(JMenuItem(ActionWrapperSwing(register(RedoAction()))))
		editMenu.addSeparator()
		editMenu.add(JMenuItem(ActionWrapperSwing(register(DeleteAction()))))
		editMenu.add(JMenuItem(ActionWrapperSwing(register(RotateAction()))))
		editMenu.add(JMenuItem(ActionWrapperSwing(register(RotateAction(clockwise = true)))))
		editMenu.add(JMenuItem(ActionWrapperSwing(register(GroupComponentsAction()))))
		editMenu.add(JMenuItem(ActionWrapperSwing(register(UngroupComponentsAction()))))
		editMenu.addSeparator()
		editMenu.add(JMenuItem(ActionWrapperSwing(register(SelectAllAction()))))
		editMenu.addSeparator()
		val arrangeMenu = JMenu(Translations.getString("edit.action.stackingOrder.name"))
		arrangeMenu.add(JMenuItem(ActionWrapperSwing(register(ToFrontAction()))))
		arrangeMenu.add(JMenuItem(ActionWrapperSwing(register(OneUpAction()))))
		arrangeMenu.add(JMenuItem(ActionWrapperSwing(register(OneDownAction()))))
		arrangeMenu.add(JMenuItem(ActionWrapperSwing(register(ToBackAction()))))
		editMenu.add(arrangeMenu)
		editMenu.addSeparator()
		editMenu.add(JMenuItem(ActionWrapperSwing(register(CutAction()))))
		editMenu.add(JMenuItem(ActionWrapperSwing(register(CopyAction()))))
		editMenu.add(JMenuItem(ActionWrapperSwing(register(PasteAction()))))

		add(editMenu)

		val viewMenu = JMenu(Translations.getString("application.menu.view"))
		viewMenu.add(JMenuItem(ActionWrapperSwing(register(ZoomPanActions.zoomInAction))))
		viewMenu.add(JMenuItem(ActionWrapperSwing(register(ZoomPanActions.zoomNormalAction))))
		viewMenu.add(JMenuItem(ActionWrapperSwing(register(ZoomPanActions.zoomOutAction))))
		viewMenu.add(JCheckBoxMenuItem(ActionWrapperSwing(register(ZoomPanActions.zoomCenterAction))))
		viewMenu.add(JCheckBoxMenuItem(ActionWrapperSwing(register(ZoomPanActions.zoomFitAction))))
		viewMenu.add(JCheckBoxMenuItem(ActionWrapperSwing(register(ZoomPanActions.zoomFitMaxNormalAction))))
		viewMenu.addSeparator()
		viewMenu.add(JCheckBoxMenuItem(ActionWrapperSwing(register(GridAction()))))
		add(viewMenu)
	}

	private fun register(action: Action): Action {
		actions.add(action)
		return action
	}
}