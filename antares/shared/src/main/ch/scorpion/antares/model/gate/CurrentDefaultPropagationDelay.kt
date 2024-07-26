package ch.scorpion.antares.model.gate

import ch.scorpion.jabbah.base.LongValue
import ch.scorpion.jabbah.base.LongValueImpl
import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.graph.library.CurrentLibraryEvent
import ch.scorpion.jabbah.graph.library.LibraryPreferences

object CurrentDefaultPropagationDelay {

    /** The name of the [Long] property in [Properties] for the default gate propagation delay. */
    const val PROP_DEFAULT_PROPAGATION_DELAY = "antares.model.gate.defaultPropagationDelay"

    var value: LongValue = fromProperties
        private set

    private val fromProperties: LongValue get() =
        LongValueImpl(BaseModule.properties.getInt(PROP_DEFAULT_PROPAGATION_DELAY).toLong())

    init {
        BaseModule.eventBus.register(CurrentLibraryEvent::class) {
            value = LongValueImpl(LibraryPreferences.getInt(PROP_DEFAULT_PROPAGATION_DELAY).toLong())
        }
    }
}