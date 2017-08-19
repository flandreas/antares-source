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
        description: String?,
        accelerator: String?
) : javax.swing.AbstractAction() {

    constructor(baseName: String): this(
            Translations.getString("$baseName.name"),
            Translations.getOptionalString("$baseName.desc"),
            Translations.getOptionalString("$baseName.accelerator")
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