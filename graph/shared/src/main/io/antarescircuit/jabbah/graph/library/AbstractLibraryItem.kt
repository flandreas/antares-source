package io.antarescircuit.jabbah.graph.library

import io.antarescircuit.jabbah.edit.model.text.TranslatableText
import io.antarescircuit.jabbah.edit.model.text.description.Name
import io.antarescircuit.jabbah.edit.model.text.description.observableName
import io.antarescircuit.jabbah.io.AbstractStorable

/**
 * Abstract base implementation of the [LibraryItem] interface
 */
abstract class AbstractLibraryItem(
	initialName: TranslatableText = TranslatableText(),
	override val iconPath: String? = null,
) : AbstractStorable(), LibraryItem {

	override var name: Name by observableName(Name(initialName))

    private var _library: Library? = null

    override val library: Library? get() = _library

    override fun bindTo(library: Library) {
        _library = library
    }

    override fun dispose() {
        _library = null
    }

	/** ---- [Any] */

	override fun toString(): String = name.value
}