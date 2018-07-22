package ch.scorpion.jabbah.edit.model.text

import ch.scorpion.jabbah.base.StringUtils

/**
 * Represents a property of a bean that contains more text than a simple [String] property.
 * Intended to be edited in a UI using a multi-line text area.
 */
data class TextProperty(val text: String? = null) {
	fun isEmpty(): Boolean = StringUtils.isEmpty(text)
	fun isNotEmpty(): Boolean = StringUtils.isNotEmpty(text)
}