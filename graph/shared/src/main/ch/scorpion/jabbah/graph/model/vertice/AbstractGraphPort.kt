package ch.scorpion.jabbah.graph.model.vertice

import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.model.text.description.Description
import ch.scorpion.jabbah.graph.model.*
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

/**
 * An abstract base implementation of [GraphPort] that mainly posts a [GraphPortNameChanged] event
 * when the name has been changed.
 */
abstract class AbstractGraphPort<T : Any>(
	port: Port<T>,
	name: String? = null,
	calculator: VerticeCalculator<*> = EmptyVerticeCalculator,
	private val eventBus: EventBus = BaseModule.eventBus
) : AbstractInteractableVertice<T>(calculator, defaultName(name, port.portType.reverse())), GraphPort<T> {

	companion object {
		private fun defaultName(name: String?, portType: PortType): String {
			if (StringUtils.isNotEmpty(name)) {
				return name!!
			}
			return when (portType) {
				PortType.INPUT -> "I1"
				PortType.OUTPUT -> "O1"
				PortType.INOUT -> "IO1"
			}
		}
	}

	init {
		propagationDelay = 0
		addPort(port)
	}

	override val storePropagationDelay: Boolean get() = false

	var customCanBeUndefined: Boolean
		get() = getPort<Any>().let {
			if (it is OutputPort) it.customCanBeUndefined else false
		}
		set(value) {
			if (value != customCanBeUndefined) {
				getPort<Any>().let {
					if (it is OutputPort) {
						it.customCanBeUndefined = value
						eventBus.post(GraphPortCanBeUndefinedChanged(this, value))
					}
				}
			}
		}

	/** ---- [InteractableVertice] interface */

	override val interactivePropagationDelay: Long get() = 1_000

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
				if (isNotReading) {
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

	/** Corresponds with [Port.description] of the single [Port] of this [GraphPort].*/
	override var description: Description
		get() = getPort<T>().description
		set(value) { getPort<T>().description = value }

	/** ---- [Storable] interface */

	override fun write(writer: StoreWriter) {
		super.write(writer)
		if (customCanBeUndefined) {
			writer.writeBoolean("canBeUndefined", customCanBeUndefined)
		}
	}

	override fun read(reader: StoreReader) {
		super.read(reader)
		if (reader.hasAttribute("canBeUndefined")) {
			customCanBeUndefined = reader.readBoolean("canBeUndefined")
		}
	}
}