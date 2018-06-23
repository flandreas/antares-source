package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.collection.ImmutableList

interface LibraryDirectory : LibraryItem {

	fun add(item: LibraryItem)

	/**
	 * Removed a [LibraryItem] from this [LibraryDirectory].
	 * @return `true` if [item] has been removed, `false` if it wasn't contained in this [LibraryDirectory]
	 */
	fun remove(item: LibraryItem): Boolean

	fun contains(item: LibraryItem): Boolean

	/** Determines whether this [LibraryDirectory] contains the specified [LibraryDirectory]. */
	fun containsRecursively(item: LibraryItem): Boolean

	/** Returns the directly contained [LibraryItem] with the specified name. */
	fun get(name: String): LibraryItem?

	/** Returns the directly or recursively contained [LibraryItem] with the specified name. */
	fun getRecursively(name: String): LibraryItem?

	fun getItems(): ImmutableList<LibraryItem>
}

