package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.Language
import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.collection.ImmutableList
import ch.scorpion.jabbah.edit.model.text.TranslatableText

/**
 * A [LibraryDirectory] is a [LibraryItem] that contains other [LibraryItem]s, hence representing an
 * application of the composite design pattern.
 */
interface LibraryDirectory : LibraryItem {

	/** Contains the displayable name of this [LibraryDictionary] in the [System]'s current [Language].*/
	override var name: String

	/** Contains the displayable name of this [LibraryDictionary] as various translations.*/
	var translatableName: TranslatableText

	/** Returns the number of [LibraryItem]s in this [LibraryDirectory].*/
	val size: Int

	fun isEmpty(): Boolean

	fun add(item: LibraryItem)

	fun add(index: Int, item: LibraryItem)

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

	/**
	 * Returns the index of the specified [LibraryItem] in this [LibraryDirectory] (starting with 0),
	 * or `-1` if not contained.
	 */
	fun indexOf(item: LibraryItem): Int

	/**
	 * Moves a [LibraryItem] to a new index within this [LibraryDirectory].
	 * @param item the [LibraryItem] to be moved within this [LibraryDirectory]
	 * @param newIndex the index to insert `item` after it has been added
	 * @throws IllegalArgumentException if `item` is not contained
	 */
	fun move(item: LibraryItem, newIndex: Int)
}

