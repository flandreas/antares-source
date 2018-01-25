package ch.scorpion.jabbah.base

import javax.swing.Action
import javax.swing.KeyStroke

/**
 * A base class for implementing actions.
 *
 * Names and other properties for graphical representations like menus and accelerators can be derived from the
 * [AbstractAction]'s base name.
 */
abstract class AbstractAction(
        name: String,
        description: String? = null,
        accelerator: String? = null
) : javax.swing.AbstractAction() {

    companion object {
        protected fun translatedName(baseName: String): String = Translations.getString("$baseName.name")
        protected fun translatedDesc(baseName: String): String? = Translations.getOptionalString("$baseName.desc")
        protected fun translatedAccelerator(baseName: String): String? = Translations.getOptionalString("$baseName.accelerator")
    }

    constructor(baseName: String): this(
            translatedName(baseName),
            translatedDesc(baseName),
            translatedAccelerator(baseName)
    )

    var name: String
        get() = getValue(Action.NAME) as String
        set(value) {
            putValue(Action.NAME, value)
        }

    var description: String?
        get() = getValue(Action.SHORT_DESCRIPTION) as String?
        set(value) {
            putValue(Action.SHORT_DESCRIPTION, value)
        }

    var accelerator: String?
        get() = (getValue(Action.ACCELERATOR_KEY) as KeyStroke?)?.toString()
        set(value) {
            if (value != null) {
                putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(value))
            }
        }

    init {
        this.name = name
        this.description = description
        this.accelerator = accelerator
    }
}