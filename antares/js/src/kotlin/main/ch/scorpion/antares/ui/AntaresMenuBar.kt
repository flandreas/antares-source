package ch.scorpion.antares.ui

import ch.scorpion.jabbah.base.mreact.jmMenuItem
import ch.scorpion.jabbah.edit.app.RedoAction
import ch.scorpion.jabbah.edit.app.UndoAction
import com.ccfraser.muirwik.components.button.mButton
import com.ccfraser.muirwik.components.menu.MenuOnCloseReason
import com.ccfraser.muirwik.components.menu.mMenu
import kotlinx.css.Display
import kotlinx.css.display
import org.w3c.dom.Node
import org.w3c.dom.events.Event
import react.*
import react.dom.div
import styled.css
import styled.styledDiv

interface AntaresMenuBarJsProps : Props {

}

fun RBuilder.antaresMenuBar(handler: AntaresMenuBarJsProps.() -> Unit) {
	child(AntaresMenuBar::class) {
		this.attrs(handler)
	}
}

class AntaresMenuBar : RComponent<AntaresMenuBarJsProps , State>() {
	private var anchorElement: Node? = null
	private var selectedMenuIndex: Int? = null
	private val undoAction = UndoAction()
	private val redoAction = RedoAction()

	private fun handleShowMenuClick(event: Event, menuIndex: Int) {
		selectedMenuIndex = menuIndex
		val currentTarget = event.currentTarget
		setState { anchorElement = currentTarget.asDynamic() as? Node }
	}

	private fun handleSimpleClick() {
		setState {
			selectedMenuIndex = null
			anchorElement = null
		}
	}

	private fun handleOnClose(reason: MenuOnCloseReason) {
		setState { anchorElement = null; selectedMenuIndex = null }
	}

	override fun RBuilder.render() {
		styledDiv {
			css { display = Display.inlineFlex }
			mButton("File")
			mButton("Edit", onClick = { handleShowMenuClick(it, 2)})
			div {
				mMenu(selectedMenuIndex == 2, anchorElement = anchorElement, onClose = { _, reason -> handleOnClose(reason)}) {
					jmMenuItem {
						action = undoAction
						parentClickHandler = ::handleSimpleClick
					}
					jmMenuItem {
						action = redoAction
						parentClickHandler = ::handleSimpleClick
					}
				}
			}
			mButton("View")
			mButton("Simulation")
		}
	}
}