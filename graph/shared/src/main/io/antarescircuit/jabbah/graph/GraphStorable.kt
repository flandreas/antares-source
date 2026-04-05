package io.antarescircuit.jabbah.graph

import io.antarescircuit.jabbah.base.HierarchyVisitor
import io.antarescircuit.jabbah.edit.model.text.TranslatableText
import io.antarescircuit.jabbah.graph.model.Graph
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.module.GraphViewModule
import io.antarescircuit.jabbah.io.*

/**
 * Stores a [GraphView] and its [Graph].
 */
class GraphStorable(
	graphView: GraphView?
) : AbstractStorable() {

	constructor() : this(null)
	constructor(name: TranslatableText) : this(GraphViewModule.graphViewFactory.create(name))

	private var _graphView: GraphView? = graphView
		set(value) {
			if (field !== value) {
				field?.dispose()
				field = value
			}
		}
	val graphView: GraphView get() = _graphView!!

	val model: Graph? get() = _graphView?.graph

	fun dispose() {
		_graphView?.dispose()
		model?.dispose()
	}

	/** ---- [Storable] interface */

	override fun write(writer: StoreWriter) {
		writer.writeStorable("model", graphView.graph!!)
		writer.writeStorable("view", graphView)
	}

	override fun read(reader: StoreReader) {
		val graph = reader.readStorable("model") as Graph
		_graphView = reader.readStorable("view") as GraphView
		reader.requestResolution(this, Reference(additionalInfo = graph))
	}

	override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) {
		graphView.graph = reference.additionalInfo as Graph
	}

	/** ---- [GraphStorable] */

	fun accept(visitor: HierarchyVisitor): Boolean {
		if (visitor.visitEnter(this)) {
			graphView.accept(visitor)
		}
		return visitor.visitLeave(this)
	}
}