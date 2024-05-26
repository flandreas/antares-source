package ch.scorpion.jabbah.graph.model.element

import ch.scorpion.jabbah.base.HierarchyVisitor
import ch.scorpion.jabbah.edit.model.text.description.Describable
import ch.scorpion.jabbah.edit.model.text.description.Description
import ch.scorpion.jabbah.edit.model.text.description.observableDescription
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.execution.actor.ActorImpl
import ch.scorpion.jabbah.graph.MetaGraphRepository
import ch.scorpion.jabbah.graph.model.*
import ch.scorpion.jabbah.graph.model.param.LongValueExpression
import ch.scorpion.jabbah.io.*

/**
 * Abstract base implementation of the [GraphElement] interface.
 */
abstract class AbstractGraphElement : ActorImpl(), GraphElement, Describable {

	/** Holds all registered [GraphElementListener]s */
	private var listeners: MutableList<GraphElementListener>? = null

	protected open val storePropagationDelay: Boolean get() = true

	override var description: Description by observableDescription(Description("")) { stateChanged() }

	/** ---- [GraphElement] interface */

	override var id: Int = 0

	override val isError: Boolean get() = executionError != null || designError != null

	override val designError: DesignError? = null

	override fun accept(visitor: HierarchyVisitor): Boolean {
		return visitor.visit(this)
	}

	override fun notifyStateChanged() {
		stateChanged(null)
	}

	override fun addGraphElementListener(l: GraphElementListener) {
		if (listeners == null) {
			listeners = mutableListOf()
		}
		if (!listeners!!.contains(l)) {
			listeners!!.add(l)
		}
	}

	override fun removeGraphElementListener(l: GraphElementListener) {
		if (listeners != null) {
			listeners!!.remove(l)
		}
	}

	override fun bind(deep: Boolean, repository: MetaGraphRepository) { }

	override fun formNet(signalHandler: SignalHandler) {
		// empty
	}

	override fun graphParamsChanged(graph: Graph) {
		(propagationDelay as? LongValueExpression)?.let { it.evaluateIn(graph)?.let { pd -> propagationDelay = pd  } }
	}

	/** ---- [Storable] interface */

	override var isReading: Boolean = false

	override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) {
		// empty
	}

	override fun write(writer: StoreWriter) {
		writer.writeInt("id", id)
		if (storePropagationDelay) {
			LongValueExpression.write("delay", propagationDelay, writer)
		}
		description.write("desc", writer)
	}

	override fun read(reader: StoreReader) {
		id = reader.readInt("id")
		if (storePropagationDelay) {
			propagationDelay = LongValueExpression.read("delay", reader)
		}
		description = Description.read("desc", reader)
		// Add an artificial resolution request so that views can request to be resolved AFTER this model
		reader.requestResolution(this, Reference(name = "modelId"))
	}

	/** ---- [AbstractGraphElement] */

	/**
	 * Notifies all registered [GraphElementListener]s that the state of this [GraphElement] has changed.
	 * @param signalHandler the [SignalHandler] if the change occurred during simulation
	 * @param reason indicates the kind of change so listeners can take appropriate action
	 */
	protected fun stateChanged(signalHandler: SignalHandler? = null, reason: String? = null) {
		if (listeners != null) {
			val event = GraphElementEvent(this, signalHandler, reason)
			listeners!!.toList().forEach { it.stateChanged(event) }
		}
	}
}