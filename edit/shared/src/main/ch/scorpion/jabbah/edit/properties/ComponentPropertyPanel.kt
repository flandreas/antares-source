package ch.scorpion.jabbah.edit.properties

import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.ui.AbstractUIController
import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.DrawableAdapter
import ch.scorpion.jabbah.draw.DrawableEvent
import ch.scorpion.jabbah.edit.*
import ch.scorpion.jabbah.edit.CommandEventType.REDO
import ch.scorpion.jabbah.edit.CommandEventType.UNDO

interface ComponentPropertyPanel : PropertyPanel

/**
 * Displays the properties of the currently selected [Component] and allows the user to edit them.
 */
class ComponentPropertyPanelController(
	editor: Editor,
	private val eventBus: EventBus = BaseModule.eventBus,
	private val properties: Properties = BaseModule.properties
) : AbstractPropertyPanelController<ComponentPropertyPanel>(editor) {

	companion object {
		const val PROP_MAX_MULTI_SELECT_COUNT = "ComponentPropertyPanelController.maxMultiSelectCount"
	}

	private val maxMultiSelectCount: Int by lazy { properties.getInt(PROP_MAX_MULTI_SELECT_COUNT) }

	private val selectionChangeHandler: EventHandler<SelectionChangeEvent> = { handle(it) }

	private val commandEventHandler: EventHandler<CommandEvent> = { handle(it) }

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
		eventBus.register(CommandEvent::class, commandEventHandler)
	}

	/** ---- [AbstractUIController] */

	override fun dispose() {
		super.dispose()
		eventBus.unregister(selectionChangeHandler)
		eventBus.unregister(commandEventHandler)
	}

	/** ---- [AbstractPropertyPanelController] */

	override val description: String
		get() = when (bean) {
			null -> ""
			is Component -> (bean as Component).type
			else -> bean.toString()
		}

	override val defaultBean: Any? get() = if (editor.active) editor.drawing else null

	override fun getDefinedDescription(description: String): String =
		if (bean is MultiSelection) {
			(bean as MultiSelection).let {
				if (it.commonType === it.selection.first()::class) {
					Translations.getString("edit.property.bean.multiSelect", description)
				} else {
					Translations.getString("edit.property.bean.multiVarious")
				}
			}
		} else {
			super.getDefinedDescription(description)
		}

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
		updateBeanFromCurrentSelection()
	}

	private fun updateBeanFromCurrentSelection() {
		bean = if (editor.view.selectionManager.selection.isEmpty()) {
			if (editor.active) {
				editor.view.drawing
			} else {
				null
			}
		} else {
			getSelectedComponent()
		}
	}

	private fun handle(event: CommandEvent) {
		if (event.commandManager !== editor.commandManager) {
			return
		}

		if (event.type != UNDO && event.type != REDO) {
			return
		}
		handleBeanChanged(bean)
	}

	private fun getSelectedComponent(): Component? {
		val currentSelection = editor.view.selectionManager.selection
		return when (currentSelection.size) {
			0 -> null
			1 -> currentSelection.first()
			else -> possibleMultiSelection(currentSelection)
		}
	}

	private fun possibleMultiSelection(components: Collection<Component>): MultiSelection? {
		require(components.size > 1)

		if (components.size > maxMultiSelectCount) {
			return null
		}

		// Optimization: Avoid expensive common superclass calculation if all have the same type
		if (components.map { it.propertyOwner }.all { it::class === components.first()::class }) {
			return MultiSelection(components, components.first().propertyOwner::class)
		}

		return System.commonSuperClass(components.map { it.propertyOwner::class })?.let {
			MultiSelection(components, it)
		}
	}

	private inner class PropertyListener : DrawableAdapter() {
		override fun drawableUpdated(event: DrawableEvent) {
			refresh()
		}
	}
}