package ch.scorpion.jabbah.graph.model.vertice

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.graph.model.GraphPortNameChanged
import ch.scorpion.jabbah.graph.model.Port
import ch.scorpion.jabbah.graph.model.GraphPort
import ch.scorpion.jabbah.graph.model.Vertice

/**
 * An abstract base implementation of [GraphPort] that mainly posts a [GraphPortNameChanged] event
 * when the name has been changed.
 */
abstract class AbstractGraphPort<T: Any>(
	baseResourceKey: String,
	port: Port<T>,
	name: String? = null,
	calculator: VerticeCalculator<*> = EmptyVerticeCalculator,
	private val eventBus: EventBus = BaseModule.eventBus
) : CalculatingVertice(baseResourceKey, calculator, name), GraphPort<T> {

	init {
		propagationDelay = 0
		addPort(port)
	}

	/** ---- [Vertice] interface */

	override var name: String?
		get() = super.name
		set(value) {
			if (super.name != value) {
				val oldName = super.name
				super.name = value
				stateChanged()
				eventBus.postVetoable(
					event = GraphPortNameChanged(this, oldName, value),
					undoEvent = GraphPortNameChanged(this, value, oldName),
					elseHandler = {
						super.name = oldName
						stateChanged()
						// TODO Post an application error event that can be displayed to the user as an info
					}
				)
			}
		}
}