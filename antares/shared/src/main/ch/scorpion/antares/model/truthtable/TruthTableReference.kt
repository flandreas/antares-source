package ch.scorpion.antares.model.truthtable

import ch.scorpion.jabbah.app.ApplicationData
import ch.scorpion.jabbah.app.ApplicationDataContentEvent
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.module.BaseModule

/**
 * Encapsulates a reference to a [TruthTable] and updates the instance references
 * whenever the [ApplicationData] changes due recovering from undoable snapshots.
 */
class TruthTableReference(
	private val truthTableProvider: () -> TruthTable,
	private val eventBus: EventBus = BaseModule.eventBus
) {

	lateinit var truthTable: TruthTable

	private val applicationDataContentHandler: EventHandler<ApplicationDataContentEvent> = { updateReference() }

	private val dataListeners = mutableListOf<TruthTableListener>()

	init {
		updateReference()
		eventBus.register(ApplicationDataContentEvent::class, applicationDataContentHandler)
	}

	fun dispose() {
		eventBus.unregister(applicationDataContentHandler)
		dataListeners.forEach { truthTable.removeListener(it) }
	}

	fun addDataListener(l: TruthTableListener) {
		if (!dataListeners.contains(l)) {
			dataListeners.add(l)
			truthTable.addListener(l)
		}
	}

	fun removeDataListener(l: TruthTableListener) {
		dataListeners.remove(l)
		truthTable.removeListener(l)
	}

	private fun updateReference() {
		dataListeners.forEach { truthTable.removeListener(it) }
		truthTable = truthTableProvider()
		dataListeners.forEach { truthTable.addListener(it) }
	}
}