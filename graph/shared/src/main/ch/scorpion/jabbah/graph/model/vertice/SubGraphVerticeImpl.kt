package ch.scorpion.jabbah.graph.model.vertice

import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.Language
import ch.scorpion.jabbah.base.collection.ImmutableList
import ch.scorpion.jabbah.base.collection.toImmutableList
import ch.scorpion.jabbah.graph.container.ContainerDrawing
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StorableCreator
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter
import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.base.exception.UnsupportedOperationException
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.edit.model.text.Translation
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.MetaGraphRepository
import ch.scorpion.jabbah.graph.model.*

/**
 * A [SubGraphVertice] implementation that is used as the model class of a [ContainerDrawing].
 * They are used as a blueprint when creating [SubGraphVerticeRef]s that reference a particular [Graph].
 *
 * Extends [AbstractVertice] for its [Port] management functionality.
 */
class SubGraphVerticeImpl(
	name: String = Translations.getString("graph.name.unknown")
) : AbstractVertice("graph.element.container", name), SubGraphVertice {

    companion object {
        private val LOG by logger(SubGraphVerticeImpl::class)
    }

    /* ---- [SubGraphVertice] */

	/** Used when [SubGraphVerticeRef]s are created from this [SubGraphVerticeImpl].*/
    override var graphUUID: UUID? = null

	override var translatableName = TranslatableText(name)

	override var shortDescription: String?
		get() = translatableDescription.getTranslation()
		set(value) {
			if (StringUtils.isNotBlank(value)) {
				translatableDescription = translatableDescription.withTranslation(value!!)
			}
		}

	override var translatableDescription: TranslatableText = TranslatableText()

    override fun getGraphIfPresent(): Graph? {
        return null
    }

    override fun getGraph(repository: MetaGraphRepository, storableCreator: StorableCreator): Graph {
        throw UnsupportedOperationException()
    }

    override fun <T : Any> propagateOutput(outputPort: SubGraphOutputPort<T>, signal: T, signalHandler: SignalHandler) {
        // Not needed here
    }

	/** ---- [AbstractVertice] */

	/** Represents the [translatableName] in the current system [Language].*/
	override var name: String?
		get() = translatableName.getTranslation()
		set(value) {
			if (StringUtils.isNotEmpty(value)) {
				translatableName = translatableName.withTranslation(value!!)
			}
		}

    /** ---- [Storable] */

	override val storesName: Boolean get() = false

    override fun write(writer: StoreWriter) {
        super.write(writer)
        writer.writeString("uuid", graphUUID.toString())
	    writer.writeStorables("name", translatableName.allTranslations())
	    if (!translatableDescription.isEmpty) {
		    writer.writeStorables("desc", translatableDescription.allTranslations())
	    }
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
		    translatableName = TranslatableText(reader.readStorables("name").map { it as Translation })
	    }
	    if (reader.hasElement("desc")) {
		    translatableDescription = TranslatableText(reader.readStorables("desc").map { it as Translation })
	    }

        for (port in reader.readStorables("ports").map { it as SubGraphPort<Any> }) {
            LOG.debug("SubGraphVerticeImpl: reading and adding SubCircuitPort $port")
            // Legacy file support. In new files, portId has always to be there!
            if (port.portId > 0) {
                addPort(port, port.portId)
            } else {
                addPort(port)
            }
        }
    }

    override fun getStorableChildren(): Iterator<Storable> {
        val list = mutableListOf<Storable>()
        list.addAll(getSubGraphPorts())
        return list.iterator()
    }

    /** ---- [SubGraphVerticeImpl] */

    private fun getSubGraphPorts(): ImmutableList<SubGraphPort<Any>> {
        return getPorts().map { it as SubGraphPort<Any>}.toImmutableList()
    }
}