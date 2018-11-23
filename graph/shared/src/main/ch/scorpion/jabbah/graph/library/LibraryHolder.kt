package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule

/**
 * Holds the one and only [Library].
 * TODO Make sure that the current Library is never null.
 */
class LibraryHolder(
	l: Library? = null,
	private val eventBus: EventBus = BaseModule.eventBus
) {

	companion object {
		private val LOG by logger(LibraryHolder::class)
	}

    var l: Library? = l
        set(value) {
	        if (field != value) {
		        LOG.debug("LibraryHolder: setting current Library to '${value?.name}'")
		        val oldValue = field
		        field?.dispose()
		        field = value
		        if (oldValue != null) {
			        eventBus.post(CurrentLibraryEvent(field!!))
		        }
	        }
        }

    val library: Library get() = l!!
}

/** Posted on [EventBus] when the current [Library] has changed.*/
data class CurrentLibraryEvent(val library: Library)