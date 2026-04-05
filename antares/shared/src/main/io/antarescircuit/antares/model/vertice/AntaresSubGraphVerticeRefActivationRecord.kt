package io.antarescircuit.antares.model.vertice

import io.antarescircuit.antares.model.AntaresGraphTypes
import io.antarescircuit.antares.model.port.DigitalPort
import io.antarescircuit.antares.model.signal.DigitalSignal
import io.antarescircuit.antares.model.signal.DigitalSignalFactory
import io.antarescircuit.antares.model.analog.AnalogSignal
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.dsl.RuntimeError
import io.antarescircuit.jabbah.base.dsl.Variable
import io.antarescircuit.jabbah.base.logger
import io.antarescircuit.jabbah.execution.SignalHandler
import io.antarescircuit.jabbah.graph.model.GraphPort
import io.antarescircuit.jabbah.graph.model.vertice.SubGraphVerticeRef
import io.antarescircuit.jabbah.graph.model.vertice.SubGraphVerticeRefActivationRecord

/**
 * Performs type conversions when DSL script set values of [GraphPort]s that are not yet
 * [DigitalSignal]s or [AnalogSignal], but e.g. [Long] or [Float].
 */
class AntaresSubGraphVerticeRefActivationRecord(
	verticeRef: SubGraphVerticeRef,
	signalHandler: SignalHandler
) : SubGraphVerticeRefActivationRecord(verticeRef, signalHandler) {

	companion object {
		private val LOG by logger(AntaresSubGraphVerticeRefActivationRecord::class)
	}

	override fun setValue(variable: Variable, value: Any) {
		val port = verticeRef.getPort<DigitalSignal>(variable.token.value!!) as DigitalPort
		val effValue = when (value) {
			is DigitalSignal -> value
			is AnalogSignal -> value
			is Long -> when (verticeRef.getGraphIfPresent()?.type) {
				AntaresGraphTypes.Analog -> AnalogSignal(value.toDouble())
				AntaresGraphTypes.Digital -> DigitalSignalFactory.of(port.bitWidth, value)
				else -> {
					LOG.error("Unsupported GraphType ${verticeRef.graphType::class.simpleName}")
					throw RuntimeError(variable.location, "Unsupported GraphType")
				}
			}
			is Float -> AnalogSignal(value.toDouble())
			else -> throw RuntimeError(variable.location, Translations.getString("graph.dsl.cannotSetPortValue.msg"))
		}
		super.setValue(variable, effValue)
	}
}