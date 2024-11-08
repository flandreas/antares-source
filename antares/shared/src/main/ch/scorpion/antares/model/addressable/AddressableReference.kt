package ch.scorpion.antares.model.addressable

import ch.scorpion.jabbah.app.ApplicationData
import ch.scorpion.jabbah.app.ApplicationDataContentEstablishedEvent
import ch.scorpion.jabbah.app.ApplicationDataContentEvent
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.model.GraphElementListener
import ch.scorpion.jabbah.graph.model.vertice.VerticeLink
import ch.scorpion.jabbah.graph.view.GraphView

/**
 * Encapsulates a reference to an [Addressable] and updates the instance reference
 * whenever [ApplicationData] content has changed due to an undo/redo snapshot exchange.
 */
class AddressableReference(
	val link: VerticeLink,
	val view: DrawingView<GraphView>,
	private val eventBus: EventBus = BaseModule.eventBus
) {

	lateinit var addressable: Addressable
		private set

	private val applicationDataContentHandler: EventHandler<ApplicationDataContentEvent> = { unregisterOldContent() }

	private val applicationDataEstablishedHandler: EventHandler<ApplicationDataContentEstablishedEvent> = {
		registerNewContent((it.data.content as MetaGraph).graph.model!!)
	}

	private val dataListeners = mutableListOf<AddressableDataListener>()

	private val graphElementListeners = mutableListOf<GraphElementListener>()

	init {
		registerNewContent(view.drawing.graph!!)
		eventBus.register(ApplicationDataContentEvent::class, applicationDataContentHandler)
		eventBus.register(ApplicationDataContentEstablishedEvent::class, applicationDataEstablishedHandler)
	}

	fun dispose() {
		eventBus.unregister(applicationDataContentHandler)
		eventBus.unregister(applicationDataEstablishedHandler)
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

	private fun unregisterOldContent() {
		dataListeners.forEach { addressable.removeDataListener(it) }
		graphElementListeners.forEach { addressable.removeGraphElementListener(it) }
	}

	private fun registerNewContent(graph: Graph) {
		addressable = link.getLinkedVertice(graph) as Addressable
		graphElementListeners.forEach { addressable.addGraphElementListener(it) }
		dataListeners.forEach { addressable.addDataListener(it) }
	}
}