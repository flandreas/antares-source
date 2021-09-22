package ch.scorpion.jabbah.graph.model.graph

import ch.scorpion.jabbah.base.dsl.ActivationRecord
import ch.scorpion.jabbah.base.dsl.RuntimeError
import ch.scorpion.jabbah.base.dsl.Variable
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.model.GraphPort

/**
 * An [ActivationRecord] implementation that allows a DSL script to read
 * a [Graph]'s [GraphPort] values as global context variables.
 */
class GraphActivationRecord(private val graph: Graph) : ActivationRecord {

	override fun clear() { }

	override fun isLocallyDefined(name: String): Boolean = isDefined(name)

	override fun isDefined(name: String): Boolean = graph.getGraphPort<Any>(name) != null

	override fun preset(name: String, value: Any) {
		throw UnsupportedOperationException("not applicable")
	}

	override fun define(variable: Variable) {
		throw UnsupportedOperationException("not applicable")
	}

	override fun setValue(variable: Variable, value: Any) {
		throw UnsupportedOperationException("not applicable")
	}

	override fun getValue(variable: Variable): Any =
		getOptionalValue(variable) ?: throw RuntimeError(variable.location, "No value in port ${variable.token.value}")

	override fun getOptionalValue(variable: Variable): Any? {
		val name = variable.token.value!!
		val port = graph.getGraphPort<Any>(name)
		return port?.signal
	}
}