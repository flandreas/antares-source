package ch.scorpion.antares.model.inout

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.graph.model.*
import ch.scorpion.jabbah.graph.model.vertice.AbstractGraphPort
import ch.scorpion.jabbah.graph.model.vertice.EmptyVerticeCalculator
import ch.scorpion.jabbah.graph.model.vertice.VerticeCalculator
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

/**
 * @param T the type of signal
 */
abstract class AbstractCircuitInOut<T : Any>(
	port: Port<T>,
	name: String? = null,
	calculator: VerticeCalculator<*> = EmptyVerticeCalculator,
	eventBus: EventBus = BaseModule.eventBus
) : AbstractGraphPort<T>(port, name, calculator, eventBus), CircuitInOut<T> {

	companion object {

		private const val INPUT_BASE_RESOURCE_KEY = "library.element.GraphInput"
		private val INPUT_TYPE get() = Translations.getString("$INPUT_BASE_RESOURCE_KEY.name")
		private val INPUT_TYPE_DESC get() = Translations.getOptionalString("$INPUT_BASE_RESOURCE_KEY.desc")

		private const val OUTPUT_BASE_RESOURCE_KEY = "library.element.GraphOutput"
		private val OUTPUT_TYPE get() = Translations.getString("$OUTPUT_BASE_RESOURCE_KEY.name")
		private val OUTPUT_TYPE_DESC get() = Translations.getOptionalString("$OUTPUT_BASE_RESOURCE_KEY.desc")

		private const val INOUT_BASE_RESOURCE_KEY = "library.element.GraphInOut"
		private val INOUT_TYPE get() = Translations.getString("$INOUT_BASE_RESOURCE_KEY.name")
		private val INOUT_TYPE_DESC get() = Translations.getOptionalString("$INOUT_BASE_RESOURCE_KEY.desc")
	}

	/** ---- [GraphElement] */

	override val type: String
		get() = when (portType) {
			PortType.INOUT -> INOUT_TYPE
			PortType.INPUT -> INPUT_TYPE
			PortType.OUTPUT -> OUTPUT_TYPE
		}

	override val typeDesc: String?
		get() = when (portType) {
			PortType.INOUT -> INOUT_TYPE_DESC
			PortType.INPUT -> INPUT_TYPE_DESC
			PortType.OUTPUT -> OUTPUT_TYPE_DESC
		}

	override var portType: PortType
		get() = getPort<T>().portType.reverse()
		set(value) {
			if (portType != value) {
				val oldValue = portType
				getPort<T>().portType = value.reverse()
				eventBus.post(GraphPortTypeChanged(this, oldValue, portType))
			}
		}

	/** ---- [GraphInput] interface */

	override var subGraphInputPort: SubGraphInputPort<T>? = null

	/** ---- [GraphOutput] */

	override var subGraphOutputPort: SubGraphOutputPort<T>? = null

	/** ---- [CircuitInOut] */

	override val isToplevel: Boolean get() = subGraphInputPort == null

	/** ---- [Storable] interface */

	override fun write(writer: StoreWriter) {
		super.write(writer)
		writer.writeString("type", portType.customName)
	}

	override fun read(reader: StoreReader) {
		super.read(reader)
		portType = PortType.withName(reader.readString("type"))
	}
}