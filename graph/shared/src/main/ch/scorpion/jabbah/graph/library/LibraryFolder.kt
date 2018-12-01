package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.EmptyHierarchyVisitor
import ch.scorpion.jabbah.base.HierarchyVisitor
import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.base.collection.ImmutableList
import ch.scorpion.jabbah.base.collection.toImmutableList
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.edit.model.text.Translation
import ch.scorpion.jabbah.io.*

class LibraryFolder(
    name: String? = null,
    iconPath: String? = null,
    val eventBus: EventBus = BaseModule.eventBus
) : AbstractLibraryItem(iconPath), LibraryDirectory {

    private val items: MutableList<LibraryItem> = mutableListOf()

	override var translatableName = TranslatableText()

	var defaultElementUUID: UUID? = null

	/** ---- [Any] */

    override fun toString(): String {
        return name
    }

    /** ---- [AbstractLibraryItem] */

    override val isFixed: Boolean get() = false

	override var name: String
		get() = translatableName.getTranslation()
		set(value) {
			if (StringUtils.isNotEmpty(value)) {
				translatableName.setTranslation(value)
			}
		}

	init {
		this.name = StringUtils.orEmpty(name)
	}

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
	    writer.writeStorables("name", translatableName.allTranslations())

	    if (defaultElementUUID != null) {
		    writer.writeString("defaultElement", defaultElementUUID.toString())
	    }
        writer.writeStorables("items", getStorableChildren())
    }

    override fun read(reader: StoreReader) {
	    if (reader.hasAttribute("name")) {
		    // backward compatibility
		    name = reader.readString("name")
	    }
	    if (reader.hasElement("name")) {
		    translatableName = TranslatableText(reader.readStorables("name").map { it as Translation })
	    }

	    if (reader.hasAttribute("defaultElement")) {
		    defaultElementUUID = UUID(reader.readString("defaultElement"))
	    }
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
	    return items.iterator()
    }

    override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) {
        if ("item" == reference.name) {
			items.add(reference.additionalInfo as LibraryItem)
		}
    }

    /** ---- [LibraryDirectory]  */

	override fun isEmpty(): Boolean = items.isEmpty()

    override fun add(item: LibraryItem) {
	    items.add(findInsertIndex(item), item)
    }


	override fun add(index: Int, item: LibraryItem) {
		items.add(index, item)
	}

    override fun remove(item: LibraryItem): Boolean {
	    return items.remove(item)
    }

    override fun contains(item: LibraryItem): Boolean {
        return items.any { it.name == item.name }
    }

	override fun containsRecursively(item: LibraryItem): Boolean {
		return items.any {
			if (it.name == item.name) {
				true
			} else if (it is LibraryDirectory) {
				it.containsRecursively(item)
			} else {
				false
			}
		}
	}

    override fun get(name: String): LibraryItem? {
        return items.firstOrNull { it.name == name }
    }

    override fun getItems(): ImmutableList<LibraryItem> {
        return items.toImmutableList()
    }

	override fun getRecursively(name: String): LibraryItem? {
		val finder=  NamedItemFinder(name)
		accept(finder)
		return finder.result
	}

	override fun indexOf(item: LibraryItem): Int {
		return items.indexOf(item)
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
			if (result == null && node is LibraryItem && node.name == name) {
				result = node
				return false
			}
			return true
		}
	}
}