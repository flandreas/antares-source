package ch.scorpion.antares.model.gate

import ch.scorpion.jabbah.base.LongValue
import ch.scorpion.jabbah.base.LongValueImpl
import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.library.Library
import ch.scorpion.jabbah.graph.library.LibraryPreferences
import ch.scorpion.jabbah.graph.library.LibraryPreferencesProperty

/**
 * Singleton object to avoid creating thousands of objects when [Libraries][Library]
 * or [MetaGraph]s are loaded.
 */
object CurrentDefaultPropagationDelay : LibraryPreferencesProperty<LongValue>() {

    /** The name of the [Long] property in [Properties] for the default gate propagation delay. */
    const val PROP_DEFAULT_PROPAGATION_DELAY = "antares.model.gate.defaultPropagationDelay"

    override val fromProperties: LongValue get() =
        LongValueImpl(BaseModule.properties.getInt(PROP_DEFAULT_PROPAGATION_DELAY).toLong())

    override val fromLibraryPreferences: LongValue get() =
        LongValueImpl(LibraryPreferences.getInt(PROP_DEFAULT_PROPAGATION_DELAY).toLong())
}