package ch.scorpion.jabbah.graph

import ch.scorpion.jabbah.base.HierarchyVisitor
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.io.*

/**
 * Stores a [GraphView] and its [Graph].
 */
class GraphStorable(
	graphView: GraphView<*>
) : Storable {

	constructor(): this(Translations.getString("graph.name.unknown"))
	constructor(name: String): this(GraphViewModule.graphViewFactory.invoke(name))

	private var _graphView: GraphView<*>? = graphView
	val graphView: GraphView<*> get() = _graphView!!

    val model: Graph? get() = graphView.graph

    /** ---- [Storable] interface */

    override var storableId: Int = 0

    override fun write(writer: StoreWriter) {
        writer.writeStorable("model", graphView.graph!!);
        writer.writeStorable("view", graphView);
    }

    override fun read(reader: StoreReader) {
        val graph = reader.readStorable("model") as Graph
        _graphView = reader.readStorable("view") as GraphView<*>
        reader.requestResolution(this, Reference(additionalInfo = graph))
    }

    override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) {
        graphView.graph = reference.additionalInfo as Graph
    }

    override fun getStorableChildren(): Iterator<Storable> {
        return listOf(graphView.graph!!, graphView).iterator()
    }

    /** ---- [GraphStorable] */

    fun accept(visitor: HierarchyVisitor): Boolean {
        if (visitor.visitEnter(this)) {
	        graphView.accept(visitor)
        }
        return visitor.visitLeave(this)
    }
}