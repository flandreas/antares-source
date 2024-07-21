package ch.scorpion.jabbah.graph.model.nonvolatile

import ch.scorpion.jabbah.io.*

class NonVolatileStorable(
    id: Int = 0,
    content: Storable? = null
) : AbstractStorable() {

    private var graphElementId: Int = id

    // TODO: Is it necessary to store a Map of content?
    var content: Storable? = content
        private set

    private val children = mutableListOf<NonVolatileStorable>()

    val hasChildren: Boolean get() = children.isNotEmpty()

    fun addChild(child: NonVolatileStorable) {
        children.add(child)
    }

    fun getChild(id: Int): NonVolatileStorable? = children.firstOrNull {
        it.graphElementId == id
    }

    /** ---- [Storable] */

    override val isReferencable: Boolean get() = false

    override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) {
        // Empty, not referencable
    }

    override fun write(writer: StoreWriter) {
        writer.writeInt("id", graphElementId)
        content?.let {
            writer.writeStorable("content", it)
        }
        writer.writeStorables("children", children.iterator())
    }

    override fun read(reader: StoreReader) {
        graphElementId = reader.readInt("id")
        children.clear()
        if (reader.hasElement("content")) {
            content = reader.readStorable("content")
        }
        children.addAll(reader.readStorables("children"))
    }
}