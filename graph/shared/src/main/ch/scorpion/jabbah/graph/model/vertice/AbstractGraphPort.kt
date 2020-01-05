package ch.scorpion.jabbah.graph.model.vertice

import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.graph.model.*

/**
 * An abstract base implementation of [GraphPort] that mainly posts a [GraphPortNameChanged] event
 * when the name has been changed.
 */
abstract class AbstractGraphPort<T : Any>(
	baseResourceKey: String,
	port: Port<T>,
	name: String? = null,
	calculator: VerticeCalculator<*> = EmptyVerticeCalculator,
	private val eventBus: EventBus = BaseModule.eventBus
) : AbstractInteractableVertice(baseResourceKey, calculator, defaultName(name, port.portType.reverse())), GraphPort<T> {

	companion object {
		private fun defaultName(name: String?, portType: PortType): String {
			if (StringUtils.isNotEmpty(name)) {
				return name!!
			}
			return when(portType) {
				PortType.INPUT -> "I1"
				PortType.OUTPUT -> "O1"
				PortType.INOUT -> "IO1"
			}
		}
	}

	init {
		propagationDelay = 1
		addPort(port)
	}

	override val storePropagationDelay: Boolean get() = false

	/** ---- [Vertice] interface */

	override var name: String?
		get() = super.name
		set(value) {
			if (super.name != value) {
				if (StringUtils.isEmpty(value)) {
					throw IllegalArgumentException(Translations.getString("graph.port.nameMustNotBeEmpty.msg"))
				}
				val oldName = super.name
				super.name = value
				stateChanged()
				eventBus.postVetoable(
					event = GraphPortNameChanged(this, oldName, value),
					undoEvent = GraphPortNameChanged(this, value, oldName),
					elseHandler = {
						super.name = oldName
						stateChanged()
						throw IllegalArgumentException(it.message)
					}
				)
			}
		}
}