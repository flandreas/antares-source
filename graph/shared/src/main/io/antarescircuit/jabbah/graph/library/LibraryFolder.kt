package io.antarescircuit.jabbah.graph.library

import io.antarescircuit.jabbah.base.EmptyHierarchyVisitor
import io.antarescircuit.jabbah.base.HierarchyVisitor
import io.antarescircuit.jabbah.base.collection.ImmutableList
import io.antarescircuit.jabbah.base.collection.toImmutableList
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.edit.model.text.TranslatableText
import io.antarescircuit.jabbah.edit.model.text.description.Name
import io.antarescircuit.jabbah.io.*

class LibraryFolder(
	initialName: TranslatableText = TranslatableText(),
	iconPath: String? = null,
	val eventBus: EventBus = BaseModule.eventBus
) : AbstractLibraryItem(initialName, iconPath), LibraryDirectory {

	constructor(initialName: String): this(TranslatableText(initialName))

	private val items: MutableList<LibraryItem> = mutableListOf()

	/** ---- [AbstractLibraryItem] */

	override val isFixed: Boolean get() = false

	override fun accept(visitor: HierarchyVisitor): Boolean {
		if (visitor.visitEnter(this)) {
			val iter = items.iterator()
			while (iter.hasNext()) {
				if (!iter.next().accept(visitor)) {
					break
				}
			}
		}
		return visitor.visitLeave(this)
	}

	override fun dispose() {
		super.dispose()
		items.forEach { it.dispose() }
	}

	/** ---- [Storable] interface */

	override fun write(writer: StoreWriter) {
		name.write("name", writer)
		writer.writeStorables("items", items.iterator())
	}

	override fun read(reader: StoreReader) {
		name = Name.read("name", reader)
		items.clear()
		for (item in reader.readStorables<LibraryItem>("items")) {
			reader.requestResolution(this, Reference(
				name = "item",
				additionalInfo = item,
				resolveAfter = listOf(reader.getGlobalId(item))
			))
		}
	}

	override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) {
		if ("item" == reference.name) {
			items.add(reference.additionalInfo as LibraryItem)
		}
	}

	/** ---- [LibraryDirectory]  */

	override val size: Int get() = items.size

	override fun isEmpty(): Boolean = items.isEmpty()

	override fun add(item: LibraryItem): LibraryDirectory {
		items.add(findInsertIndex(item), item)
		return this
	}

	override fun add(index: Int, item: LibraryItem): LibraryDirectory {
		items.add(index, item)
		return this
	}

	override fun remove(item: LibraryItem): Boolean {
		return items.remove(item)
	}

	override fun contains(item: LibraryItem): Boolean {
		return items.any { it === item }
	}

	override fun containsRecursively(item: LibraryItem): Boolean {
		return items.any {
			when {
				it === item -> true
				it is LibraryDirectory -> it.containsRecursively(item)
				else -> false
			}
		}
	}

	override fun get(name: String): LibraryItem? {
		return items.firstOrNull { it.name.value == name }
	}

	override fun getItems(): ImmutableList<LibraryItem> {
		return items.toImmutableList()
	}

	override fun getRecursively(name: String): LibraryItem? {
		val finder = NamedItemFinder(name)
		accept(finder)
		return finder.result
	}

	override fun indexOf(item: LibraryItem): Int {
		return items.indexOf(item)
	}

	override fun move(item: LibraryItem, newIndex: Int) {
		if (!contains(item)) {
			throw IllegalArgumentException("doesn't contain item to move")
		}
		items.remove(item)
		items.add(newIndex, item)
	}

	/** ---- [LibraryFolder] */

	override fun replaceWith(libraryDirectory: LibraryDirectory) {
		items.clear()
		items.addAll(libraryDirectory.getItems())
	}

	/**
	 * Finds the index at which a [LibraryItem] is added to this [LibraryFolder].
	 * Makes sure that fixed [LibraryItem]s are inserted before all non-fixed [LibraryItem]s.
	 */
	private fun findInsertIndex(item: LibraryItem): Int {
		if (!item.isFixed) {
			return items.size
		}
		var i = 0
		while (i < items.size - 1 && items[i].isFixed) {
			i++
		}
		return i
	}

	/** Finds the first [LibraryItem] with a particular name. Note that names are not unique within a [Library].*/
	private class NamedItemFinder(private val name: String) : EmptyHierarchyVisitor() {

		/** Holds the result, if any.*/
		var result: LibraryItem? = null

		override fun visitEnter(node: Any): Boolean {
			if (result == null && node is LibraryItem && node.name.value == name) {
				result = node
				return false
			}
			return true
		}
	}
}