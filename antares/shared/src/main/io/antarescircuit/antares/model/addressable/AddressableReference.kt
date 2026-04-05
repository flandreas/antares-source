package io.antarescircuit.antares.model.addressable

import io.antarescircuit.jabbah.app.ApplicationData
import io.antarescircuit.jabbah.app.ApplicationDataContentEstablishedEvent
import io.antarescircuit.jabbah.app.ApplicationDataContentEvent
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.event.EventHandler
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.edit.DrawingView
import io.antarescircuit.jabbah.graph.MetaGraph
import io.antarescircuit.jabbah.graph.model.Graph
import io.antarescircuit.jabbah.graph.model.vertice.ObjectLink
import io.antarescircuit.jabbah.graph.view.GraphView

/**
 * Encapsulates a reference to an [Addressable] and updates the instance reference
 * whenever [ApplicationData] content has changed due to an undo/redo snapshot exchange.
 */
class AddressableReference(
	val link: ObjectLink<Addressable>,
	val view: DrawingView<GraphView>?,
	private val eventBus: EventBus = BaseModule.eventBus
) {

	lateinit var addressable: Addressable
		private set

	private val applicationDataContentHandler: EventHandler<ApplicationDataContentEvent> = { unregisterOldContent() }

	private val applicationDataEstablishedHandler: EventHandler<ApplicationDataContentEstablishedEvent> = {
		registerNewContent((it.data.content as MetaGraph).graph.model!!)
	}

	private val addressableListeners = mutableListOf<AddressableListener>()

	init {
		registerNewContent(view?.drawing?.graph)
		eventBus.register(ApplicationDataContentEvent::class, applicationDataContentHandler)
		eventBus.register(ApplicationDataContentEstablishedEvent::class, applicationDataEstablishedHandler)
	}

	fun dispose() {
		eventBus.unregister(applicationDataContentHandler)
		eventBus.unregister(applicationDataEstablishedHandler)
		addressableListeners.forEach { addressable.removeListener(it) }
	}

	fun addListener(l: AddressableListener) {
		if (!addressableListeners.contains(l)) {
			addressableListeners.add(l)
			addressable.addListener(l)
		}
	}

	fun removeListener(l: AddressableListener) {
		addressableListeners.remove(l)
		addressable.removeListener(l)
	}

	private fun unregisterOldContent() {
		addressableListeners.forEach { addressable.removeListener(it) }
	}

	private fun registerNewContent(graph: Graph?) {
		this.addressable = link.getLinkedObject(graph)
		addressableListeners.forEach { addressable.addListener(it) }
	}
}