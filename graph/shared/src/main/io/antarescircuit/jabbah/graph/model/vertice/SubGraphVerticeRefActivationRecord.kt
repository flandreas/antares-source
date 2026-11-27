package io.antarescircuit.jabbah.graph.model.vertice

import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.dsl.*
import io.antarescircuit.jabbah.base.parser.TextLocation
import io.antarescircuit.jabbah.execution.SignalHandler
import io.antarescircuit.jabbah.graph.model.GraphTypeSignalAdapter
import io.antarescircuit.jabbah.graph.model.Port
import io.antarescircuit.jabbah.graph.model.InputPort
import io.antarescircuit.jabbah.graph.model.OutputPort
import io.antarescircuit.jabbah.graph.model.GraphActorData
import io.antarescircuit.jabbah.graph.model.SampledGraphActorData

typealias SubGraphVerticeRefActivationRecordFactory =
	(verticeRef: SubGraphVerticeRef, signalHandler: SignalHandler) -> SubGraphVerticeRefActivationRecord

/**
 * An [ActivationRecord] implementation that allows a DSL script to access
 * a [SubGraphVerticeRef]'s [Port]'s values as global context variables
 * for reading ([InputPort]) and writing ([OutputPort]).
 */
open class SubGraphVerticeRefActivationRecord(
	protected val verticeRef: SubGraphVerticeRefIF,
	protected val signalHandler: SignalHandler
) : ActivationRecord {

	private var inputSnapshot: SampledGraphActorData? = null

	/** Scope sampled input reads to this invocation; outputs and stored script state remain live. */
	fun <T> withInputSnapshot(data: GraphActorData, action: () -> T): T {
		val previous = inputSnapshot
		inputSnapshot = data as? SampledGraphActorData
		try {
			return action()
		} finally {
			inputSnapshot = previous
		}
	}

	private val adapter: GraphTypeSignalAdapter<Any, Any> get() =
		// Accesses singletons, no objects being created
		verticeRef.graphType.adaptTo(verticeRef.getGraph().type)

	override fun clear() { }

	override fun isLocallyDefined(name: String): Boolean = verticeRef.hasPort(name)

	override fun isDefined(name: String): Boolean = verticeRef.hasPort(name)

	override fun preset(name: String, value: Any) {
		throw UnsupportedOperationException("not applicable")
	}

	override fun define(variable: Variable) {
		throw UnsupportedOperationException("not applicable")
	}

	override fun setValue(variable: Variable, value: Any) {
		if (!verticeRef.hasOutput(variable.token.value!!)) {
			throw RuntimeError(variable.location, Translations.getString("graph.dsl.outputNotFound.msg", variable.token.value!!))
		}
		// Convert between GraphTypes
		val outerTypeValue = verticeRef.graphType.adaptTo<Any, Any>(verticeRef.getGraph().type).convertOutgoingSignal(value)
		verticeRef.getOutput<Any>(variable.token.value!!).setOutgoingSignalBuffered(outerTypeValue, signalHandler)
	}

	override fun getValue(variable: Variable): Any =
		getValue(variable.token.value!!, variable.location)

	override fun getValue(name: String, location: TextLocation): Any {
		if (verticeRef.hasInput(name)) {
			// Convert between GraphTypes
			val port = verticeRef.getInput<Any>(name)
			val snapshot = inputSnapshot
			val outerTypeValue = if (snapshot != null) snapshot.getSignal<Any>(port.portId)!! else port.getIncomingSignal()!!
			return adapter.convertIncomingSignal(outerTypeValue)!!
		} else if (verticeRef.hasOutput(name)) {
			val innerTypeValue = verticeRef.getOutput<Any>(name).getOutgoingSignal()!!
			return adapter.convertOutgoingSignal(innerTypeValue)!!
		}
		verticeRef.paramValues.getTypedValue<Any>(name)?.let {
			return it.type.toDslValue(it.value)
		} ?: throw RuntimeError(location, Translations.getString("graph.dsl.inputNotFound.msg", name))
	}

	override fun getOptionalValue(variable: Variable): Any? =
		getOptionalValue(variable.token.value!!, variable.location)

	override fun getOptionalValue(name: String, location: TextLocation): Any? =
		if (isDefined(name)) {
			getValue(name, location)
		} else {
			null
		}
}
