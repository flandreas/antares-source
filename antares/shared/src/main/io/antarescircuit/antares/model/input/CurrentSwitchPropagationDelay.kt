package io.antarescircuit.antares.model.input

import io.antarescircuit.jabbah.base.LongValue
import io.antarescircuit.jabbah.base.LongValueImpl
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.graph.library.LibraryPreferences
import io.antarescircuit.jabbah.graph.library.LibraryPreferencesProperty

object CurrentSwitchPropagationDelay : LibraryPreferencesProperty<LongValue>() {

    const val PROP_DEFAULT_DELAY = "io.antarescircuit.antares.model.input.Switch.defaultPropDelay"

    override val fromProperties: LongValue get() =
        LongValueImpl(BaseModule.properties.getInt(PROP_DEFAULT_DELAY).toLong())

    override val fromLibraryPreferences: LongValue get() =
        LongValueImpl(LibraryPreferences.getInt(PROP_DEFAULT_DELAY).toLong())
}