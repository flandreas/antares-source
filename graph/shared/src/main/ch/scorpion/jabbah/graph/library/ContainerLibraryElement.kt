package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.HierarchyVisitor
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.model.GraphElement
import ch.scorpion.jabbah.graph.view.GraphElementView
import ch.scorpion.jabbah.io.*
import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.base.logger

/**
 * A [LibraryElement] that contains a [MetaGraph].
 *
 * @property uuid the UUID of the reference [MetaGraph]
 */
class ContainerLibraryElement(
    var uuid: UUID = UUID("undefined"),
    override var name: String = "",
    iconPath: String? = null,
    val storableCloner: StorableCloner = IOModule.storableClonerProvider.invoke(),
    val storableCreator: StorableCreator = IOModule.storableCreator,
    val libraryService: LibraryService = LibraryModule.libraryService,
    val eventBus: EventBus = BaseModule.eventBus
) : LibraryElement(iconPath) {

    companion object {
        val LOG by logger()
    }

    /** Lazily initialized instance of the referenced [MetaGraph]. */
    private var metaGraph: MetaGraph? = null

    /** ---- [LibraryItem] */

    override val isFixed: Boolean get() = false

    override fun handleRemoved() {
        libraryService.deleteContainerLibraryElement(library!!, uuid)
    }

    /** ---- [Storable] */

    override fun write(writer: StoreWriter) {
        writer.writeString("name", name)
        writer.writeString("uuid", uuid.toString())

    }

    override fun read(reader: StoreReader) {
        name = reader.readString("name")
        uuid = UUID(reader.readString("uuid"))
    }

    /** ---- [LibraryElement] */

    override fun accept(visitor: HierarchyVisitor): Boolean {
        if (visitor.visitEnter(this)) {
            if (metaGraph != null) {
                metaGraph!!.accept(visitor)
            }
        }
        return visitor.visitLeave(this)
    }

    override fun <T : GraphElement> getNewInstance(): GraphElementView<T> {
        LOG.debug("Create new instance of '$name'")
        ensureMetaGraph()
        val instance = metaGraph!!.containerDrawing!!.createSubGraphVerticeViewRef()
        instance.model!!.shortDescription = metaGraph!!.graph!!.model!!.shortDescription
        if (metaGraph!!.graph!!.model!!.propagationDelay != null) {
            instance.model!!.propagationDelay = metaGraph!!.graph!!.model!!.propagationDelay!!
        }
        @Suppress("UNCHECKED_CAST")
        return instance as GraphElementView<T>
    }

    /** ---- [ContainerLibraryElement] */

    /** Returns this [ContainerLibraryElement]'s [MetaGraph], after loading from persistent store if not already loaded.*/
    fun openMetaGraph(): MetaGraph {
        ensureMetaGraph()
        return metaGraph!!
    }

    /**
     * Creates a clone of the specified [MetaGraph] and stores it as part of the persistent library state in a
     * separate file.
     * @param metaGraph the [MetaGraph] to be saved in this [ContainerLibraryElement]
     */
    fun saveMetaGraph(metaGraph: MetaGraph) {
        LOG.debug("Storing MetaGraph '${metaGraph.name}'")
        name = metaGraph.name
        this.metaGraph = storableCloner.cloneUsingCreator(metaGraph, storableCreator) as MetaGraph
        libraryService.storeMetaGraph(library!!, metaGraph)
        eventBus.post(LibraryItemUpdatedEvent(library!!, this))
    }

    /** Makes sure that [metaGraph] is loaded from persistent store.*/
    private fun ensureMetaGraph() {
        if (metaGraph == null) {
            metaGraph = libraryService.loadMetaGraph(library!!, uuid)
            LOG.debug("Loaded MetaGraph '${metaGraph!!.name}' $uuid")
        }
    }
}