package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule

/**
 * Holds the one and only [Library].
 * TODO Make sure that the current Library is never null.
 */
class LibraryHolder(
	l: Library? = null,
	private val eventBus: EventBus = BaseModule.eventBus
) {

    var l: Library? = l
        set(value) {
	        if (field != value) {
		        field?.dispose()
		        field = value
		        eventBus.post(LibraryEvent(field!!))
	        }
        }

    val library: Library get() = l!!
}

/** Posted on [EventBus] when the current [Library] has changed.*/
data class LibraryEvent(val library: Library)