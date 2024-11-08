package ch.scorpion.jabbah.graph.model.vertice

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.dsl.*
import ch.scorpion.jabbah.base.parser.TextLocation
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.GraphTypeSignalAdapter
import ch.scorpion.jabbah.graph.model.Port
import ch.scorpion.jabbah.graph.model.InputPort
import ch.scorpion.jabbah.graph.model.OutputPort

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
			val outerTypeValue = verticeRef.getInput<Any>(name).getIncomingSignal()!!
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