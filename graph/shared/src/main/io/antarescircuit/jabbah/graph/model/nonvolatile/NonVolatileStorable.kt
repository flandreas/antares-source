package io.antarescircuit.jabbah.graph.model.nonvolatile

import io.antarescircuit.jabbah.io.*
import io.antarescircuit.jabbah.graph.model.GraphElement

/**
 * A [Storable] for storing a tree of [NonVolatileStorable] used to store non-volatile
 * data produced by [NonVolatile]. Each tree node corresponds with a [NonVolatile]
 * with the given ID.
 *
 * A [NonVolatile] adds its non-volatile data in [GraphElement.executionStoppedNonVolatile]
 * as content to the provided [NonVolatileStorable], and reads it back in
 * [GraphElement.executionInitializeNonVolatile]. The content data is organized as
 * a map of named [Storable]s.
 *
 * @param id the ID of the [GraphElement]
 */
class NonVolatileStorable(
    id: Int = 0
) : AbstractStorable() {

    private var graphElementId: Int = id

    private val content = mutableMapOf<String, Storable>()

    private val children = mutableListOf<NonVolatileStorable>()

    val hasChildren: Boolean get() = children.isNotEmpty()

    fun setContent(name: String, storable: Storable) {
        content[name] = storable
    }

    fun getContent(name: String): Storable? = content[name]

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
        if (content.isNotEmpty()) {
            writer.writeMap("content", content)
        }
        if (children.isNotEmpty()) {
            writer.writeStorables("children", children.iterator())
        }
    }

    override fun read(reader: StoreReader) {
        graphElementId = reader.readInt("id")
        children.clear()
        if (reader.hasElement("content")) {
            reader.readMap("content").forEach {
                (name, storable) -> content[name] = storable
            }
        }
        if (reader.hasElement("children")) {
            children.addAll(reader.readStorables("children"))
        }
    }
}