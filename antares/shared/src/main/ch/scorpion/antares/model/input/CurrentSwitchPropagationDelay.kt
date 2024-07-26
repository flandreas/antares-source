package ch.scorpion.antares.model.input

import ch.scorpion.jabbah.base.LongValue
import ch.scorpion.jabbah.base.LongValueImpl
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.graph.library.CurrentLibraryEvent
import ch.scorpion.jabbah.graph.library.LibraryPreferences

object CurrentSwitchPropagationDelay {

    const val PROP_DEFAULT_DELAY = "ch.scorpion.antares.model.input.Switch.defaultPropDelay"

    var value: LongValue = fromProperties
        private set

    private val fromProperties: LongValue get() =
        LongValueImpl(BaseModule.properties.getInt(PROP_DEFAULT_DELAY).toLong())

    init {
        BaseModule.eventBus.register(CurrentLibraryEvent::class) {
            value = LongValueImpl(LibraryPreferences.getInt(PROP_DEFAULT_DELAY).toLong())
        }
    }
}