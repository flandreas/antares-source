package ch.scorpion.jabbah.app

import ch.scorpion.jabbah.io.Storable

/**
 * Posted on an [EventBus] when the current application data in an [Application] has changed.
 */
data class ApplicationDataEvent(val application: Application, val oldData: Storable?, val newData: Storable?)

/**
 * A request to close the specified application data [Storable]. The class that centrally manages application state
 * has to decide whether this is possible and allowed, in which case it clears the application state and
 * sends an [ApplicationDataEvent] with an empty `newData` value. All classes that display the current application
 * state then react to that and display an `empty` state.
 */
data class CloseApplicationDataRequest(val data: Storable)