package io.antarescircuit.jabbah.graph.library

import io.antarescircuit.jabbah.base.PreferencesChangedEvent
import io.antarescircuit.jabbah.base.module.BaseModule

/**
 * A property in [LibraryPreferences] that is kept updated when its value sources change.
 * Meant to be implemented by singletons holding the corresponding value.
 */
abstract class LibraryPreferencesProperty<T> {

    /** Editable mainly for unit tests. Besides that, the value is taken from the sources. */
    var value: T = fromProperties

    abstract val fromProperties: T
    abstract val fromLibraryPreferences: T

    init {
        BaseModule.eventBus.register(PreferencesChangedEvent::class) { value = fromLibraryPreferences }
        BaseModule.eventBus.register(CurrentLibraryEvent::class) { value = fromLibraryPreferences }
        BaseModule.eventBus.register(LibraryStoredEvent::class) { value = fromLibraryPreferences }
    }
}