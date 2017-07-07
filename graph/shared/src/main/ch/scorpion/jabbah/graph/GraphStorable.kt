package ch.scorpion.jabbah.graph

import ch.scorpion.jabbah.base.HierarchyVisitor
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.view.GraphElementView
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.graph.GraphViewImpl
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.io.*

/**
 * Stores a [GraphView] and its [Graph].
 */
class GraphStorable(var graphView: GraphView<*>? = null) : Storable {
    constructor(): this(GraphViewModule.graphViewFactory.invoke())

    val model: Graph? get() = graphView?.graph

    /** ---- [Storable] interface */

    override var storableId: Int = 0

    override fun write(writer: StoreWriter) {
        writer.writeStorable("model", graphView!!.graph!!);
        writer.writeStorable("view", graphView!!);
    }

    override fun read(reader: StoreReader) {
        val graph = reader.readStorable("model") as Graph
        graphView = reader.readStorable("view") as GraphView<*>
        reader.requestResolution(this, Reference(additionalInfo = graph))
    }

    override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) {
        graphView!!.graph = reference.additionalInfo as Graph
    }

    override fun getStorableChildren(): Iterator<Storable> {
        return listOf(graphView!!.graph!!, graphView!!).iterator()
    }

    /** ---- [GraphStorable] */

    fun accept(visitor: HierarchyVisitor): Boolean {
        if (visitor.visitEnter(this)) {
            graphView?.accept(visitor)
        }
        return visitor.visitLeave(this)
    }
}