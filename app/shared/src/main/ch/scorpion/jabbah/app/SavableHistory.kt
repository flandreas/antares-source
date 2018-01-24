package ch.scorpion.jabbah.app

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule

/**
 * Maintains a list of the most recently saved [Savable]s in order to be able to re-open the quickly.
 * Posts a [SavableHistoryEvent] whenever its state has changed.
 */
class SavableHistory(
        private val maxSize: Int = MAX_SIZE,
        private val eventBus: EventBus = BaseModule.eventBus
) {

    companion object {
        private val MAX_SIZE = 4
    }

    /**
     * Returns the registered [Savable]s in "most recent" order, i.e the most recently registered [Savable]
     * is returned as the first one in the [List].
     */
    val savables: List<Savable> get() = _savables

    val size: Int get() = savables.size

    private val _savables = mutableListOf<Savable>()

    fun register(savable: Savable) {
        _savables.remove(savable)
        _savables.add(0, savable)
        limitToMaxSize()
        eventBus.post(SavableHistoryEvent(this))
    }

    private fun limitToMaxSize() {
        while (size > maxSize) {
            _savables.removeAt(maxSize)
        }
    }
}

/** Posted by [SavableHistory] when its state has changed.*/
data class SavableHistoryEvent(val savableHistory: SavableHistory)