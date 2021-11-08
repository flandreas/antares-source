package ch.scorpion.jabbah.edit.style

import ch.scorpion.jabbah.draw.style.StyleType

/**
 * Defines [StyleType]s of the [ch.scorpion.jabbah.edit] package.
 */
open class EditStyleType(
	name: String,
	descriptionKey: String,
	isSystem: Boolean = false,
	isBackdrop: Boolean = false
) : StyleType(name, descriptionKey, isSystem, isBackdrop) {

    companion object {
	    val SELECTION = EditStyleType("selection", "edit.styleType.selection.name", true)
        val HIGHLIGHT = EditStyleType("highlight", "edit.styleType.highlight.name", true)
        val MESSAGE_INFO = EditStyleType("message-info", "edit.styleType.message-info.name", true)
	    val MESSAGE_ERROR = EditStyleType("message-error", "edit.styleType.message-error.name", true)
    }
}