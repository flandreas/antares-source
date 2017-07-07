package ch.scorpion.jabbah.edit.model.text

/**
 * Represents a property of a bean that contains more text than a simple [String] property.
 * Intended to be edited in a UI using a multi-line text area.
 */
data class TextProperty(val text: String?)