package io.antarescircuit.jabbah.graph.model.oscilloscope

import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.execution.SignalHandler
import io.antarescircuit.jabbah.graph.model.*
import io.antarescircuit.jabbah.graph.model.module.GraphModelModule
import io.antarescircuit.jabbah.graph.model.port.PortFactory
import io.antarescircuit.jabbah.graph.model.vertice.AbstractVertice
import io.antarescircuit.jabbah.io.Storable
import io.antarescircuit.jabbah.io.StoreReader
import io.antarescircuit.jabbah.io.StoreWriter

/**
 * A [Vertice] that probes signals in a [Graph] in order to be tracked and displayed
 * on the view layer.
 *
 * This constructor is meant for deserialization and does NOT create an [InputPort],
 * since the type of the [InputPort] is not know before reading from external data.
 */
open class OscilloscopeProbeVertice<T : Any>(
	graphType: GraphType = GenericGraphType,
	private val portFactory: PortFactory = GraphModelModule.portFactory
) : AbstractVertice() {

	companion object {
		private const val BASE_RESOURCE_KEY = "graph.component.oscilloscope.port"
		private val TYPE get() = Translations.getString("$BASE_RESOURCE_KEY.name")
		private val TYPE_DESC get() = Translations.getOptionalString("$BASE_RESOURCE_KEY.desc")

		/** This constructor creates an [InputPort] in this [OscilloscopeProbeVertice]. */
		fun <T : Any> create(
			name: String? = null,
			graphType: GraphType = GenericGraphType,
			portFactory: PortFactory = GraphModelModule.portFactory
		): OscilloscopeProbeVertice<T> {
			val vertice = graphType.createOscilloscopeProbeVertice<T>(name)
			vertice.addPort(portFactory.createOscilloscopeProbePort<T>(name, graphType))
			return vertice
		}
	}

	var graphType: GraphType = graphType
		private set

	/** ---- [Vertice] interface */

	override val type: String get() = TYPE
	override val typeDesc: String? get() = TYPE_DESC

	override fun inputChanged(input: InputPort<*>, signalHandler: SignalHandler, force: Boolean) {
		stateChanged(signalHandler)
	}

	/** ---- [Storable] interface */

	override fun write(writer: StoreWriter) {
		super.write(writer)
		writer.writeString("name", getPort<T>().name!!)
		writer.writeString("type", graphType.customName)
	}

	override fun read(reader: StoreReader) {
		super.read(reader)
		val name = reader.readString("name")
		graphType = if (reader.hasAttribute("type")) {
			GraphModelModule.graphTypeRegistry.withCustomName(reader.readString("type"))
		} else {
			GraphModelModule.defaultGraphType
		}
		addPort(portFactory.createOscilloscopeProbePort(name, graphType))
	}
}