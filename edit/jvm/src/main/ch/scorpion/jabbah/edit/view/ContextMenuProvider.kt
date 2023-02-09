package ch.scorpion.jabbah.edit.view

import ch.scorpion.jabbah.base.ActionWrapperSwing
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.draw.View
import ch.scorpion.jabbah.draw.view.ContextMenuProvider
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.app.*
import javax.swing.JMenu
import javax.swing.JPopupMenu


open class EditContextMenuProvider : ContextMenuProvider {

	companion object {
		private val cutAction by lazy { ActionWrapperSwing(CutAction()) }
		private val copyAction by lazy { ActionWrapperSwing(CopyAction()) }
		private val duplicateAction by lazy { ActionWrapperSwing(DuplicateAction()) }
		private val deleteAction by lazy { ActionWrapperSwing(DeleteAction()) }
		private val rotateAction by lazy { ActionWrapperSwing(RotateAction()) }
		private val toFrontAction by lazy { ActionWrapperSwing(ToFrontAction()) }
		private val toBackAction by lazy { ActionWrapperSwing(ToBackAction()) }
		private val oneDownAction by lazy { ActionWrapperSwing(OneDownAction()) }
		private val oneUpAction by lazy { ActionWrapperSwing(OneUpAction()) }
	}

	override var applicationName: String = ""

	override fun fillContextMenu(view: View<*>, x: Double, y: Double, menu: JPopupMenu) {
		fillContextMenu(view, (view as DrawingView<*>).selectionManager.selection, menu)
	}

	private fun fillContextMenu(view: View<*>, selection: Collection<Component>, popupMenu: JPopupMenu) {
		popupMenu.removeAll()
		if (selection.size == 1) {
			addActions(view, popupMenu)
		}
	}

	protected open fun addClipboardActions(popupMenu: JPopupMenu) {
		popupMenu.add(cutAction)
		popupMenu.add(copyAction)
		popupMenu.add(duplicateAction)
	}

	protected open fun addActions(view: View<*>, popupMenu: JPopupMenu) {
		addClipboardActions(popupMenu)
		popupMenu.addSeparator()
		popupMenu.add(deleteAction)
		popupMenu.add(rotateAction)
		val arrangeMenu = JMenu(Translations.getString("edit.action.stackingOrder.name"))
		arrangeMenu.add(toFrontAction)
		arrangeMenu.add(oneUpAction)
		arrangeMenu.add(oneDownAction)
		arrangeMenu.add(toBackAction)
		popupMenu.add(arrangeMenu)
	}
}