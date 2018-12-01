package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.app.module.AppModule
import ch.scorpion.jabbah.app.user.UserHolder
import ch.scorpion.jabbah.base.*
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.MetaGraphRepository
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.base.collection.ImmutableList
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.graph.model.element.ContainerLibraryElementCollector
import ch.scorpion.jabbah.graph.repository.SubGraphVerticeLocator
import ch.scorpion.jabbah.io.*

/**
 * Standard implementation of the [Library] interface.
 */
open class LibraryImpl(
	properties: LibraryProperties = LibraryProperties(""),
	override val libraryService: LibraryService = LibraryModule.libraryService.invoke(),
	private val storableCreator: StorableCreator = IOModule.storableCreator,
	private val descriptionKey: String = "library.library.name",
	userHolder: UserHolder = AppModule.userHolder
) : Library, LibraryDirectory {

	constructor(
		name: String = "",
		libraryService: LibraryService = LibraryModule.libraryService.invoke(),
		storableCreator: StorableCreator = IOModule.storableCreator,
		descriptionKey: String = "library.library.name",
		userHolder: UserHolder = AppModule.userHolder
	): this(LibraryProperties(name), libraryService, storableCreator, descriptionKey, userHolder)

	companion object {
		private val LOG by logger(LibraryImpl::class)
	}

	override var uuid: UUID = System.get().createUUID()

	override var description: String? = properties.description

	override var author: UUID = userHolder.user.uuid

	override var defaultElementUUID: UUID? = null

	private var libraryFolder: LibraryFolder = LibraryFolder(properties.name)

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

	/** ---- [LibraryDirectory] */

	override var translatableName: TranslatableText
		get() = libraryFolder.translatableName
		set(value) {
			libraryFolder.translatableName = value
		}

	override fun isEmpty(): Boolean = libraryFolder.isEmpty()

	override fun add(item: LibraryItem) {
		libraryFolder.add(item)
	}

	override fun add(index: Int, item: LibraryItem) {
		libraryFolder.add(index, item)
	}

	override fun remove(item: LibraryItem): Boolean = libraryFolder.remove(item)

	override fun contains(item: LibraryItem): Boolean = libraryFolder.contains(item)

	override fun containsRecursively(item: LibraryItem): Boolean = libraryFolder.containsRecursively(item)

	override fun get(name: String): LibraryItem? = libraryFolder.get(name)

	override fun getRecursively(name: String): LibraryItem? = libraryFolder.getRecursively(name)

	override fun getItems(): ImmutableList<LibraryItem> = libraryFolder.getItems()

	override fun indexOf(item: LibraryItem): Int = libraryFolder.indexOf(item)

	override val library: Library? get() = libraryFolder.library

	override val isFixed: Boolean get() = libraryFolder.isFixed

	override var name: String
		get() = libraryFolder.name
		set(value) {
			libraryFolder.name = value
		}

	override val iconPath: String? get() = libraryFolder.iconPath

	override fun accept(visitor: HierarchyVisitor): Boolean = libraryFolder.accept(visitor)

	override fun bindTo(library: Library) {
		libraryFolder.bindTo(library)
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
		if (StringUtils.isNotEmpty(description)) {
			writer.writeString("desc", description!!)
		}
	}

	override fun read(reader: StoreReader) {
		if (reader.hasAttribute("defaultElement")) {
			defaultElementUUID = UUID(reader.readString("defaultElement"))
		}
		libraryFolder = reader.readStorable("folder") as LibraryFolder
		uuid = System.get().createUUID(reader.readString("uuid"))
		author = System.get().createUUID(reader.readString("author"))
		description = reader.readOptionalString("desc")
	}

	override fun getStorableChildren(): Iterator<Storable> {
		return mutableListOf<Storable>(libraryFolder).iterator()
	}

	override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) {
		// empty
	}

	/** ---- [Library] interface */

	override var properties: LibraryProperties
		get() = LibraryProperties(name, description)
		set(value) {
			name = value.name
			description = value.description
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