package ch.scorpion.jabbah.base

import java.util.*
import javax.swing.Action
import javax.swing.KeyStroke

/**
 * A base class for implementing actions.
 *
 * Names and other properties for graphical representations like menus and accelerators are derived from the
 * [AbstractAction]'s base name.
 */
abstract class AbstractAction(val baseName: String) : javax.swing.AbstractAction() {

    init {
        setName(Translations.getString(getNameKey()))
        setDescription(Translations.getOptionalString(getDescKey()))
        addAccelerator()
    }

    protected fun setName(name: String) {
        putValue(Action.NAME, name)
    }

    protected fun setDescription(desc: String?) {
        putValue(Action.SHORT_DESCRIPTION, desc)
    }

    protected fun getNameKey(): String {
        return "${baseName}.name"
    }

    protected fun getDescKey(): String {
        return "${baseName}.desc"
    }

    private fun getAcceleratorKey(): String {
        return "${baseName}.accelerator"
    }

    private fun addAccelerator() {
        try {
            val accelerator = Translations.getString(getAcceleratorKey())
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(accelerator))

        } catch (e: MissingResourceException) {
            // empty, no accelerator available, and not mandatory
        }
    }
}