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
 */
class LibraryImpl(
    name: String,
	override val libraryFolder: LibraryFolder = LibraryFolder(name),
    override val libraryService: LibraryService,
	private val storableCreator: StorableCreator = IOModule.storableCreator,
    private val descriptionKey: String = "library.library.name"
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
        return "${Translations.getString(descriptionKey)} \"$name\""
    }

    /** ---- [Library] interface */

    override fun getMetaGraph(uuid: UUID): MetaGraph {
        LOG.debug("LibraryImpl: Retrieve MetaGraph for UUID '${uuid.id}'")
	    return libraryService.getMetaGraph(this, findContainerLibraryElementFor(uuid)!!)
    }

    override fun getOptionalMetaGraph(uuid: UUID): MetaGraph? {
        val element = findContainerLibraryElementFor(uuid) ?: return null
	    return libraryService.getMetaGraph(this, element)
    }

    override fun containsMetaGraph(uuid: UUID): Boolean {
        return findContainerLibraryElementFor(uuid) != null
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

    override fun bindLibraryItems() {
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