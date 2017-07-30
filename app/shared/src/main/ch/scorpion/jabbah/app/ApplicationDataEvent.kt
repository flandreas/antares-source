package ch.scorpion.jabbah.app

import ch.scorpion.jabbah.io.Storable

/**
 * Posted on an [EventBus] when the current application data in an [Application] has changed.
 */
data class ApplicationDataEvent(val application: Application, val oldData: Storable?, val newData: Storable?)