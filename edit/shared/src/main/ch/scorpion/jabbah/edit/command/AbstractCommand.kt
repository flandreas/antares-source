package ch.scorpion.jabbah.edit.command

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.edit.Command
import ch.scorpion.jabbah.edit.Editor

/**
 * Abstract base implementation of the [Command] interface.
 */
abstract class AbstractCommand(
    descriptionKey: String,
    val editor: Editor? = null,
    override val changesApplicationData: Boolean = true
) : Command {

    private val _description = Translations.getString(descriptionKey)

    /** ---- [Command] interface */

    override fun getDescription(): String = _description

	override fun validate() {
        editor?.drawing?.validate()
    }

    override fun registered() {
        // empty
    }
}