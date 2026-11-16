package io.antarescircuit.antares.web

import io.antarescircuit.antares.AntaresApplication
import io.antarescircuit.antares.model.AntaresGraphTypes
import io.antarescircuit.jabbah.edit.model.text.TranslatableText
import io.antarescircuit.jabbah.edit.SnapResult
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.UUID
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.base.richtext.RichText
import io.antarescircuit.jabbah.draw.view.CanvasJs
import io.antarescircuit.jabbah.draw.view.DragTargetHandler
import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.edit.module.EditModule
import io.antarescircuit.jabbah.edit.auth.UserIdentity
import io.antarescircuit.jabbah.base.geom.Rotation
import io.antarescircuit.jabbah.graph.MetaGraph
import io.antarescircuit.jabbah.graph.library.*
import io.antarescircuit.jabbah.graph.model.image.ImageLibraryElement
import io.antarescircuit.jabbah.graph.project.Project
import io.antarescircuit.jabbah.graph.project.ProjectModule
import io.antarescircuit.jabbah.graph.view.GraphElementView
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.module.GraphViewModule
import io.antarescircuit.jabbah.io.DomXmlReader
import io.antarescircuit.jabbah.io.StorableCloner
import io.antarescircuit.jabbah.io.StoreXmlReader
import org.w3c.dom.DragEvent

/**
 * Library-related operations of the web editor: the component palette, placing components,
 * and managing projects and their circuits.
 */
