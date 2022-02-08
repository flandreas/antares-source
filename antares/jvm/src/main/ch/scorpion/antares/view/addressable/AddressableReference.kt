package ch.scorpion.antares.view.addressable

import ch.scorpion.antares.model.addressable.Addressable
import ch.scorpion.antares.model.addressable.AddressableDataListener
import ch.scorpion.jabbah.app.ApplicationData
import ch.scorpion.jabbah.app.ApplicationDataContentEvent
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.graph.model.GraphElementListener
import ch.scorpion.jabbah.graph.view.GraphView

/**
 * Encapsulates a reference to an [Addressable] and updates the instance reference
 * whenever [ApplicationData] content has changed due to an undo/redo snapshot exchange.
 */
class AddressableReference(
	val id: Int,
	private val view: DrawingView<GraphView>,
	private val eventBus: EventBus = BaseModule.eventBus
) {

	lateinit var addressable: Addressable
		private set

	private val applicationDataContentHandler: EventHandler<ApplicationDataContentEvent> = { updateAddressable() }

	private val dataListeners = mutableListOf<AddressableDataListener>()

	private val graphElementListeners = mutableListOf<GraphElementListener>()

	init {
		updateAddressable()
		eventBus.register(ApplicationDataContentEvent::class, applicationDataContentHandler)
	}

	fun dispose() {
		eventBus.unregister(applicationDataContentHandler)
		dataListeners.forEach { addressable.removeDataListener(it) }
	}

	fun addDataListener(l: AddressableDataListener) {
		if (!dataListeners.contains(l)) {
			dataListeners.add(l)
			addressable.addDataListener(l)
		}
	}

	fun removeDataListener(l: AddressableDataListener) {
		dataListeners.remove(l)
		addressable.removeDataListener(l)
	}

	fun addGraphElementListener(l: GraphElementListener) {
		if (!graphElementListeners.contains(l)) {
			graphElementListeners.add(l)
			addressable.addGraphElementListener(l)
		}
	}

	fun removeGraphElementListener(l: GraphElementListener) {
		graphElementListeners.remove(l)
		addressable.removeGraphElementListener(l)
	}

	private fun updateAddressable() {
		dataListeners.forEach { addressable.removeDataListener(it) }
		graphElementListeners.forEach { addressable.removeGraphElementListener(it) }

		addressable = view.drawing.graph!!.withId(id) as Addressable

		graphElementListeners.forEach { addressable.addGraphElementListener(it) }
		dataListeners.forEach { addressable.addDataListener(it) }
	}
}