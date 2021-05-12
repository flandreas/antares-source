package ch.scorpion.jabbah.graph.model.oscilloscope

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.model.InputPort
import ch.scorpion.jabbah.graph.model.Vertice
import ch.scorpion.jabbah.graph.model.module.GraphModelModule
import ch.scorpion.jabbah.graph.model.port.PortFactory
import ch.scorpion.jabbah.graph.model.vertice.AbstractVertice
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

/**
 * A [Vertice] that probes signals in a [Graph] in order to be tracked and displayed
 * on the view layer.
 */
class OscilloscopeProbeVertice<T : Any>(
	name: String? = null,
	portFactory: PortFactory = GraphModelModule.portFactory
) : AbstractVertice() {

	companion object {
		private const val BASE_RESOURCE_KEY = "graph.component.oscilloscope.port"
		private val TYPE get() = Translations.getString("$BASE_RESOURCE_KEY.name")
		private val TYPE_DESC get() = Translations.getOptionalString("$BASE_RESOURCE_KEY.desc")
	}

	init {
		addPort(portFactory.createOscilloscopeProbePort<T>(name))
	}

	/** ---- [Vertice] interface */

	override val type: String get() = TYPE
	override val typeDesc: String? get() = TYPE_DESC

	override fun inputChanged(input: InputPort<*>, signalHandler: SignalHandler) {
		stateChanged(signalHandler)
	}

	/** ---- [Storable] interface */

	override fun write(writer: StoreWriter) {
		super.write(writer)
		writer.writeString("name", getPort<T>().name!!)
	}

	override fun read(reader: StoreReader) {
		super.read(reader)
		getPort<T>().name = reader.readString("name")
	}
}