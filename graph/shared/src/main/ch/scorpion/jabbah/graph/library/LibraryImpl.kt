package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.app.Savable
import ch.scorpion.jabbah.base.*
import ch.scorpion.jabbah.base.collection.ImmutableList
import ch.scorpion.jabbah.edit.auth.EditAuthModule
import ch.scorpion.jabbah.edit.auth.UserHolder
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.edit.model.text.description.Describable
import ch.scorpion.jabbah.edit.model.text.description.Description
import ch.scorpion.jabbah.edit.model.text.description.Name
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.MetaGraphRepository
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.model.element.ContainerLibraryElementCollector
import ch.scorpion.jabbah.graph.repository.SubGraphVerticeLocator
import ch.scorpion.jabbah.io.*

/**
 * Standard implementation of the [Library] interface.
 */
open class LibraryImpl(
	properties: LibraryProperties = LibraryProperties(),
	override val libraryService: LibraryService = LibraryModule.libraryService,
	private val storableCreator: StorableCreator = IOModule.storableCreator,
	private val objectTypeKey: String = "library.library.name",
	userHolder: UserHolder = EditAuthModule.userHolder
) : Library, LibraryDirectory, Describable {

	constructor(
		name: TranslatableText = TranslatableText(),
		libraryService: LibraryService = LibraryModule.libraryService,
		storableCreator: StorableCreator = IOModule.storableCreator,
		objectTypeKey: String = "library.library.name",
		description: TranslatableText = TranslatableText(),
		userHolder: UserHolder = EditAuthModule.userHolder
	) : this(LibraryProperties(name, description), libraryService, storableCreator, objectTypeKey, userHolder)

	constructor(
		name: String,
		libraryService: LibraryService = LibraryModule.libraryService
	) : this(TranslatableText(name), libraryService)

	companion object {
		private val LOG by logger(LibraryImpl::class)
	}

	override var uuid: UUID = System.createUUID()

	override var isSystem: Boolean = false

	override var author: UUID = userHolder.user.uuid

	override var defaultElementUUID: UUID? = null

	private var libraryFolder: LibraryFolder = LibraryFolder(properties.name)

	override var description: Description = Description(properties.description)

	init {
		libraryFolder.bindTo(this)
	}

	override fun dispose() {
		libraryFolder.dispose()
	}

	/** ---- [Any] */

	override fun toString(): String {
		return "${Translations.getString(objectTypeKey)} \"${name.value}\""
	}

	/** ---- [LibraryDirectory] */

	override val size: Int get() = libraryFolder.size

	override fun isEmpty(): Boolean = libraryFolder.isEmpty()

	override fun add(item: LibraryItem): LibraryDirectory = libraryFolder.add(item)

	override fun add(index: Int, item: LibraryItem): LibraryDirectory = libraryFolder.add(index, item)

	override fun remove(item: LibraryItem): Boolean = libraryFolder.remove(item)

	override fun contains(item: LibraryItem): Boolean = libraryFolder.contains(item)

	override fun containsRecursively(item: LibraryItem): Boolean = libraryFolder.containsRecursively(item)

	override fun get(name: String): LibraryItem? = libraryFolder.get(name)

	override fun getRecursively(name: String): LibraryItem? = libraryFolder.getRecursively(name)

	override fun getItems(): ImmutableList<LibraryItem> = libraryFolder.getItems()

	override fun indexOf(item: LibraryItem): Int = libraryFolder.indexOf(item)

	override val library: Library? get() = libraryFolder.library

	override val isFixed: Boolean get() = libraryFolder.isFixed

	override var name: Name
		get() = libraryFolder.name
		set(value) { libraryFolder.name = value }

	override val iconPath: String? get() = libraryFolder.iconPath

	override fun accept(visitor: HierarchyVisitor): Boolean {
		// Don't use libraryFolder.accept(visitor) in order to achieve that this Library is the resulting instance,
		// and not its folder (which should be transparent to the outside world)
		if (visitor.visitEnter(this)) {
			val iter = libraryFolder.getItems().iterator()
			while (iter.hasNext()) {
				if (!iter.next().accept(visitor)) {
					break
				}
			}
		}
		return visitor.visitLeave(this)
	}

	override fun bindTo(library: Library) {
		libraryFolder.bindTo(library)
	}

	override fun move(item: LibraryItem, newIndex: Int) {
		libraryFolder.move(item, newIndex)
	}

	/** ---- [MetaGraphRepository] */

	override fun graphContainsRecursively(graphUUID: UUID, graphElementUUID: UUID): Boolean {
		val metaGraph = getMetaGraph(graphUUID)
		if (metaGraph.graph.model!!.uuid == graphElementUUID) {
			return true
		}
		return SubGraphVerticeLocator(
			graph = metaGraph.graph.model!!,
			repository = this,
			storableCreator = storableCreator
		).contains(graphElementUUID)
	}

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

	/** ---- [Storable] interface */

	override var storableId: Int = 0

	override fun write(writer: StoreWriter) {
		if (defaultElementUUID != null) {
			writer.writeString("defaultElement", defaultElementUUID.toString())
		}
		writer.writeStorable("folder", libraryFolder)
		writer.writeString("uuid", uuid.toString())
		writer.writeString("author", author.toString())
		description.write("desc", writer)
		if (isSystem) {
			writer.writeBoolean("system", isSystem)
		}
	}

	override fun read(reader: StoreReader) {
		if (reader.hasAttribute("defaultElement")) {
			defaultElementUUID = UUID(reader.readString("defaultElement"))
		}
		libraryFolder = reader.readStorable("folder") as LibraryFolder
		uuid = System.createUUID(reader.readString("uuid"))
		author = System.createUUID(reader.readString("author"))
		description = Description.read("desc", reader)
		if (reader.hasAttribute("system")) {
			isSystem = reader.readBoolean("system")
		}
	}

	override fun getStorableChildren(): Iterator<Storable> {
		return mutableListOf<Storable>(libraryFolder).iterator()
	}

	override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) {
		// empty
	}

	/** ---- [Library] interface */

	override var properties: LibraryProperties
		get() = LibraryProperties(name.translation, description.translation)
		set(value) {
			name = Name(value.name)
			description = Description(value.description)
		}

	override fun replaceContentsWith(libraryFolder: LibraryFolder) {
		this.libraryFolder.replaceWith(libraryFolder)
		this.libraryFolder.defaultElementUUID = libraryFolder.defaultElementUUID
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

	/** Returns the [ContainerLibraryElement] to be opened when this [Library] is opened.*/
	override fun getDefaultElement(): ContainerLibraryElement? {
		if (defaultElementUUID == null) {
			val finder = DefaultElementFinder()
			accept(finder)
			return finder.result
		}
		val finder = GraphFinder(defaultElementUUID!!)
		accept(finder)
		return finder.result
	}

	override fun getContainerLibraryElement(uuid: UUID): ContainerLibraryElement? {
		return findContainerLibraryElementFor(uuid)
	}

	override fun containsAllRecursivelyReferencedBy(graph: Graph): Boolean {
		return ContainerLibraryElementCollector(repository = this)
			.collect(graph)
			.all { containsMetaGraph(it) }
	}

	override fun createSavable(element: ContainerLibraryElement): Savable = LibrarySavable(element)

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

	/** Returns the very first [ContainerLibraryElement] of this [Library].*/
	private class DefaultElementFinder : EmptyHierarchyVisitor() {

		/** Holds the result, if any.*/
		var result: ContainerLibraryElement? = null

		override fun visitEnter(node: Any): Boolean {
			if (result == null && node is ContainerLibraryElement) {
				result = node
				return false
			}
			return true
		}
	}
}