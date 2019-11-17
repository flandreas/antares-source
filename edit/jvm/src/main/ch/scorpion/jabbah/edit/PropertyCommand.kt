package ch.scorpion.jabbah.edit

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.edit.command.AbstractCommand

/**
 * A [Command] that reflects the undoable change of an object's property.
 * @param V the type of the property's value
 */
class PropertyCommand<V>(
    editor: Editor,
    private val propertyBaseKey: String,
    getter: () -> V?,
    private val setter: (V?) -> Unit,
    private val newValue: V?
) : AbstractCommand("edit.command.property", editor) {

    private val oldValue = getter.invoke()

    override fun getDescription(): String {
        return Translations.getString("$propertyBaseKey.name")
    }

    override fun execute() {
        setter.invoke(newValue)
    }

    override fun undo() {
        setter.invoke(oldValue)
    }
}