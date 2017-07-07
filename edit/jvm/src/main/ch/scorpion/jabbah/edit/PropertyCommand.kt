package ch.scorpion.jabbah.edit

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.edit.command.AbstractCommand
import kotlin.reflect.jvm.internal.impl.descriptors.ModuleDescriptor.DefaultImpls.accept



/**
 * A {@link Command} that reflects the undoable change of an {@link Object}'s property.
 * @param V the type of the property's value
 */
class PropertyCommand<V>(
    editor: Editor,
    private val propertyBaseKey: String,
    private val getter: () -> V?,
    private val setter: (V?) -> Unit,
    private val newValue: V?
) : AbstractCommand("edit.command.property", editor) {

    private val oldValue = getter.invoke()

    override fun getDescription(): String {
        return Translations.getString(propertyBaseKey + ".name")
    }

    override fun execute() {
        setter.invoke(newValue)
    }

    override fun undo() {
        setter.invoke(oldValue)
    }
}