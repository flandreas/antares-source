package ch.scorpion.jabbah.graph.view.net.netview

import ch.scorpion.jabbah.base.collection.ImmutableList
import ch.scorpion.jabbah.base.collection.toImmutableList
import ch.scorpion.jabbah.graph.model.Net
import ch.scorpion.jabbah.graph.model.Port
import ch.scorpion.jabbah.graph.model.net.NetImpl
import ch.scorpion.jabbah.graph.view.NetView
import ch.scorpion.jabbah.graph.view.NetViewElement
import ch.scorpion.jabbah.io.*

/**
 * A standard implementation of the [NetView] interface.
 * @param T the type of signal that the [Net] forwards.
 */
class NetViewImpl<T : Any>(
	override var net: Net<T> = NetImpl(),
	style: NetViewStyle = NetViewStyle.LINE
) : NetView<T> {

	override var style: NetViewStyle = style
		set(value) {
			field = value
			elements.forEach { it.handleNetViewStyleChanged() }
		}

	private val elements = mutableListOf<NetViewElement<T>>()

	/** ---- [NetView] interface */

	override val isEmpty: Boolean get() = elements.isEmpty()

	override fun add(elem: NetViewElement<T>) {
		if (!elements.contains(elem)) {
			elem.netView = this
			elements.add(elem)
			elem.handleNetViewStyleChanged()
		}
	}

	override fun remove(elem: NetViewElement<T>) {
		if (!elements.contains(elem)) {
			return
		}
		elements.remove(elem)
		elem.netView = null
	}

	override fun getElements(): ImmutableList<NetViewElement<T>> {
		return elements.toImmutableList()
	}

	/** ---- [Storable] interface */

	override fun write(writer: StoreWriter) {
		writer.writeInt("modelId", writer.provideIdentity(net))
		writer.writeString("style", style.customName)
	}

	override fun read(reader: StoreReader) {
		style = NetViewStyle.withName(reader.readString("style"))
		val modelId = reader.readInt("modelId")
		if (modelId >= 0) {
			// There are files out there with "modelId=-1" due to a bug in an older version
			reader.requestResolution(this, Reference(
				name = "modelId",
				referenceId = modelId,
				resolveAfter = listOf(modelId)))
		}
	}

	override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) {
		if (reference.name == "modelId") {
			referenceResolver.getStorable<Net<T>>(reference.referenceId)?.let { net = it }
		}
	}

	override fun splitOff(ports: Set<Port<T>>): NetView<T> {
		val newNetView = NetViewImpl(net.splitOff(ports), style)
		elements.toList().forEach {
			val connectedPorts = it.connectedPorts
			if (connectedPorts.isNotEmpty() && ports.containsAll(connectedPorts)) {
				it.net = newNetView.net
				it.netView?.remove(it)
				newNetView.add(it)
			}
		}
		return newNetView
	}
}