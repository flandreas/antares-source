package ch.scorpion.jabbah.edit.ui

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.ui.AbstractUIController
import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.DrawableAdapter
import ch.scorpion.jabbah.draw.DrawableEvent
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.SelectionChangeEvent

interface ComponentPropertyPanel : PropertyPanel

/**
 * Displays the properties of the currently selected [Component] and allows the user to edit them.
 */
class ComponentPropertyPanelController(
	editor: Editor,
	private val eventBus: EventBus = BaseModule.eventBus
) : AbstractPropertyPanelController<ComponentPropertyPanel>(editor) {

	private val selectionChangeHandler: EventHandler<SelectionChangeEvent> = { handle(it) }

	/**
	 * Listens for property changes of the currently selected [Component] that are NOT initiated by this
	 * [ComponentPropertyPanel], such as rotations requested by keyboard or menu interactions, in order
	 * to update the contents of the [ComponentPropertyPanel] and avoiding to set outdated property values
	 * the next time the user changes any other properties.
	 *
	 * Ideally, this mechanisms would use regular PropertyEvents from editable [Component] properties,
	 * but such a mechanism doesn't (yet) exist, so for the moment we use [DrawableEvent]s, assuming that
	 * all relevant property changes will result in [Drawable.update].
	 */
	private val propertyListener = PropertyListener()

	init {
		eventBus.register(SelectionChangeEvent::class, selectionChangeHandler)
	}

	/** ---- [AbstractUIController] */

	override fun dispose() {
		super.dispose()
		eventBus.unregister(selectionChangeHandler)
	}

	/** ---- [AbstractPropertyPanelController] */

	override val description: String
		get() = when (bean) {
			null -> ""
			is Component -> (bean as Component).type
			else -> bean.toString()
		}

	override val defaultBean: Any? get() = if (editor.active) editor.drawing else null

	/** ---- [ComponentPropertyPanelController] */

	override fun handleBeanChangedHandler(oldValue: Any?) {
		oldValue?.let {
			if (it is Component) {
				it.removeDrawableListener(propertyListener)
			}
			if (bean is Component) {
				(bean as Drawable).addDrawableListener(propertyListener)
			}
		}
	}

	private fun handle(event: SelectionChangeEvent) {
		if (event.view !== editor.view) {
			return
		}

		bean = if (event.type !== SelectionChangeEvent.Type.SELECTED) {
			if (editor.active) {
				editor.view.drawing
			} else {
				null
			}
		} else {
			getSelectedComponent(event)
		}
	}

	private fun getSelectedComponent(event: SelectionChangeEvent): Component? {
		if (event.components.size == 1) {
			return event.components.iterator().next()
		}
		return null
	}

	private inner class PropertyListener : DrawableAdapter() {
		override fun drawableUpdated(event: DrawableEvent) {
			refresh()
		}
	}
}