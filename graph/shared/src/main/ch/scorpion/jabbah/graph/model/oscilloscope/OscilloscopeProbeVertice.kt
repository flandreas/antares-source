package ch.scorpion.jabbah.graph.model.oscilloscope

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.model.InputPort
import ch.scorpion.jabbah.graph.model.PortType
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
		private val type = Translations.getString("graph.component.oscilloscope.port")
	}

	init {
		val port = portFactory.createPort<T>(PortType.INPUT)
		port.name = name
		addPort(portFactory.createOscilloscopeProbePort<T>(name))
	}

	/** ---- [Vertice] interface */

	override val type: String get() = OscilloscopeProbeVertice.type
	override val typeDesc: String? get() = null

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