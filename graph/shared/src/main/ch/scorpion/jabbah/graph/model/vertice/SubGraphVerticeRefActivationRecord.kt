package ch.scorpion.jabbah.graph.model.vertice

import ch.scorpion.jabbah.base.dsl.*
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.Port
import ch.scorpion.jabbah.graph.model.InputPort
import ch.scorpion.jabbah.graph.model.OutputPort

fun interface SubGraphVerticeRefActivationRecordFactory {
	fun create(verticeRef: SubGraphVerticeRef, signalHandler: SignalHandler): SubGraphVerticeRefActivationRecord
}
/**
 * An [ActivationRecord] implementation that allows a DSL script to access
 * a [SubGraphVerticeRef]'s [Port]'s values as global context variables
 * for reading ([InputPort]) and writing ([OutputPort]).
 */
open class SubGraphVerticeRefActivationRecord(
	protected val verticeRef: SubGraphVerticeRef,
	protected val signalHandler: SignalHandler
) : ActivationRecord {

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
			throw RuntimeError(variable.location, "Output '${variable.token.value!!}' not defined")
		}
		verticeRef.getOutput<Any>(variable.token.value!!).setOutgoingSignalBuffered(value, signalHandler)
	}

	override fun getValue(variable: Variable): Any {
		if (!verticeRef.hasInput(variable.token.value!!)) {
			throw RuntimeError(variable.location, "Input '${variable.token.value!!}' not defined")
		}
		return verticeRef.getInput<Any>(variable.token.value!!).getIncomingSignal()!!
	}

	override fun getOptionalValue(variable: Variable): Any? =
		if (isDefined(variable.token.value!!)) {
			getValue(variable)
		} else {
			null
		}
}