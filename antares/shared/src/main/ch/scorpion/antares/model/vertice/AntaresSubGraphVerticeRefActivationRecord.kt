package ch.scorpion.antares.model.vertice

import ch.scorpion.antares.model.AntaresGraphTypes
import ch.scorpion.antares.model.port.DigitalPort
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.DigitalSignalFactory
import ch.scorpion.antares.model.analog.AnalogSignal
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.dsl.RuntimeError
import ch.scorpion.jabbah.base.dsl.Variable
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.GraphPort
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVerticeRef
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVerticeRefActivationRecord

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