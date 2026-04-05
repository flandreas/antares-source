package io.antarescircuit.jabbah.graph.model.element

import io.antarescircuit.jabbah.base.HierarchyVisitor
import io.antarescircuit.jabbah.base.event.VetoException
import io.antarescircuit.jabbah.base.event.VetoHandler
import io.antarescircuit.jabbah.edit.model.text.description.Describable
import io.antarescircuit.jabbah.edit.model.text.description.Description
import io.antarescircuit.jabbah.edit.model.text.description.observableDescription
import io.antarescircuit.jabbah.execution.SignalHandler
import io.antarescircuit.jabbah.execution.actor.ActorImpl
import io.antarescircuit.jabbah.graph.MetaGraphRepository
import io.antarescircuit.jabbah.graph.model.*
import io.antarescircuit.jabbah.graph.model.param.LongValueExpression
import io.antarescircuit.jabbah.io.*

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
	 * @param argument optional context object that might be used by the view object to handle the state change (e.g. the Graph)
	 */
	protected fun stateChanged(signalHandler: SignalHandler? = null, reason: String? = null, argument: Any? = null) {
		if (listeners != null && listeners!!.isNotEmpty()) {
			val event = GraphElementEvent(this, signalHandler, reason, argument)
			// Tuning: Faster than: listeners!!.toList().forEach { it.stateChanged(event) }
			var i = 0
			while (i < listeners!!.size) {
				listeners!![i].stateChanged(event)
				i++
			}
		}
	}

	/**
	 * Implements two-phase vetoable [GraphElementEvent] handling.
	 *
	 * First let all registered [GraphElementListener] check whether they would accept [event] by calling
	 * [GraphElementListener.checkStateChange]. If any of them throws [VetoException], [vetoHandler] is called,
	 * otherwise [GraphElementListener.stateChanged] is called on all [GraphElementListener]s, and finally[successHandler] gets called to give this [GraphElement] a chance to update its state.
	 */
	protected fun vetoableStateChanged(event: GraphElementEvent, successHandler: VetoHandler<Any>, vetoHandler: VetoHandler<VetoException>) {
		try {
			listeners?.toList()?.forEach { it.checkStateChange(event) }
			listeners?.toList()?.forEach { it.stateChanged(event) }
			successHandler(event)
		} catch (e: VetoException) {
			vetoHandler(e)
		}
	}
}