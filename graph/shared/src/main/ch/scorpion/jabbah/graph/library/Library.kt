package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.graph.model.GraphElement
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.base.UUID


interface Library : LibraryDirectory {

	/** The file name under which this [Library] is stored in persistent storage. */
	val fileName: String

	/**
	 * The fully qualified path (including directories, without fileName) at which this [Library] is stored.
	 * On some target platforms, this property might not be used
	 **/
	val locationPath: String?

    val libraryFolder: LibraryFolder

    /** Determines whether this [Library] is currently loading from persistent store.*/
    val isLoading: Boolean


    /** Returns the entire [MetaGraph] with the specified [UUID], including the view representations. */
    fun getMetaGraph(uuid: UUID, service: LibraryService = LibraryModule.libraryService.invoke()): MetaGraph

    /** Returns the entire [MetaGraph] with the specified [UUID] if it exists, or `null` otherwise. */
    fun getOptionalMetaGraph(uuid: UUID, service: LibraryService = LibraryModule.libraryService.invoke()): MetaGraph?

    /** Checks whether a [MetaGraph] with [uuid] exists in this [Library]. */
    fun containsMetaGraph(uuid: UUID): Boolean

    /** Replaces the contents of this [Library] with the content of the specified [LibraryFolder].*/
    fun replaceContentsWith(libraryFolder: LibraryFolder)

    fun load()

    /**
     * Determines whether a [Graph] contains directly or recursively a [GraphElement]
     * with the specified UUID.
     */
    fun graphContainsRecursively(graphUUID: UUID, graphElementUUID: UUID, service: LibraryService = LibraryModule.libraryService.invoke()): Boolean

}
