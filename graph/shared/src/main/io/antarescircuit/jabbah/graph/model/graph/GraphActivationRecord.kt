package io.antarescircuit.jabbah.graph.model.graph

import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.dsl.ActivationRecord
import io.antarescircuit.jabbah.base.dsl.RuntimeError
import io.antarescircuit.jabbah.base.dsl.Variable
import io.antarescircuit.jabbah.base.parser.TextLocation
import io.antarescircuit.jabbah.graph.model.Graph
import io.antarescircuit.jabbah.graph.model.GraphPort
import io.antarescircuit.jabbah.graph.model.param.GraphParamValues

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
		getValue(variable.token.value!!, variable.location)

	override fun getValue(name: String, location: TextLocation): Any =
		getOptionalValue(name) ?: throw RuntimeError(location, Translations.getString("antares.dsl.noValueAtPort.msg", name))

	override fun getOptionalValue(variable: Variable): Any? = getOptionalValue(variable.token.value!!)

	override fun getOptionalValue(name: String, location: TextLocation): Any? = getPortValue(name) ?: getParamValue(name)

	private fun getPortValue(name: String): Any? = graph.getGraphPort<Any>(name)?.signal

	private fun getParamValue(name: String): Any? =
		graph.parameterValues.getTypedValue<Any>(name)?.let {
			it.type.toDslValue(it.value)
		}
}