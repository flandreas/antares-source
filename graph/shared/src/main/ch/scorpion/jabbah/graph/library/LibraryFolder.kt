package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.HierarchyVisitor
import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.collection.ImmutableList
import ch.scorpion.jabbah.base.collection.toImmutableList
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.model.GraphElement
import ch.scorpion.jabbah.graph.view.GraphElementView
import ch.scorpion.jabbah.io.*
import kotlin.reflect.KClass

class LibraryFolder(
    name: String?,
    iconPath: String?,
    val eventBus: EventBus,
    val storableCloner: StorableCloner,
    val storableCreator: StorableCreator,
    val libraryService: LibraryService
) : AbstractLibraryItem(iconPath), LibraryDirectory {

    constructor(name: String?): this(name, null, BaseModule.eventBus, IOModule.storableClonerProvider.invoke(), IOModule.storableCreator, LibraryModule.libraryService)
    @Suppress("unused") constructor(): this(null)

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

    override fun handleRemoved() {
        throw UnsupportedOperationException("removing of entire LibraryFolders is not yet implemented")
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

    override fun addBaseElement(name: String, translationKey: String, iconPath: String?, storableCreator: StorableCreator?, clazz: KClass<out GraphElementView<*>>): BaseLibraryElement {
        val elem = BaseLibraryElement(name, translationKey, iconPath, storableCreator, clazz)
        add(elem)
        return elem
    }

    override fun addBaseElement(name: String, translationKey: String, iconPath: String?, supplier: () -> GraphElementView<out GraphElement>): BaseLibraryElement {
        val elem = BaseLibraryElement(name, translationKey, iconPath, null, null, supplier)
        add(elem)
        return elem
    }

    /** ---- [LibraryDirectory]  */

    override fun add(item: LibraryItem) {
        item.bindTo(library!!)
        items.add(findInsertIndex(item), item)
        eventBus.post(LibraryItemAddedEvent(this, item))
    }

    override fun remove(item: LibraryItem) {
        if (items.contains(item)) {
            item.handleRemoved()
            item.dispose()
            items.remove(item)
            eventBus.post(LibraryItemRemovedEvent(this, item))
        }
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

    override fun addContainerElement(metaGraph: MetaGraph): ContainerLibraryElement {
        val elem = ContainerLibraryElement(
            uuid = metaGraph.uuid,
            name = metaGraph.name,
            iconPath = null,
            storableCloner = storableCloner,
            storableCreator = storableCreator,
            libraryService = libraryService,
            eventBus = eventBus)
        elem.bindTo(library!!)
        elem.saveMetaGraph(metaGraph)
        add(elem)
        return elem
    }

    override fun addFolder(name: String): LibraryFolder {
        return addFolderImpl(name)
    }

    /** ---- [LibraryFolder] */

    fun replaceWith(libraryFolder: LibraryFolder) {
        items.clear()
        items.addAll(libraryFolder.items)
    }

    private fun addFolderImpl(name: String): LibraryFolder {
        val folder = LibraryFolder(name)
        add(folder)
        return folder
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