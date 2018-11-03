package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.*
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.MetaGraphRepository
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.base.collection.ImmutableList
import ch.scorpion.jabbah.graph.repository.SubGraphVerticeLocator
import ch.scorpion.jabbah.io.*

/**
 * Standard implementation of the [Library] interface.
 *
 * Since we want to implement the [LibraryDirectory] interface by delegation to the [libraryFolder] property,
 * we cannot exchange the property value when reading the library from persistent store, because in Kotlin
 * the delegate is bound at instantiation time. Instead, [LibraryPersistenceService] loads the contents of the [Library]'s
 * [LibraryFolder] directly into the existing [LibraryFolder] instance, without instantiating it.
 */
open class LibraryImpl(
	name: String = "",
	override val libraryService: LibraryService = LibraryModule.libraryService.invoke(),
	private val storableCreator: StorableCreator = IOModule.storableCreator,
	private val descriptionKey: String = "library.library.name"
) : Library, LibraryDirectory {

	companion object {
		private val LOG by logger(LibraryImpl::class)
	}

	// override var uuid: UUID = System.get().createUUID()

	/*
	override var importedLibrary: UUID? = null
		private set
	*/

	override var libraryFolder: LibraryFolder = LibraryFolder(name)

	/**
	 * Can't be stored locally, because delegation would not work any more (cannot change delegated object).
	 * As a workaround, store it in [LibraryFolder].
	 */
	override var defaultElementUUID: UUID? = null

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

	override val name: String get() = libraryFolder.name

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
		/*
		writer.writeString("uuid", uuid.toString())
		if (importedLibrary != null) {
			writer.writeString("import", importedLibrary.toString())
		}
		libraryFolder.write(writer)
		*/
	}

	override fun read(reader: StoreReader) {
		if (reader.hasAttribute("defaultElement")) {
			defaultElementUUID = UUID(reader.readString("defaultElement"))
		}
		libraryFolder = reader.readStorable("folder") as LibraryFolder
		/*
		uuid = System.get().createUUID(reader.readString("uuid"))
		if (reader.hasAttribute("import")) {
			importedLibrary = System.get().createUUID(reader.readString("import"))
		}
		libraryFolder.read(reader)
		*/
	}

	override fun getStorableChildren(): Iterator<Storable> {
		return mutableListOf<Storable>(libraryFolder).iterator()
	}

	override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) {
		// empty
	}

	/** ---- [Library] interface */

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

	/** Returns the [ContainerLibraryElement] to be opened when this [Library] is openend.*/
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