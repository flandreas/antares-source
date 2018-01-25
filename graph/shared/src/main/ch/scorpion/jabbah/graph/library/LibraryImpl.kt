package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.EmptyHierarchyVisitor
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.io.IOModule
import ch.scorpion.jabbah.io.StorableCreator
import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.base.logger

/**
 * Standard implementation of the [Library] interface.
 *
 * Since we want to implement the [LibraryDirectory] interface by delegation to the [libraryFolder] property,
 * we cannot exchange the property value when reading the library from persistent store, because in Kotlin
 * the delegate is bound at instantiation time. Instead, [LibraryService] loads the contents of the [Library]'s
 * [LibraryFolder] directly into the existing [LibraryFolder] instance, without instantiating it.
 *
 * @property fileName the file name under which this [Library] is stored in persistent storage
 * @property locationPath the fully qualified path (including directories, without fileName) at which this [Library] is stored
 *      On some target platforms, this property might not be used
 *
 */
class LibraryImpl(
    val fileName: String,
    val locationPath: String?,
    override val libraryFolder: LibraryFolder,
    private val storableCreator: StorableCreator,
    private val libraryService: LibraryService,
    private val eventBus: EventBus
) : Library, LibraryDirectory by libraryFolder {

    constructor(fileName: String, locationPath: String? = null): this(
        fileName, locationPath, LibraryFolder(Translations.getString("library.library.name")),
        IOModule.storableCreator, LibraryModule.libraryService, BaseModule.eventBus)

    private val libraryItemAddedHandler: EventHandler<LibraryItemAddedEvent> = { if (containsLibraryDirectory(it.parent)) store() }
    private val libraryItemRemovedHandler: EventHandler<LibraryItemRemovedEvent> = { if (containsLibraryDirectory(it.parent)) store() }
    private val libraryItemUpdatedHandler: EventHandler<LibraryItemUpdatedEvent> = { if (it.library === this) store() }

    init {
        eventBus.register(LibraryItemAddedEvent::class, libraryItemAddedHandler)
        eventBus.register(LibraryItemRemovedEvent::class, libraryItemRemovedHandler)
        eventBus.register(LibraryItemUpdatedEvent::class, libraryItemUpdatedHandler)

        libraryFolder.bindTo(this)
    }

    override fun dispose() {
        eventBus.unregister(LibraryItemAddedEvent::class, libraryItemAddedHandler)
        eventBus.unregister(LibraryItemRemovedEvent::class, libraryItemRemovedHandler)
        eventBus.unregister(LibraryItemUpdatedEvent::class, libraryItemUpdatedHandler)

        libraryFolder.dispose()
    }

    private val LOG by logger(LibraryImpl::class)

    /** ---- [Any] */

    override fun toString(): String {
        return name
    }

    /** ---- [Library] interface */

    override var isLoading: Boolean = false

    override fun getMetaGraph(uuid: UUID): MetaGraph {
        LOG.debug("LibraryImpl: Retrieve MetaGraph for UUID '${uuid.id}'")
        return findContainerLibraryElementFor(uuid)!!.openMetaGraph()
    }

    override fun getOptionalMetaGraph(uuid: UUID): MetaGraph? {
        val metaGraph = findContainerLibraryElementFor(uuid) ?: return null
        return metaGraph.openMetaGraph()
    }

    override fun containsMetaGraph(uuid: UUID): Boolean {
        return findContainerLibraryElementFor(uuid) != null
    }

    override fun load() {
        try {
            isLoading = true
            libraryService.loadLibrary(this, fileName, locationPath)
            bindLibraryItems()
        } catch (e: Throwable) {
            LOG.error("LibraryImpl: Error while loading library: ${e.message}")
            throw e
        } finally {
            isLoading = false
        }
    }

    override fun store() {
        libraryService.storeLibrary(this, fileName, locationPath)
    }

    override fun graphContainsRecursively(graphUUID: UUID, graphElementUUID: UUID): Boolean {
        val metaGraph = getMetaGraph(graphUUID)
        if (metaGraph.graph!!.model!!.uuid == graphElementUUID) {
            return true
        }
        return SubGraphVerticeLocator(
            graph = metaGraph.graph!!.model!!,
            library = this,
            storableCreator = storableCreator
        ).contains(graphElementUUID)
    }

    override fun replaceContentsWith(libraryFolder: LibraryFolder) {
        this.libraryFolder.replaceWith(libraryFolder)
    }

    /** ---- [LibraryImpl] */

    /** Determines whether this [Library] contains the specified [LibraryDirectory].*/
    private fun containsLibraryDirectory(directory: LibraryDirectory): Boolean {
        return accept(object : EmptyHierarchyVisitor() {
            override fun visitEnter(node: Any): Boolean {
                return node != directory
            }
        })
    }

    /**
     * Finds the [ContainerLibraryElement] in this [Library] which contains the [Graph] with the specified [UUID].
     */
    private fun findContainerLibraryElementFor(uuid: UUID): ContainerLibraryElement? {
        val graphFinder = GraphFinder(uuid)
        libraryFolder.accept(graphFinder)
        return graphFinder.result
    }

    /** Binds all [LibraryItem]s of this [Library] to this [Library] by calling [LibraryItem.bindTo]*/
    private fun bindLibraryItems() {
        this.accept(object : EmptyHierarchyVisitor() {
            override fun visitEnter(node: Any): Boolean {
                if (node is LibraryItem) {
                    node.bindTo(this@LibraryImpl)
                }
                return true
            }

            override fun visit(node: Any): Boolean {
                if (node is LibraryItem) {
                    node.bindTo(this@LibraryImpl)
                }
                return true
            }
        })
    }

    /**
     * Traverses the [Library] tree until it finds the [ContainerLibraryElement]
     * which contains the [Graph] with the specified [UUID], if any.
     */
    private class GraphFinder(private val uuid: UUID) : EmptyHierarchyVisitor() {

        /** Holds the result, if any.*/
        var result: ContainerLibraryElement? = null

        override fun visitEnter(node: Any): Boolean {
            if (node is ContainerLibraryElement && node.uuid == uuid) {
                result = node
                return false
            }
            return true
        }
    }
}