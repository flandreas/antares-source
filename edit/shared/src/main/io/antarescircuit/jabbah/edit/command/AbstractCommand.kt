package io.antarescircuit.jabbah.edit.command

import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.edit.Command
import io.antarescircuit.jabbah.edit.Editor

/**
 * Abstract base implementation of the [Command] interface.
 */
abstract class AbstractCommand(
    descriptionKey: String,
    val editor: Editor? = null
) : Command {

    private val _description = Translations.getString(descriptionKey)

	private val tags: MutableSet<String> by lazy { mutableSetOf() }

    @Suppress("UNCHECKED_CAST")
    fun <T> castedView(): T = editor!!.view as T

    /** ---- [Command] interface */

    override fun getDescription(): String = _description

	override fun validate() {
        editor?.drawing?.validate()
    }

	override fun setTags(vararg names: String) {
		tags.addAll(names)
	}

	override fun hasTag(name: String): Boolean =
		tags.contains(name)
}