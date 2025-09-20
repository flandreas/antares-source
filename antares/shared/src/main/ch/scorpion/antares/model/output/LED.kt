package ch.scorpion.antares.model.output

import ch.scorpion.antares.model.port.DigitalPortImpl
import ch.scorpion.antares.model.signal.Bit
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.jabbah.base.LongValueImpl
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.execution.actor.Actor
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.model.GraphActorData
import ch.scorpion.jabbah.graph.model.Vertice
import ch.scorpion.jabbah.graph.model.vertice.CalculatingVertice
import ch.scorpion.jabbah.graph.model.vertice.VerticeCalculator
import ch.scorpion.jabbah.graph.model.element.AbstractGraphElement

/**
 * A light emitting [Vertice] that turns on with [Bit.True].
 */
class LED : CalculatingVertice(CALCULATOR), LightEmitterModel {

	companion object {

		private const val BASE_RESOURCE_KEY = "library.element.LED"
		private val TYPE get() = Translations.getString("$BASE_RESOURCE_KEY.name")
		private val TYPE_DESC get() = Translations.getOptionalString("$BASE_RESOURCE_KEY.desc")

		private val CALCULATOR = Calculator()

		private class Calculator : VerticeCalculator<LED> {
			override fun calculate(vertice: LED, data: GraphActorData, signalHandler: SignalHandler) {
				vertice.isOn = (data.getSignal<DigitalSignal>(1)!!).bitAt(0) == Bit.True
			}
		}
	}

	override val type: String get() = TYPE
	override val typeDesc: String? get() = TYPE_DESC

	var isOn: Boolean = false
		set(value) {
			if (field != value) {
				field = value
				stateChanged()
			}
		}

	init {
		addPort(DigitalPortImpl.createInput())
		propagationDelay = LongValueImpl.ZERO
	}

	/** ---- [Actor] */

	override fun executionStopped(signalHandler: SignalHandler) {
		super.executionStopped(signalHandler)
		isOn = false
	}

	/** ---- [AbstractGraphElement] */

	override fun graphParamsChanged(graph: Graph) {
		super.graphParamsChanged(graph)
		stateChanged(null, LightEmitterModel.REASON_GRAPH_PARAM_CHANGED, graph)
	}
}