package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.HierarchyVisitor
import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.collection.ImmutableList
import ch.scorpion.jabbah.base.collection.toImmutableList
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.io.*

class LibraryFolder(
    name: String? = null,
    iconPath: String? = null,
    val eventBus: EventBus = BaseModule.eventBus,
    val storableCloner: StorableCloner = IOModule.storableClonerProvider.invoke(),
    val storableCreator: StorableCreator = IOModule.storableCreator,
    val libraryPersistenceService: LibraryPersistenceService = LibraryModule.libraryPersistenceService
) : AbstractLibraryItem(iconPath), LibraryDirectory {

    private val items: MutableList<LibraryItem> = mutableListOf()

    /** ---- [Any] */

    override fun toString(): String {
        return name
    }

    /** ---- [AbstractLibraryItem] */

    override val isFixed: Boolean get() = false

    override var name: String = StringUtils.orEmpty(name)

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

    override var storableId: Int = 0

    override fun write(writer: StoreWriter) {
        writer.writeString("name", name)
        writer.writeStorables("items", getStorableChildren())
    }

    override fun read(reader: StoreReader) {
        name = reader.readString("name")
        items.clear()
        for (item in reader.readStorables("items")) {
            reader.requestResolution(this, Reference(
                name = "item",
                additionalInfo = item,
                resolveAfter = listOf(item.storableId)
            ))
        }
    }

    override fun getStorableChildren(): Iterator<Storable> {
        return items.filter { !it.isFixed }.iterator()
    }

    override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) {
        if ("item" == reference.name) {
			items.add(reference.additionalInfo as LibraryItem)
		}
    }

    /** ---- [LibraryDirectory]  */

    override fun add(item: LibraryItem) {
	    items.add(findInsertIndex(item), item)
    }

    override fun remove(item: LibraryItem): Boolean {
	    return items.remove(item)
    }

    override fun contains(item: LibraryItem): Boolean {
        return items.any { it.name == item.name }
    }

    override fun get(name: String): LibraryItem? {
        return items.firstOrNull { it.name == name }
    }

    override fun getItems(): ImmutableList<LibraryItem> {
        return items.toImmutableList()
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
}