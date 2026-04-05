package io.antarescircuit.antares.model.gate

import io.antarescircuit.jabbah.base.LongValue
import io.antarescircuit.jabbah.base.LongValueImpl
import io.antarescircuit.jabbah.base.Properties
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.graph.MetaGraph
import io.antarescircuit.jabbah.graph.library.Library
import io.antarescircuit.jabbah.graph.library.LibraryPreferences
import io.antarescircuit.jabbah.graph.library.LibraryPreferencesProperty

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