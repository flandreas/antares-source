package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.io.*
import ch.scorpion.jabbah.graph.MetaGraph

/**
 * Contains preferences that are supposed to be independent of the user's preferences
 * stored in [BaseModule.properties], so that [Libraries][Library] can be exported and
 * successfully be executed in other user's environments.
 *
 * These [LibraryPreferences] are stored within a [Library] as part of their persistent state.
 */
class LibraryPreferences : Properties(), Storable {

    /**
     * The getters in this object get the preference from the current [Library] of the [LibraryHolder],
     * or else from the environment's [Properties]. This distinction is mainly necessary to support unit test,
     * which don't always incorporate a [Library]. Productive use cases will always rely on
     * a [MetaGraph] in the current [Library], at least during execution.
     */
    companion object {

        fun getInt(name: String): Int =
            LibraryModule.libraryHolder.l?.preferences?.getInt(name)
                ?: BaseModule.properties.getInt(name)

        fun getString(name: String): String =
            LibraryModule.libraryHolder.l?.preferences?.getString(name)
                ?: BaseModule.properties.getString(name)
    }

    /**
     * Overwritten for backward compatibility. Existing [Libraries][Library] have empty
     * [LibraryPreferences], so fallback to the local user's preferences.
     */
    override fun getOptionalEntry(name: String): Entry? =
        super.getOptionalEntry(name) ?: BaseModule.properties.getOptionalEntry(name)

    /** ---- [Storable] */

    override var isReading: Boolean = false

    override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) {}

    override fun write(writer: StoreWriter) {
        val preferences = mutableMapOf<String, Storable>()
        for (key in getKeys()) {
            preferences[key] = StringStorable(getEntry(key).stringValue)
        }
        writer.writeMap("content", preferences)
    }

    override fun read(reader: StoreReader) {
        if (reader.hasElement("content")) {
            clear()
            val preferences = reader.readMap("content")
            preferences.forEach { (k, v) ->
                load(k, (v as StringStorable).content)
            }
        }
    }
}