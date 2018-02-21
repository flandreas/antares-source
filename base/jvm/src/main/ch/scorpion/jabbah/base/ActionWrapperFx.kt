package ch.scorpion.jabbah.base

import ch.scorpion.jabbah.base.event.PropertyChangeEvent
import ch.scorpion.jabbah.base.event.PropertyChangeListener
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.event.EventTarget
import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.input.KeyCombination

object ActionWrapperFx {

	enum class ButtonType {
		Text,
		Image,
		TextAndImage
	}

	private val wrappers = mutableListOf<Wrapper>()

	fun wrap(button: ButtonBase, action: ch.scorpion.jabbah.base.Action, type: ButtonType = ButtonType.Text): ButtonBase {
		wrappers.add(ButtonWrapper(button, action, type))
		return button
	}

	fun imageButton(button: ButtonBase, action: ch.scorpion.jabbah.base.Action): ButtonBase = wrap(button, action, ButtonType.Image)

	fun textButton(button: ButtonBase, action: ch.scorpion.jabbah.base.Action): ButtonBase = wrap(button, action, ButtonType.Text)

	fun wrap(menuItem: MenuItem, action: ch.scorpion.jabbah.base.Action): MenuItem {
		wrappers.add(MenuItemWrapper(menuItem, action))
		return menuItem
	}

	fun unwrap(button: Button) {
		wrappersOf(button).forEach {
			it.dispose()
			wrappers.remove(it)
		}
	}

	private fun wrappersOf(target: EventTarget): List<Wrapper> {
		return wrappers.filter { it.target == target }
	}

	private abstract class Wrapper(val target: EventTarget, private val action: ch.scorpion.jabbah.base.Action) : PropertyChangeListener<Any>, EventHandler<ActionEvent> {

		init {
			action.addPropertyChangeListener(this)
		}

		open fun dispose() {
			action.removePropertyChangeListener(this)
		}

		override fun handle(e: ActionEvent?) {
			action.execute(ch.scorpion.jabbah.base.event.ActionEvent(
				event = e,
				source = e!!.source,
				modifiers = 0,
				action = "action",
				time = System.SYSTEM!!.currentTimeMillis()
			))
		}
	}

	private class ButtonWrapper(val button: ButtonBase, private val action: ch.scorpion.jabbah.base.Action, private val type: ButtonType) : Wrapper(button, action) {

		init {
			button.isDisable = !action.enabled
			button.onAction = this
			update()
		}

		override fun dispose() {
			super.dispose()
			button.removeEventHandler(ActionEvent.ACTION, this)
		}

		override fun propertyChanged(e: PropertyChangeEvent<Any>) {
			when (e.name) {
				Action.PROP_NAME -> button.text = e.newValue as String
				Action.PROP_ENABLED -> button.isDisable = !(e.newValue as Boolean)
				Action.PROP_SELECTED -> if (button is ToggleButton) button.isSelected = e.newValue as Boolean
			}
		}

		private fun update() {
			button.graphic = if (type == ButtonType.Image || type == ButtonType.TextAndImage) {
				ImageView(Image(ActionWrapperFx::class.java.getResourceAsStream(action.imagePath)))
			} else null

			button.text = if (type == ButtonType.Text || type == ButtonType.TextAndImage) {
				action.name
			} else null
			if (button is ToggleButton) {
				button.isSelected = action.selected
			}

			button.tooltip = if (type == ButtonType.Image && StringUtils.isNotEmpty(action.name)) Tooltip(action.name) else null
		}
	}

	private class MenuItemWrapper(val menuItem: MenuItem, action: ch.scorpion.jabbah.base.Action) : Wrapper(menuItem, action) {

		init {
			menuItem.isDisable = !action.enabled
			menuItem.text = action.name
			menuItem.accelerator = getKeyCombination(action.accelerator)
			menuItem.onAction = this
			if (menuItem is CheckMenuItem) {
				menuItem.isSelected = action.selected
			}
		}

		override fun dispose() {
			super.dispose()
			menuItem.removeEventHandler(ActionEvent.ACTION, this)
		}

		override fun propertyChanged(e: PropertyChangeEvent<Any>) {
			when (e.name) {
				Action.PROP_NAME -> menuItem.text = e.newValue as String
				Action.PROP_ACCELERATOR -> menuItem.accelerator = getKeyCombination(e.newValue as String)
				Action.PROP_ENABLED -> menuItem.isDisable = !(e.newValue as Boolean)
				Action.PROP_SELECTED -> if (menuItem is CheckMenuItem) menuItem.isSelected = e.newValue as Boolean
			}
		}

		private fun getKeyCombination(accelerator: String?): KeyCombination? {
			if (StringUtils.isEmpty(accelerator)) {
				return null
			}
			return KeyCombination.valueOf(accelerator)
		}
	}
}