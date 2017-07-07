package ch.scorpion.jabbah.edit.style

import ch.scorpion.jabbah.draw.style.StyleType

/**
 * Defines [StyleType]s of the [ch.scorpion.jabbah.edit] package.
 */
open class EditStyleType(name: String, descriptionKey: String) : StyleType(name, descriptionKey) {

    companion object {
        val HIGHLIGHT = EditStyleType("highlight", "edit.styleType.highlight.name")
        val MESSAGE = EditStyleType("message", "edit.styleType.message.name")
    }
}