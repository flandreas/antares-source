package io.antarescircuit.jabbah.graph.model.vertice

import io.antarescircuit.jabbah.base.*
import io.antarescircuit.jabbah.base.collection.ImmutableList
import io.antarescircuit.jabbah.base.collection.toImmutableList
import io.antarescircuit.jabbah.edit.model.text.TranslatableText
import io.antarescircuit.jabbah.edit.model.text.description.Name
import io.antarescircuit.jabbah.execution.SignalHandler
import io.antarescircuit.jabbah.graph.container.ContainerDrawing
import io.antarescircuit.jabbah.graph.model.Graph
import io.antarescircuit.jabbah.graph.model.Port
import io.antarescircuit.jabbah.graph.model.SubGraphOutputPort
import io.antarescircuit.jabbah.graph.model.SubGraphPort
import io.antarescircuit.jabbah.io.Storable
import io.antarescircuit.jabbah.io.StoreReader
import io.antarescircuit.jabbah.io.StoreWriter

/**
 * A [SubGraphVertice] implementation that is used as the model class of a [ContainerDrawing].
 * They are used as a blueprint when creating [SubGraphVerticeRef]s that reference a particular [Graph].
 *
 * Extends [AbstractVertice] for its [Port] management functionality.
 */
class SubGraphVerticeImpl(
	name: String = Translations.getString("graph.name.unknown")
) : AbstractVertice(name), SubGraphVertice {

	companion object {
		private val type = Translations.getString("graph.element.container.name")
	}

	override val type: String get() = SubGraphVerticeImpl.type
	override val typeDesc: String? get() = null

	/** ---- [SubGraphVertice] */

	/** Used when [SubGraphVerticeRef]s are created from this [SubGraphVerticeImpl].*/
	override var graphUUID: UUID? = null

	override var graphName = Name(TranslatableText(name))

	override fun getGraphIfPresent(): Graph? {
		return null
	}

	override fun getGraphIfNotBroken(): Graph? {
		return null
	}

	override fun getGraph(): Graph {
		throw UnsupportedOperationException()
	}

	override fun <T : Any> propagateOutput(outputPort: SubGraphOutputPort<T>, signal: T, signalHandler: SignalHandler) {
		// Not needed here
	}

	/** ---- [AbstractVertice] */

	/** Represents the [graphName] in the current system [Language].*/
	override var name: String?
		get() = graphName.value
		set(value) {
			if (StringUtils.isNotEmpty(value)) {
				graphName.translation.withTranslation(value!!)
			}
		}

	/** ---- [Storable] */

	override val storesName: Boolean get() = false

	override fun write(writer: StoreWriter) {
		super.write(writer)
		writer.writeString("uuid", graphUUID.toString())
		graphName.write("name", writer)
		writer.writeStorables("ports", getSubGraphPorts().iterator())
	}

	override fun read(reader: StoreReader) {
		super.read(reader)
		graphUUID = UUID(reader.readString("uuid"))

		if (reader.hasAttribute("name")) {
			// backward compatibility
			name = reader.readString("name")
		}
		if (reader.hasElement("name")) {
			graphName = Name.read("name", reader)
		}

		for (port in reader.readStorables<SubGraphPort<Any>>("ports")) {
			// Legacy file support. In new files, portId has always to be there!
			if (port.portId > 0) {
				addPort(port, port.portId)
			} else {
				addPort(port)
			}
		}
	}

	/** ---- [SubGraphVerticeImpl] */

	fun getSubGraphPorts(): ImmutableList<SubGraphPort<*>> =
		getPorts().map { it as SubGraphPort<*> }.toImmutableList()
}