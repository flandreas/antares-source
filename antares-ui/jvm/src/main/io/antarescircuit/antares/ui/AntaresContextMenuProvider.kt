package io.antarescircuit.antares.ui

import io.antarescircuit.antares.model.gate.NonUnaryLogicGateType
import io.antarescircuit.antares.model.gate.UnaryLogicGateType
import io.antarescircuit.antares.view.gate.ChangeLogicGateTypeAction
import io.antarescircuit.antares.view.gate.LogicGateView
import io.antarescircuit.jabbah.app.Application
import io.antarescircuit.jabbah.base.ActionWrapperSwing
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.draw.View
import io.antarescircuit.jabbah.edit.DrawingView
import io.antarescircuit.jabbah.graph.ui.GraphContextMenuProvider
import javax.swing.JMenu
import javax.swing.JMenuItem
import javax.swing.JPopupMenu

class AntaresContextMenuProvider(
	application: Application
) : GraphContextMenuProvider(application) {

	companion object {

		private val unaryChangeLogicGateTypeActions = lazy {
			UnaryLogicGateType.values().map { ActionWrapperSwing(ChangeLogicGateTypeAction(it)) }
		}

		private val nonUnaryChangeLogicGateTypeActions = lazy {
			NonUnaryLogicGateType.values().map { ActionWrapperSwing(ChangeLogicGateTypeAction(it)) }
		}

		private val unaryChangeLogicGateTypeMenu = lazy {
			JMenu(Translations.getString("antares.action.changeLogicGateType.name")).also { menu ->
				unaryChangeLogicGateTypeActions.value.forEach { action ->
					menu.add(JMenuItem(action))
				}
			}
		}

		private val nonUnaryChangeLogicGateTypeMenu = lazy {
			JMenu(Translations.getString("antares.action.changeLogicGateType.name")).also { menu ->
				nonUnaryChangeLogicGateTypeActions.value.forEach { action ->
					menu.add(JMenuItem(action))
				}
			}
		}
	}

	override fun addApplicationSpecificActions(view: View<*>, popupMenu: JPopupMenu) {
		val selection = (view as DrawingView<*,*>).selectionManager.selection

		if (selection.all { it is LogicGateView && it.model.gateType is NonUnaryLogicGateType }) {
			popupMenu.addSeparator()
			popupMenu.add(nonUnaryChangeLogicGateTypeMenu.value)
		} else if (selection.all { it is LogicGateView && it.model.gateType is UnaryLogicGateType }) {
			popupMenu.addSeparator()
			popupMenu.add(unaryChangeLogicGateTypeMenu.value)
		}
	}
}