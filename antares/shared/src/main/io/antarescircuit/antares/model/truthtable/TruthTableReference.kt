package io.antarescircuit.antares.model.truthtable

import io.antarescircuit.jabbah.app.ApplicationData
import io.antarescircuit.jabbah.app.ApplicationDataContentEvent
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.event.EventHandler
import io.antarescircuit.jabbah.base.module.BaseModule

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