internal class WebLibrary(
	internal val store: WebFileStore,
	private val localUser: UserIdentity,
) {
	private val holder get() = LibraryModule.libraryHolder

	/** Library elements of the palette and project, by key. Rebuilt with every [palette] and [circuits] call. */
	private val elements = mutableMapOf<String, LibraryElement>()

	private fun keyOf(library: Library, element: LibraryElement): String = when (element) {
		is BaseLibraryElement -> "${library.uuid.id}|base:${element.id}"
		is ContainerLibraryElement -> "${library.uuid.id}|circuit:${element.uuid.id}"
		is ImageLibraryElement -> "${library.uuid.id}|image:${element.storable.uuid.id}"
		else -> "${library.uuid.id}|${element.name.value}"
	}

	/** Returns the key of [element] of [library] and makes it available to [element]. */
	internal fun keyFor(library: Library, element: LibraryElement): String = keyOf(library, element).also { elements[it] = element }

	private fun plainName(item: LibraryItem): String = RichText.stripToPlainText(item.name.value)

	/** ---- Palette */

	fun palette(): String {
		val project = holder.l ?: return "[]"
		val libraries = project.expandedImports.libraries.filter { it.uuid != project.uuid }
		return toJson(libraries.mapNotNull { lib -> node(lib, lib) }.toJsArray())
	}

	private fun node(library: Library, item: LibraryItem): dynamic = when (item) {
		is LibraryDirectory -> {
			val children = item.getItems().mapNotNull { node(library, it) }
			if (children.isEmpty()) null else obj {
				kind = "folder"
				name = plainName(item)
				this.children = children.toJsArray()
			}
		}
		is LibraryElement -> {
			val key = keyOf(library, item)
			elements[key] = item
			obj {
				kind = if (item is ContainerLibraryElement) "circuit" else "component"
				this.key = key
				name = plainName(item)
				type = item.graphType.toString()
			}
		}
		else -> null
	}

	/** Returns the [LibraryElement] for a palette or project circuit [key]. */
	fun element(key: String): LibraryElement? = elements[key]

	fun info(key: String): String {
		val element = element(key) ?: return "null"
		val description = when (element) {
			is ContainerLibraryElement -> try {
				element.library?.libraryService?.getMetaGraph(element.library!!, element)?.graph?.model?.description?.value
			} catch (_: Throwable) {
				null
			}
			else -> null
		}
		return toJson(obj {
			name = plainName(element)
			this.description = description?.let { RichText.stripToPlainText(it) } ?: ""
		})
	}

	/**
	 * Checks whether [element] can be placed into the circuit edited by [editor],
	 * like the desktop does for drag and drop. Returns an error message, or `null` if it can be placed.
	 */
	fun checkPlaceable(element: LibraryElement, editor: Editor): String? {
		val graph = (editor.drawing as GraphView).graph ?: return null
		graph.type.checkImport(element)?.let { return it }
		if (element is ContainerLibraryElement) {
			if (element.uuid == graph.uuid || holder.graphContainsRecursively(element.uuid, graph.uuid)) {
				return Translations.getString("graph.cycleError.msg")
			}
			val target = holder.getContainingLibrary(graph.uuid)
			val source = element.library
			if (target != null && source != null && !target.expandedImports.contains(source.uuid)) {
				return Translations.getString("graph.dependencyError.msg")
			}
		}
		return null
	}

	/**
	 * Lets [canvas] accept palette items dragged from the web application, showing a live preview while dragging.
	 * The web application sets the HTML drag data type `application/x-antares-component` to the item key.
	 */
	fun bindDropTarget(canvas: CanvasJs, editor: () -> Editor, canPlace: () -> Boolean) {
		canvas.dragTargetHandler = object : DragTargetHandler() {
			private var key: String? = null
			private var component: GraphElementView<*>? = null

			private fun currentKey(event: DragEvent): String? =
				event.dataTransfer?.types?.firstOrNull { it.startsWith(DRAG_TYPE_PREFIX) }
					?.removePrefix(DRAG_TYPE_PREFIX)
					?.let { decodeKey(it) }

			override fun onDragEnter(event: DragEvent, viewLocation: Point2D) = onDragOver(event, viewLocation)

			override fun onDragOver(event: DragEvent, viewLocation: Point2D) {
				val newKey = currentKey(event) ?: return
				if (!canPlace()) return
				event.preventDefault()
				val e = editor()
				if (newKey != key || component == null) {
					key = newKey
					val element = element(newKey) ?: return
					if (checkPlaceable(element, e) != null) {
						component = null
						return
					}
					component = try {
						element.getNewInstance<io.antarescircuit.jabbah.graph.model.GraphElement>().also {
							EditModule.drawingAppService.customizeAddedComponent(it, e.view.drawing)
						}
					} catch (t: Throwable) {
						null
					}
				}
				component?.let { e.dragManager.setDropComponent(it, e.view.viewToModel(viewLocation)) }
			}

			override fun onDragLeave(event: DragEvent) {
				reset()
			}

			override fun onDrop(event: DragEvent, viewLocation: Point2D) {
				val k = currentKey(event)
				val dropped = component
				val e = editor()
				reset()
				if (k == null || !canPlace()) return
				event.preventDefault()
				val element = element(k) ?: return
				checkPlaceable(element, e)?.let {
					throw IllegalArgumentException(it)
				}
				val location = dropped?.location ?: e.view.viewToModel(viewLocation).let { p ->
					val r = SnapResult()
					e.view.grid.snap(p.x, p.y, r)
					p.add(r.dx, r.dy)
				}
				val rotation = if (dropped?.useRotation == true) dropped.rotation else Rotation.R0
				GraphViewModule.graphViewAppService.addGraphElementViewFromLibrary(element, location, rotation, e)
				canvas.requestViewFocus()
			}

			private fun reset() {
				if (component != null) {
					editor().dragManager.setDropComponent(null, null)
				}
				component = null
				key = null
			}
		}
	}

	/** ---- Projects */

	fun projects(): String {
		val entries = ProjectModule.projectManagementService.getProjectDirectoryEntries()
		return toJson(entries.map { entry ->
			obj {
				uuid = entry.uuid.id
				name = entry.name.value
			}
		}.sortedBy { (it.name as String).lowercase() }.toJsArray())
	}

	fun createProject(name: String): Project {
		val metaGraph = MetaGraph.create(TranslatableText(Translations.getString("graph.name.unknown")), AntaresGraphTypes.Digital)
		return ProjectModule.projectManagementService.create(
			LibraryProperties(TranslatableText(name.trim()), importUuid = AntaresApplication.DEF_LIBRARY_UUID),
			metaGraph
		)
	}

	fun renameProject(uuid: UUID, name: String) {
		val open = holder.l as? Project
		if (open?.uuid == uuid) {
			ProjectModule.projectManagementService.update(LibraryProperties.ofLibrary(open).copy(name = TranslatableText(name.trim())))
		} else {
			val project = ProjectModule.projectManagementService.load(LibraryIdentification(uuid, localUser))
			project.libraryService.renameLibrary(project, TranslatableText(name.trim()))
			ProjectModule.projectDictionaryService.rename(project, TranslatableText(name.trim()))
		}
	}

	fun deleteProject(uuid: UUID) {
		val prefix = "${WebPersistence.PROJECTS}/${uuid.id}/"
		ProjectModule.projectDictionaryService.remove(uuid)
		filesOf(prefix).forEach { store.remove(it) }
	}

	/** Lists the paths of all known files below [prefix], based on the library's circuits and images. */
	internal fun filesOf(prefix: String): List<String> {
		val xml = store.read("${prefix}library.xml") ?: return emptyList()
		val library = StoreXmlReader(DomXmlReader(xml)).readStorable() as Library
		return fileNamesOf(library).map { "$prefix$it" } + "${prefix}library.xml"
	}

	/** Names of the files of all circuits (`<uuid>.cir`) and images (`<uuid>.<extension>`) in [directory]. */
	internal fun fileNamesOf(directory: LibraryDirectory): List<String> =
		directory.getItems().flatMap {
			when (it) {
				is ContainerLibraryElement -> listOf("${it.uuid.id}.cir")
				is ImageLibraryElement -> listOf(WebLibraryPersistenceService.imageFileName(it.storable.uuid, it.storable.imageType))
				is LibraryDirectory -> fileNamesOf(it)
				else -> emptyList()
			}
		}

	fun exportProject(project: Project): String {
		val prefix = "${WebPersistence.PROJECTS}/${project.uuid.id}/"
		val files = obj {}
		filesOf(prefix).forEach { path -> files[path.removePrefix(prefix)] = store.read(path) }
		return toJson(obj {
			format = EXPORT_FORMAT
			version = 1
			uuid = project.uuid.id
			name = project.name.value
			this.files = files
		})
	}

	/** Imports a project exported by [exportProject]. An existing project with the same UUID is replaced. */
	fun importProject(json: String): String {
		val data: dynamic = JSON.parse(json)
		if (data.format != EXPORT_FORMAT) {
			throw IllegalArgumentException(Translations.getString("web.import.invalid"))
		}
		val files: dynamic = data.files
		val names = js("Object.keys")(files).unsafeCast<Array<String>>()
		return addProject(UUID(data.uuid as String)) { name -> if (names.contains(name)) files[name] as String else null }
	}

	/** Imports the sample project [uuid] (whose files the web application has put into the store below `samples/`). */
	fun importSample(uuid: UUID): String {
		if (ProjectModule.projectDictionaryService.contains(uuid)) {
			return uuid.id
		}
		return addProject(uuid) { name -> store.read("samples/${uuid.id}/$name") }
	}

	private fun addProject(uuid: UUID, read: (name: String) -> String?): String {
		val prefix = "${WebPersistence.PROJECTS}/${uuid.id}/"
		val libraryXml = read("library.xml") ?: throw IllegalArgumentException(Translations.getString("web.import.invalid"))
		val library = StoreXmlReader(DomXmlReader(libraryXml)).readStorable() as? Project
			?: throw IllegalArgumentException(Translations.getString("web.import.invalid"))
		library.uuid = uuid
		library.author = localUser

		if (ProjectModule.projectDictionaryService.contains(uuid)) {
			ProjectModule.projectDictionaryService.remove(uuid)
		}
		fileNamesOf(library).forEach { name ->
			read(name)?.let { store.write("$prefix$name", it) }
		}
		store.write("${prefix}library.xml", StorableCloner.serialize(library))
		ProjectModule.projectDictionaryService.add(library)
		return uuid.id
	}

	/** ---- Circuits */

	/** Returns all items of [project] (or library; circuits, truth tables, …) as JSON tree; [current] is the UUID of the open item. */
	fun items(project: Library?, current: String?): String {
		project ?: return "null"
		return toJson(itemNodes(project, project, current).toJsArray())
	}

	private fun itemNodes(project: Library, directory: LibraryDirectory, current: String?): List<dynamic> =
		directory.getItems().mapNotNull { item ->
			val kind = WebItemKinds.kindOf(item)
			when {
				item is LibraryDirectory -> obj {
					this.kind = "folder"
					this.name = plainName(item)
					this.children = itemNodes(project, item, current).toJsArray()
				}
				kind != null -> {
					val uuid = WebItemKinds.uuidOf(item)
					val key = if (item is LibraryElement) keyOf(project, item).also { elements[it] = item } else null
					val type = (item as? ContainerLibraryElement)?.graphType?.toString()
					val isDefault = item is ContainerLibraryElement && project.defaultElementUUID == item.uuid
					val name = plainName(item)
					obj {
						this.kind = kind
						this.key = key
						this.uuid = uuid
						this.name = name
						this.type = type
						this.isDefault = isDefault
						this.isOpen = uuid == current
					}
				}
				else -> null
			}
		}

	/** Finds the item of the open project with the specified UUID. */
	fun item(uuid: String): LibraryItem? = holder.l?.let { findItem(it, uuid) }

	private fun findItem(directory: LibraryDirectory, uuid: String): LibraryItem? {
		for (item in directory.getItems()) {
			if (WebItemKinds.uuidOf(item) == uuid) return item
			if (item is LibraryDirectory) findItem(item, uuid)?.let { return it }
		}
		return null
	}

	companion object {
		const val EXPORT_FORMAT = "antares-project"
		const val DRAG_TYPE_PREFIX = "application/x-antares-"

		/** HTML drag data types are lower-cased, so keys are hex-encoded into the type name. */
		fun decodeKey(encoded: String): String =
			encoded.chunked(2).map { it.toInt(16).toChar() }.joinToString("")
	}
}
