package ch.scorpion.jabbah.graph.view.net.netview

import ch.scorpion.jabbah.base.collection.EmptyIterator
import ch.scorpion.jabbah.base.collection.ImmutableList
import ch.scorpion.jabbah.base.collection.toImmutableList
import ch.scorpion.jabbah.graph.model.Net
import ch.scorpion.jabbah.graph.model.net.NetImpl
import ch.scorpion.jabbah.graph.view.NetView
import ch.scorpion.jabbah.graph.view.NetViewElement
import ch.scorpion.jabbah.io.*

/**
 * A standard implementation of the [NetView] interface.
 * @param T the type of signal that the {@link Net} forwards.
 */
class NetViewImpl<T: Any>(
    override var net: Net<T> = NetImpl()
) : NetView<T> {

    override var style: NetViewStyle = NetViewStyle.LINE
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

    override var storableId: Int = 0

    override fun write(writer: StoreWriter) {
        writer.writeInt("modelId", writer.provideIdentity(net))
        writer.writeString("style", style.customName)
    }

    override fun read(reader: StoreReader) {
        style = NetViewStyle.withName(reader.readString("style"))
        reader.requestResolution(this, Reference(
                name = "modelId",
                referenceId = reader.readInt("modelId"),
                resolveAfter = listOf(reader.readInt("modelId"))))
    }

    override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) {
        if (reference.name == "modelId") {
            // TODO The resolved model should never be null. But there are files out there with "modelId=-1"??
            // Consider insisting on net != null and fixing the files (which have probably been created by a but in an old version)
            val newNet = referenceResolver.getStorable(reference.referenceId) as Net<T>?
            if (newNet != null) {
                net = referenceResolver.getStorable(reference.referenceId) as Net<T>
            }
        }
    }

    override fun getStorableChildren(): Iterator<Storable> {
        return EmptyIterator()
    }
}