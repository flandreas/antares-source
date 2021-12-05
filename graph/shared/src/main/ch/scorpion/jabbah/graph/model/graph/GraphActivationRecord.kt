package ch.scorpion.jabbah.graph.model.graph

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.dsl.ActivationRecord
import ch.scorpion.jabbah.base.dsl.RuntimeError
import ch.scorpion.jabbah.base.dsl.Variable
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.model.GraphPort
import ch.scorpion.jabbah.graph.model.param.GraphParamValues

/**
 * An [ActivationRecord] implementation that allows a DSL script to read
 * a [Graph]'s [GraphPort] signals or [GraphParamValues] as global context variables.
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
		getOptionalValue(variable) ?: throw RuntimeError(variable.location, Translations.getString("antares.dsl.noValueAtPort.msg", variable.token.value!!))

	override fun getOptionalValue(variable: Variable): Any? {
		return getPortValue(variable) ?: getParamValue(variable)
	}

	private fun getPortValue(variable: Variable): Any? {
		val name = variable.token.value!!
		val port = graph.getGraphPort<Any>(name)
		return port?.signal
	}

	private fun getParamValue(variable: Variable): Any? {
		val name = variable.token.value!!
		return graph.parameterValues.getTypedValue<Any>(name)?.let {
			it.type.toDslValue(it.value)
		}
	}
}