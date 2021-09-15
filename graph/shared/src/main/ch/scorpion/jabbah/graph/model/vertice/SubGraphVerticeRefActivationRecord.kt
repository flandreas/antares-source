package ch.scorpion.jabbah.graph.model.vertice

import ch.scorpion.jabbah.base.dsl.*
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.GraphPort
import ch.scorpion.jabbah.graph.model.GraphInput
import ch.scorpion.jabbah.graph.model.GraphOutput


fun interface SubGraphVerticeRefActivationRecordFactory {
	fun create(verticeRef: SubGraphVerticeRef, signalHandler: SignalHandler): SubGraphVerticeRefActivationRecord
}
/**
 * An [ActivationRecord] implements that allows a DSL script to access
 * a [SubGraphVerticeRef]'s [GraphPort]'s values as global context variables
 * for reading ([GraphInput]) and writing [GraphOutput].
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
		verticeRef.getOutput<Any>(variable.token.value!!).setOutgoingSignalBuffered(value, signalHandler)
	}

	override fun getValue(variable: Variable): Any =
		verticeRef.getInput<Any>(variable.token.value!!).getIncomingSignal()
			?: throw RuntimeError(variable.location, "Port ${variable.token.value!!} not defined")

	override fun getOptionalValue(variable: Variable): Any? =
		if (isDefined(variable.token.value!!)) {
			getValue(variable)
		} else {
			null
		}
}