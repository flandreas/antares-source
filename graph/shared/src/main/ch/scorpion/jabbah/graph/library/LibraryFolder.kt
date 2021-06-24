package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.EmptyHierarchyVisitor
import ch.scorpion.jabbah.base.HierarchyVisitor
import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.base.collection.ImmutableList
import ch.scorpion.jabbah.base.collection.toImmutableList
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.edit.model.text.description.Name
import ch.scorpion.jabbah.io.*

class LibraryFolder(
	initialName: TranslatableText = TranslatableText(),
	iconPath: String? = null,
	val eventBus: EventBus = BaseModule.eventBus
) : AbstractLibraryItem(initialName, iconPath), LibraryDirectory {

	constructor(initialName: String): this(TranslatableText(initialName))

	private val items: MutableList<LibraryItem> = mutableListOf()

	var defaultElementUUID: UUID? = null

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
		if (defaultElementUUID != null) {
			writer.writeString("defaultElement", defaultElementUUID.toString())
		}
		writer.writeStorables("items", items.iterator())
	}

	override fun read(reader: StoreReader) {
		name = Name.read("name", reader)
		if (reader.hasAttribute("defaultElement")) {
			defaultElementUUID = UUID(reader.readString("defaultElement"))
		}
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
		return items.any { it.name == item.name }
	}

	override fun containsRecursively(item: LibraryItem): Boolean {
		return items.any {
			when {
				it.name == item.name -> true
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

	fun replaceWith(libraryFolder: LibraryFolder) {
		items.clear()
		items.addAll(libraryFolder.items)
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