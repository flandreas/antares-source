package ch.scorpion.antares.model.vertice

import ch.scorpion.antares.model.port.DigitalPort
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.DigitalSignalFactory
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.dsl.RuntimeError
import ch.scorpion.jabbah.base.dsl.Variable
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.GraphPort
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVerticeRef
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVerticeRefActivationRecord

/**
 * Performs type conversions when DSL script set values of [GraphPort]s that are not yet
 * [DigitalSignal]s, but e.g. [Long].
 */
class DigitalSubGraphVerticeRefActivationRecord(
	verticeRef: SubGraphVerticeRef,
	signalHandler: SignalHandler
) : SubGraphVerticeRefActivationRecord(verticeRef, signalHandler) {

	override fun setValue(variable: Variable, value: Any) {
		val port = verticeRef.getPort<DigitalSignal>(variable.token.value!!) as DigitalPort
		val effValue = when (value) {
			is DigitalSignal -> value
			is Long -> DigitalSignalFactory.of(port.bitWidth, value)
			else -> throw RuntimeError(variable.location, Translations.getString("graph.dsl.cannotSetPortValue.msg"))
		}
		port.setOutgoingSignalBuffered(effValue, signalHandler)
	}
}