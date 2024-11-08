package ch.scorpion.antares.model.input

import ch.scorpion.jabbah.base.LongValue
import ch.scorpion.jabbah.base.LongValueImpl
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.graph.library.LibraryPreferences
import ch.scorpion.jabbah.graph.library.LibraryPreferencesProperty

object CurrentSwitchPropagationDelay : LibraryPreferencesProperty<LongValue>() {

    const val PROP_DEFAULT_DELAY = "ch.scorpion.antares.model.input.Switch.defaultPropDelay"

    override val fromProperties: LongValue get() =
        LongValueImpl(BaseModule.properties.getInt(PROP_DEFAULT_DELAY).toLong())

    override val fromLibraryPreferences: LongValue get() =
        LongValueImpl(LibraryPreferences.getInt(PROP_DEFAULT_DELAY).toLong())
}