package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.EmptyHierarchyVisitor
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
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
 * the delegate is bound at instantiation time. Instead, [LibraryPersistenceService] loads the contents of the [Library]'s
 * [LibraryFolder] directly into the existing [LibraryFolder] instance, without instantiating it.
 *
 * @property fileName the file name under which this [Library] is stored in persistent storage
 * @property locationPath the fully qualified path (including directories, without fileName) at which this [Library] is stored.
 *      On some target platforms, this property might not be used
 *
 */
class LibraryImpl(
	override val fileName: String,
	override val locationPath: String? = null,
	override val libraryFolder: LibraryFolder = LibraryFolder(Translations.getString("library.library.name")),
	private val storableCreator: StorableCreator = IOModule.storableCreator,
	private val libraryPersistenceService: LibraryPersistenceService = LibraryModule.libraryPersistenceService,
	private val eventBus: EventBus = BaseModule.eventBus
) : Library, LibraryDirectory by libraryFolder {

	companion object {
		private val LOG by logger(LibraryImpl::class)
	}

    init {
        libraryFolder.bindTo(this)
    }

    override fun dispose() {
        libraryFolder.dispose()
    }

    /** ---- [Any] */

    override fun toString(): String {
        return name
    }

    /** ---- [Library] interface */

    override var isLoading: Boolean = false

    override fun getMetaGraph(uuid: UUID, service: LibraryService): MetaGraph {
        LOG.debug("LibraryImpl: Retrieve MetaGraph for UUID '${uuid.id}'")
	    return service.getMetaGraph(this, findContainerLibraryElementFor(uuid)!!)
    }

    override fun getOptionalMetaGraph(uuid: UUID, service: LibraryService): MetaGraph? {
        val element = findContainerLibraryElementFor(uuid) ?: return null
	    return service.getMetaGraph(this, element)
    }

    override fun containsMetaGraph(uuid: UUID): Boolean {
        return findContainerLibraryElementFor(uuid) != null
    }

    override fun load() {
        try {
            isLoading = true
            libraryPersistenceService.loadLibrary(this, fileName, locationPath)
            bindLibraryItems()
        } catch (e: Throwable) {
            LOG.error("LibraryImpl: Error while loading library: ${e.message}")
            throw e
        } finally {
            isLoading = false
        }
    }

    override fun graphContainsRecursively(graphUUID: UUID, graphElementUUID: UUID, service: LibraryService): Boolean {
        val metaGraph = getMetaGraph(graphUUID, service)
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

    /** Binds all [LibraryItem]s of this [Library] to this [Library] by calling [LibraryItem.bindTo]. */
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