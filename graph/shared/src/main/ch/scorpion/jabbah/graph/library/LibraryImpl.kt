package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.app.Savable
import ch.scorpion.jabbah.base.*
import ch.scorpion.jabbah.edit.auth.EditAuthModule
import ch.scorpion.jabbah.edit.auth.User
import ch.scorpion.jabbah.edit.auth.UserHolder
import ch.scorpion.jabbah.edit.auth.UserIdentity
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.edit.model.text.description.Describable
import ch.scorpion.jabbah.edit.model.text.description.Description
import ch.scorpion.jabbah.edit.model.text.description.Name
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.MetaGraphRepository
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.model.element.ContainerLibraryElementCollector
import ch.scorpion.jabbah.io.*

/**
 * Standard implementation of the [Library] interface.
 */
open class LibraryImpl(
	properties: LibraryProperties = LibraryProperties(),
	override val libraryService: LibraryService = LibraryModule.libraryService,
	private val objectTypeKey: String = "library.library.name",
	userHolder: UserHolder<User> = EditAuthModule.userHolder
) : AbstractStorable(), Library, Describable {

	constructor(
		name: TranslatableText = TranslatableText(),
		libraryService: LibraryService = LibraryModule.libraryService,
		objectTypeKey: String = "library.library.name",
		description: TranslatableText = TranslatableText(),
		userHolder: UserHolder<User> = EditAuthModule.userHolder
	) : this(LibraryProperties(name, description), libraryService, objectTypeKey, userHolder)

	constructor(
		name: String,
		libraryService: LibraryService = LibraryModule.libraryService
	) : this(TranslatableText(name), libraryService)

	companion object {
		private val LOG by logger(LibraryImpl::class)
	}

	override var uuid: UUID = System.createUUID()

	override var isSystem: Boolean = false

	override var author: UserIdentity = userHolder.user.identity

	override var defaultElementUUID: UUID? = null

	override var visibility: LibraryVisibility = LibraryVisibility.Private

	override var directory: LibraryDirectory = LibraryFolder(properties.name)

	override var description: Description = Description(properties.description)

	override val metaGraphCount: Int get() =
		MetaGraphCounter().run {
			accept(this)
			result
		}

	init {
		directory.bindTo(this)
	}

	override fun dispose() {
		directory.dispose()
	}

	/** ---- [Any] */

	override fun toString(): String {
		return "${Translations.getString(objectTypeKey)} \"${name.value}\""
	}

	/** ---- [LibraryDirectory] */

	override var name: Name
		get() = directory.name
		set(value) { directory.name = value }

	fun accept(visitor: HierarchyVisitor): Boolean {
		// Don't use libraryFolder.accept(visitor) in order to achieve that this Library is the resulting instance,
		// and not its folder (which should be transparent to the outside world)
		if (visitor.visitEnter(this)) {
			val iter = directory.getItems().iterator()
			while (iter.hasNext()) {
				if (!iter.next().accept(visitor)) {
					break
				}
			}
		}
		return visitor.visitLeave(this)
	}

	/** ---- [MetaGraphRepository] */

	override fun getMetaGraph(uuid: UUID): MetaGraph {
		val metaGraph = libraryService.getMetaGraph(this, findContainerLibraryElementFor(uuid)!!)
		LOG.trace("Retrieved MetaGraph for UUID '${uuid.id}' with ID ${metaGraph.hashCode()}")
		return metaGraph
	}

	override fun getOptionalMetaGraph(uuid: UUID): MetaGraph? {
		val element = findContainerLibraryElementFor(uuid) ?: return null
		return libraryService.getMetaGraph(this, element)
	}

	override fun containsMetaGraph(uuid: UUID): Boolean =
		findContainerLibraryElementFor(uuid) != null

	/** ---- [Storable] interface */

	override fun write(writer: StoreWriter) {
		if (defaultElementUUID != null) {
			writer.writeString("defaultElement", defaultElementUUID.toString())
		}
		writer.writeStorable("folder", directory)
		writer.writeString("uuid", uuid.toString())
		writer.writeString("author", author.toString())
		description.write("desc", writer)
		if (isSystem) {
			writer.writeBoolean("system", isSystem)
		}
		if (visibility != LibraryVisibility.Private) {
			writer.writeString("visibility", visibility.customName)
		}
	}

	override fun read(reader: StoreReader) {
		if (reader.hasAttribute("defaultElement")) {
			defaultElementUUID = UUID(reader.readString("defaultElement"))
		}
		directory = reader.readStorable("folder") as LibraryFolder
		uuid = System.createUUID(reader.readString("uuid"))
		author = UserIdentity(reader.readString("author"))
		description = Description.read("desc", reader)
		if (reader.hasAttribute("system")) {
			isSystem = reader.readBoolean("system")
		}
		if (reader.hasAttribute("visibility")) {
			visibility = LibraryVisibility.withName(reader.readString("visibility"))
		}
	}

	override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) { }

	/** ---- [Library] interface */

	override var properties: LibraryProperties
		get() = LibraryProperties.ofLibrary(this)
		set(value) {
			name = Name(value.name)
			description = Description(value.description)
			visibility = value.visibility
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

	/**
	 * Finds the [ContainerLibraryElement] in this [Library] which contains the [Graph] with the specified [UUID].
	 */
	private fun findContainerLibraryElementFor(uuid: UUID): ContainerLibraryElement? {
		val graphFinder = GraphFinder(uuid)
		directory.accept(graphFinder)
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
			return node is LibraryDirectory
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

	private class MetaGraphCounter : EmptyHierarchyVisitor() {
		var result: Int = 0

		override fun visitEnter(node: Any): Boolean {
			if (node is ContainerLibraryElement) {
				result++
			}
			return true
		}
	}
}