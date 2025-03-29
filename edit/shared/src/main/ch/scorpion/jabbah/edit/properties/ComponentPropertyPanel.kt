package ch.scorpion.jabbah.edit.properties

import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.ui.AbstractUIController
import ch.scorpion.jabbah.edit.*
import ch.scorpion.jabbah.edit.CommandEventType.*

interface ComponentPropertyPanel : PropertyPanel

/**
 * Displays the properties of the currently selected [Component] and allows the user to edit them.
 * If there is no selection, it displays the current application data property bean as [defaultBean].
 *
 *  * @param currentEditorEventFilter determines how this [ComponentPropertyPanelController] should react to
 *  * [CurrentEditorEvent]s, if at all.

 */
open class ComponentPropertyPanelController(
	editor: Editor,
	eventBus: EventBus = BaseModule.eventBus,
	currentEditorEventFilter: ((CurrentEditorEvent) -> Boolean)? = null,
	private val properties: Properties = BaseModule.properties
) : AbstractPropertyPanelController<ComponentPropertyPanel>(editor, eventBus, currentEditorEventFilter) {

	companion object {
		const val PROP_MAX_MULTI_SELECT_COUNT = "ComponentPropertyPanelController.maxMultiSelectCount"
	}

	private val maxMultiSelectCount: Int by lazy { properties.getInt(PROP_MAX_MULTI_SELECT_COUNT) }

	private val selectionChangeHandler: EventHandler<SelectionChangeEvent> = { handle(it) }

	private val commandEventHandler: EventHandler<CommandEvent> = { handle(it) }

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
				// Cannot use commonType, because there might be different types implemented by the same class
				if (it.selection.all { component -> component.type == it.type }) {
					Translations.getString("edit.property.bean.multiSelect", description)
				} else {
					Translations.getString("edit.property.bean.multiVarious")
				}
			}
		} else {
			super.getDefinedDescription(description)
		}

	override fun updateTitle() {
		if (bean == null) {
			if (editor.view.selectionManager.selectionCount > 0) {
				title = Translations.getString("edit.property.bean.multiTooMany", maxMultiSelectCount)
			} else {
				super.updateTitle()
			}
		} else {
			super.updateTitle()
		}
	}

	/** ---- [ComponentPropertyPanelController] */

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

		when (event.type) {
			UNDO, REDO -> {
				handleBeanChanged(bean)
			}
			COMMIT_TRANSACTION -> {
				// Update to reflect changes not caused by this panel (e.g. keyboard/mouse interaction)
				refresh()
			}
			else -> {}
		}
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
		// Cannot use clazz comparison, because there might be different types implemented by the same class
		if (components.map { it.propertyOwner }.all { it.type == components.first().type }) {
			return MultiSelection(components, components.first().propertyOwner::class)
		}

		return System.commonSuperClass(components.map { it.propertyOwner::class })?.let {
			MultiSelection(components, it)
		}
	}
}