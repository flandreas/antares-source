package io.antarescircuit.antares.web

import io.antarescircuit.antares.model.AntaresGraphTypes
import io.antarescircuit.antares.model.gate.CurrentDefaultPropagationDelay
import io.antarescircuit.antares.model.gate.UndefinedGateInputBehavior
import io.antarescircuit.antares.model.input.CurrentSwitchPropagationDelay
import io.antarescircuit.jabbah.base.Language
import io.antarescircuit.jabbah.base.System
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.UUID
import io.antarescircuit.jabbah.base.richtext.RichText
import io.antarescircuit.jabbah.edit.auth.EditAuthModule
import io.antarescircuit.jabbah.edit.model.text.TranslatableText
import io.antarescircuit.jabbah.graph.MetaGraph
import io.antarescircuit.jabbah.graph.app.ApplicationMode
import io.antarescircuit.jabbah.graph.library.*
import io.antarescircuit.jabbah.graph.library.dictionary.LibraryDictionaryEntry
import io.antarescircuit.jabbah.graph.model.port.InconsistentNetError
import io.antarescircuit.jabbah.graph.project.Project
import io.antarescircuit.jabbah.graph.project.ProjectModule
import io.antarescircuit.jabbah.io.DomXmlReader
import io.antarescircuit.jabbah.io.StorableCloner
import io.antarescircuit.jabbah.io.StoreXmlReader

/**
 * Project structure and user libraries, like the desktop's library tree (`LibraryTreeViewActionsSwing`:
 * folders, moving items by drag and drop, project/library properties) and its library dialogs
 * (`LibraryPersistencePanel`, `CreateLibraryPanel`, `LibrarySelectionPanel`, `RemoveLibraryAction`).
 *
 * Tree operations work on the open project or user library ([LibraryHolder.l]). Tree nodes are addressed by
 * their path, the dot-separated indexes from the library root (e.g. `"2.0"`; `""` is the root itself).
 */
@JsExport
class WebLibraries internal constructor(private val ctx: WebEditorContext) {

	private val holder get() = LibraryModule.libraryHolder
	private val management get() = LibraryModule.libraryManagementService
	private val userDictionary get() = LibraryModule.userLibraryDictionaryService
	private val systemDictionary get() = LibraryModule.systemLibraryDictionaryService
	private val user get() = EditAuthModule.userHolder.user.identity
	private val treeController get() = ctx.frame().graphPanelViewController.libraryPanelController.libraryTreeViewController

	private fun plain(text: String?): String = RichText.stripToPlainText(text ?: "")

	private fun current(): Library = holder.l ?: throw IllegalStateException("No project open")

	private fun checkEditing() {
		check(!ctx.frame().applicationModeHolder.currentMode.isExecute()) { "Stop the simulation first" }
	}

	/** ---- Tree of the open project or library */

	/**
	 * Returns the open project or library as JSON tree: kind (project|library), uuid, name and children.
	 * Folders have kind, path, name and children; items have kind, path, uuid, key (circuits), name, type,
	 * isDefault and isOpen.
	 */
	fun tree(): String = ctx.guarded("null") {
		val library = holder.l ?: return@guarded "null"
		val kind = if (library is Project) "project" else "library"
		val name = plain(library.name.value)
		val children = nodes(library, library, "", openItemUuid)
		toJson(obj {
			this.kind = kind
			this.uuid = library.uuid.id
			this.name = name
			this.children = children
		})
	}

	private val openItemUuid: String?
		get() = (ctx.dataController().data?.savable as? AbstractLibraryItemSavable)?.item?.let { WebItemKinds.uuidOf(it) }

	private fun nodes(library: Library, directory: LibraryDirectory, prefix: String, current: String?): Array<dynamic> =
		directory.getItems().mapIndexedNotNull { index, item ->
			val path = if (prefix.isEmpty()) "$index" else "$prefix.$index"
			val name = plain(item.name.value)
			val kind = WebItemKinds.kindOf(item)
			when {
				item is LibraryDirectory -> {
					val children = nodes(library, item, path, current)
					obj {
						this.kind = "folder"
						this.path = path
						this.name = name
						this.children = children
					}
				}
				kind != null -> {
					val uuid = WebItemKinds.uuidOf(item)
					val key = (item as? LibraryElement)?.let { ctx.library().keyFor(library, it) }
					val type = (item as? ContainerLibraryElement)?.graphType?.toString()
					val isDefault = item is ContainerLibraryElement && library.defaultElementUUID == item.uuid
					obj {
						this.kind = kind
						this.path = path
						this.uuid = uuid
						this.key = key
						this.name = name
						this.type = type
						this.isDefault = isDefault
						this.isOpen = uuid == current
					}
				}
				else -> null
			}
		}.toTypedArray()

	private fun itemAt(path: String): LibraryItem {
		val library = current()
		if (path.isEmpty()) return library
		return path.split('.').fold<String, LibraryItem>(library) { item, index ->
			(item as LibraryDirectory).getItems()[index.toInt()]
		}
	}

	private fun directoryAt(path: String): LibraryDirectory =
		itemAt(path) as? LibraryDirectory ?: throw IllegalArgumentException("Not a folder: $path")

	private fun pathOf(directory: LibraryDirectory, item: LibraryItem, prefix: String = ""): String? {
		directory.getItems().forEachIndexed { index, child ->
			val path = if (prefix.isEmpty()) "$index" else "$prefix.$index"
			if (child === item) return path
			if (child is LibraryDirectory) pathOf(child, item, path)?.let { return it }
		}
		return null
	}

	/** Adds a folder named [name] to folder [parent] and returns the new folder's path. */
	fun addFolder(parent: String, name: String): String = ctx.guarded("") {
		checkEditing()
		require(name.isNotBlank()) { Translations.getString("edit.property.name.empty.error") }
		val library = current()
		val folder = library.libraryService.addFolder(library, TranslatableText(name.trim()), directoryAt(parent))
		ctx.notifyChanged()
		pathOf(library, folder) ?: ""
	}

	/** Describes folder [path] as JSON: name (translations by language code) and counts of its contents. */
	fun folder(path: String): String = ctx.guarded("null") {
		val folder = directoryAt(path)
		val (circuits, items, folders) = countContents(folder)
		val name = translations(folder.name.translation)
		toJson(obj {
			this.name = name
			this.circuits = circuits
			this.items = items
			this.folders = folders
		})
	}

	/** Counts the circuits, other items and folders below [directory]. */
	private fun countContents(directory: LibraryDirectory): Triple<Int, Int, Int> {
		var circuits = 0
		var items = 0
		var folders = 0
		fun count(d: LibraryDirectory) {
			d.getItems().forEach {
				when (it) {
					is LibraryDirectory -> { folders++; count(it) }
					is ContainerLibraryElement -> circuits++
					else -> items++
				}
			}
		}
		count(directory)
		return Triple(circuits, items, folders)
	}

	/** Renames folder [path]; [name] is a JSON object of translations by language code, e.g. `{"en":"Adders"}`. */
	fun renameFolder(path: String, name: String) = ctx.guarded(Unit) {
		checkEditing()
		val folder = directoryAt(path)
		require(folder !is Library)
		folder.library!!.libraryService.renameDirectory(folder, translatable(JSON.parse(name)))
		ctx.notifyChanged()
	}

	/** Deletes the empty folder [path], like the desktop's `DeleteLibraryFolderAction`. */
	fun deleteFolder(path: String) = ctx.guarded(Unit) {
		checkEditing()
		val folder = directoryAt(path)
		require(folder !is Library)
		require(folder.isEmpty()) { Translations.getString("graph.action.deleteLibraryDirectory.nonEmpty.msg") }
		val library = current()
		library.libraryService.removeLibraryItem(library, folder)
		ctx.notifyChanged()
	}

	/** Whether item or folder [source] may be moved into folder [target] (desktop: `LibraryTreeViewController.allowMove`). */
	fun canMove(source: String, target: String): Boolean = ctx.guarded(false) {
		if (source.isEmpty() || ctx.frame().applicationModeHolder.currentMode.isExecute()) return@guarded false
		val item = itemAt(source)
		val destination = directoryAt(target)
		// A folder can't be moved into itself or one of its sub-folders
		if (item is LibraryDirectory && (item === destination || item.containsRecursively(destination))) return@guarded false
		treeController.allowMove(item, destination)
	}

	/**
	 * Moves item or folder [source] into folder [target] at [index] (counted with the item still at its old
	 * position; -1 appends), like dropping it in the desktop's library tree.
	 */
	fun move(source: String, target: String, index: Int) = ctx.guarded(Unit) {
		if (!canMove(source, target)) return@guarded
		moveImpl(itemAt(source), directoryAt(target), index)
	}

	/** Moves the item with [uuid] (e.g. one that was just created) to the end of folder [target]. */
	fun moveItem(uuid: String, target: String) = ctx.guarded(Unit) {
		val item = ctx.library().item(uuid) ?: return@guarded
		val destination = directoryAt(target)
		if (destination.contains(item) || !treeController.allowMove(item, destination)) return@guarded
		moveImpl(item, destination, -1)
	}

	private fun moveImpl(item: LibraryItem, destination: LibraryDirectory, index: Int) {
		val library = current()
		val origin = library.libraryService.getDirectoryOf(library, item)
		var newIndex = if (index < 0) destination.size else index
		if (origin === destination) {
			val oldIndex = origin.indexOf(item)
			if (oldIndex < newIndex) newIndex--
			if (oldIndex == newIndex) return
		}
		library.libraryService.move(library, item, destination, newIndex)
		ctx.notifyChanged()
	}

	/** Creates a circuit in folder [folder] and opens it. [type] is "digital" or "analog". Returns its UUID. */
	fun createCircuit(name: String, type: String, folder: String): String = ctx.guarded("") {
		checkEditing()
		val library = current()
		val directory = directoryAt(folder)
		val graphType = if (type == "analog") AntaresGraphTypes.Analog else AntaresGraphTypes.Digital
		MetaGraph.validateName(TranslatableText(name))
		ctx.saveNow()
		val element = library.libraryService.addContainerLibraryElement(library, MetaGraph.create(TranslatableText(name), graphType), directory)
		ctx.openItem(element.uuid.id)
		element.uuid.id
	}

	/** ---- Properties of the open project or library */

	/**
	 * Describes the open project or library as JSON, like the desktop's `LibraryPropertiesPanel`: kind, name and
	 * description (translations by language code), visibility with the possible visibilities, the library
	 * preferences (settings stored with the project, which override the user's preferences) and statistics.
	 */
	fun properties(): String = ctx.guarded("null") {
		val library = holder.l ?: return@guarded "null"
		val kind = if (library is Project) "project" else "library"
		val name = translations(library.name.translation)
		val description = translations(library.description.translation)
		val visibility = library.visibility.customName
		val visibilities = LibraryVisibility.entries.map { v ->
			val id = v.customName
			val label = v.toString()
			val text = v.description()
			obj {
				this.id = id
				this.label = label
				this.description = text
			}
		}.toJsArray()
		val preferences = PREFERENCES.map { p -> p.describe(library.preferences) }.toJsArray()
		val (circuits, items, folders) = countContents(library)
		val imports = library.expandedImports.libraries.filter { it.uuid != library.uuid }.map { plain(it.name.value) }.toJsArray()
		toJson(obj {
			this.kind = kind
			this.uuid = library.uuid.id
			this.name = name
			this.description = description
			this.visibility = visibility
			this.visibilities = visibilities
			this.preferences = preferences
			this.circuits = circuits
			this.items = items
			this.folders = folders
			this.imports = imports
		})
	}

	/**
	 * Updates the open project or library from JSON: name and description (translations by language code),
	 * visibility, and preferences (by key).
	 */
	fun setProperties(json: String) = ctx.guarded(Unit) {
		checkEditing()
		val library = current()
		val data: dynamic = JSON.parse(json)
		val name = translatable(data.name)
		require(!name.isEmpty) { Translations.getString("library.emptyName.msg") }
		val description = translatable(data.description)
		val visibility = LibraryVisibility.withName(data.visibility as String)
		val properties = LibraryProperties(name, description, visibility, library.author)
		ctx.saveNow()
		if (library is Project) {
			val service = ProjectModule.projectManagementService
			require(!service.existsName(name, except = library.uuid)) { Translations.getString("library.duplicate.msg", plain(name.getTranslation())) }
			service.update(properties)
		} else {
			require(!management.existsName(name, except = library.uuid)) { Translations.getString("library.duplicate.msg", plain(name.getTranslation())) }
			management.update(library, properties)
		}
		val values: dynamic = data.preferences
		if (values != null) {
			PREFERENCES.forEach { p ->
				val value: dynamic = values[p.key]
				if (value != null && value != undefined) p.set(library.preferences, value)
			}
			library.libraryService.storeLibrary(library)
		}
		ctx.notifyChanged()
	}

	private fun translations(text: TranslatableText): dynamic {
		val result = obj {}
		Language.entries.forEach { language ->
			result[language.code] = if (text.hasTranslation(language)) plain(text.getTranslation(language)) else ""
		}
		return result
	}

	private fun translatable(value: dynamic): TranslatableText {
		var text = TranslatableText()
		if (value == null) return text
		Language.entries.forEach { language ->
			val raw: Any? = value[language.code]
			val translation = (raw as? String)?.trim()
			if (!translation.isNullOrEmpty()) text = text.withTranslation(language, translation)
		}
		return text
	}

	/** ---- User libraries */

	/** Lists the user libraries as JSON: uuid, name, description and whether it is open. */
	fun list(): String = ctx.guarded("[]") {
		val open = holder.l?.uuid
		toJson(userDictionary.getEntries().map { entry ->
			val name = plain(entry.name.value)
			val description = plain(entry.description.value)
			val isOpen = entry.uuid == open
			obj {
				this.uuid = entry.uuid.id
				this.name = name
				this.description = description
				this.isOpen = isOpen
			}
		}.sortedBy { (it.name as String).lowercase() }.toJsArray())
	}

	/**
	 * Creates a user library (desktop: `CreateLibraryPanel`) importing library [importUuid] (or none if empty),
	 * and returns its UUID.
	 */
	fun create(name: String, description: String, importUuid: String): String = ctx.guarded("") {
		val text = TranslatableText(name.trim())
		require(!text.isEmpty && name.isNotBlank()) { Translations.getString("library.emptyName.msg") }
		require(!management.existsName(text)) { Translations.getString("library.duplicate.msg", name.trim()) }
		val properties = LibraryProperties(text, if (description.isBlank()) TranslatableText() else TranslatableText(description.trim()))
		val import = importUuid.takeIf { it.isNotEmpty() }?.let { LibraryIdentification(UUID(it), user) }
		val library = management.create(properties, import)
		ctx.notifyChanged()
		library.uuid.id
	}

	/** Opens user library [uuid] for editing in place of the open project, with its default or first circuit. */
	fun open(uuid: String) = ctx.guarded(Unit) {
		ctx.saveNow()
		val modeHolder = ctx.frame().applicationModeHolder
		if (modeHolder.currentMode.isExecute()) modeHolder.setMode(ApplicationMode.EDIT) {}
		ctx.editor.commandManager.reset()
		val id = LibraryIdentification(UUID(uuid), user)
		val library = management.loadLibrary(id)
		val first = library.defaultElementUUID ?: library.firstLocalItemOrNull { it is ContainerLibraryElement }?.let { (it as ContainerLibraryElement).uuid }
		ctx.dataController().openLibrary(id, first)
		ctx.notifyChanged()
	}

	/** Closes the open project or library. */
	fun close() = ctx.guarded(Unit) {
		ctx.saveNow()
		val modeHolder = ctx.frame().applicationModeHolder
		if (modeHolder.currentMode.isExecute()) modeHolder.setMode(ApplicationMode.EDIT) {}
		ctx.editor.commandManager.reset()
		ctx.dataController().closeData()
		holder.l = null
		ctx.notifyChanged()
	}

	/** Renames user library [uuid] (in the current language). */
	fun rename(uuid: String, name: String) = ctx.guarded(Unit) {
		require(name.isNotBlank()) { Translations.getString("library.emptyName.msg") }
		val id = UUID(uuid)
		val library = holder.l?.takeIf { it.uuid == id } ?: management.loadLibrary(LibraryIdentification(id, user))
		val newName = library.name.translation.withTranslation(System.currentLanguage(), name.trim())
		require(!management.existsName(newName, except = id)) { Translations.getString("library.duplicate.msg", name.trim()) }
		management.update(library, LibraryProperties.ofLibrary(library).copy(name = newName))
		ctx.notifyChanged()
	}

	/** Lists the names of projects and user libraries that import library [uuid], as JSON array. */
	fun usedBy(uuid: String): String = ctx.guarded("[]") {
		val id = UUID(uuid)
		fun imports(load: () -> Library): Boolean = try {
			load().importedLibraryIds.contains(id)
		} catch (_: Throwable) {
			false
		}
		val projects = ProjectModule.projectManagementService.getProjectDirectoryEntries()
			.filter { e -> imports { ProjectModule.projectManagementService.load(LibraryIdentification(e.uuid, user)) } }
		val libraries = userDictionary.getEntries()
			.filter { e -> e.uuid != id && imports { management.loadLibrary(LibraryIdentification(e.uuid, user)) } }
		toJson((projects + libraries).map { plain(it.name.value) }.toJsArray())
	}

	/** Deletes user library [uuid] with all its files. The open library can't be deleted. */
	fun delete(uuid: String) = ctx.guarded(Unit) {
		val id = UUID(uuid)
		require(holder.l?.uuid != id) { "The open library can't be deleted" }
		val files = ctx.library().filesOf(prefixOf(uuid))
		management.delete(LibraryIdentification(id, user))
		files.forEach { ctx.library().store.remove(it) }
		ctx.notifyChanged()
	}

	private fun prefixOf(uuid: String) = "${WebPersistence.USER}/$uuid/"

	/** Exports user library [uuid] with all its files as JSON (format `antares-library`), see [importLibrary]. */
	fun exportLibrary(uuid: String): String = ctx.guarded("") {
		if (holder.l?.uuid?.id == uuid) ctx.saveNow()
		val entry = userDictionary.getEntry(UUID(uuid)) ?: throw IllegalArgumentException("Unknown library $uuid")
		val prefix = prefixOf(uuid)
		val web = ctx.library()
		val files = obj {}
		web.filesOf(prefix).forEach { path -> files[path.removePrefix(prefix)] = web.store.read(path) }
		val name = plain(entry.name.value)
		toJson(obj {
			format = EXPORT_FORMAT
			version = 1
			this.uuid = uuid
			this.name = name
			this.files = files
		})
	}

	/**
	 * Imports a user library exported by [exportLibrary] (or read from a desktop `.acl` file), like the desktop's
	 * `LibraryImportProcess`. Returns JSON with status (`ok`, `exists`, `nameExists`, `staleReference`, `open`
	 * or `invalid`), uuid and name. An existing library with the same UUID is only replaced if [replace] is set.
	 */
	fun importLibrary(json: String, replace: Boolean): String = ctx.guarded("""{"status":"invalid"}""") {
		val data: dynamic = JSON.parse(json)
		fun result(status: String, uuid: String = "", name: String = "") = toJson(obj {
			this.status = status
			this.uuid = uuid
			this.name = name
		})
		if (data.format != EXPORT_FORMAT) return@guarded result("invalid")
		val files: dynamic = data.files
		val names = js("Object.keys")(files).unsafeCast<Array<String>>()
		val read = { name: String -> if (names.contains(name)) files[name] as String else null }
		val library = read("library.xml")?.let {
			try {
				StoreXmlReader(DomXmlReader(it)).readStorable() as? Library
			} catch (_: Throwable) {
				null
			}
		}
		if (library == null || library is Project || library.isSystem || systemDictionary.contains(library.uuid)) {
			return@guarded result("invalid")
		}
		val uuid = library.uuid
		val name = plain(library.name.value)
		val exists = userDictionary.contains(uuid)
		when {
			exists && holder.l?.uuid == uuid -> return@guarded result("open", uuid.id, name)
			exists && !replace -> return@guarded result("exists", uuid.id, name)
			management.existsName(library.name.translation, except = uuid) -> return@guarded result("nameExists", uuid.id, name)
			library.importedLibraryIds.any { !userDictionary.contains(it) && !systemDictionary.contains(it) } ->
				return@guarded result("staleReference", uuid.id, name)
		}
		if (exists) delete(uuid.id)
		library.author = user
		val web = ctx.library()
		val prefix = prefixOf(uuid.id)
		web.fileNamesOf(library).forEach { file -> read(file)?.let { web.store.write("$prefix$file", it) } }
		web.store.write("${prefix}library.xml", StorableCloner.serialize(library))
		userDictionary.add(library)
		ctx.notifyChanged()
		result("ok", uuid.id, name)
	}

	/** ---- Libraries imported by the open project or library */

	/**
	 * Lists all system and user libraries as JSON with whether the open project or library imports them:
	 * uuid, name, description, system, imported (directly), indirect (through another library) and
	 * cyclic (a library that imports the open library and can't be imported by it), and isDefault (the standard
	 * library that new projects and libraries import).
	 */
	fun imports(): String = ctx.guarded("[]") {
		val open = holder.l
		val direct = open?.importedLibraryIds ?: emptySet()
		val expanded = open?.expandedImports
		fun entry(e: LibraryDictionaryEntry, system: Boolean): dynamic {
			val imported = direct.contains(e.uuid)
			val indirect = !imported && expanded?.contains(e.uuid) == true
			val isDefault = e.uuid == LibraryModule.DEF_LIBRARY_UUID
			val cyclic = !system && open != null && open !is Project && try {
				management.loadLibrary(LibraryIdentification(e.uuid, user)).expandedImports.contains(open.uuid)
			} catch (_: Throwable) {
				false
			}
			val name = plain(e.name.value)
			val description = plain(e.description.value)
			return obj {
				this.uuid = e.uuid.id
				this.name = name
				this.description = description
				this.system = system
				this.imported = imported
				this.indirect = indirect
				this.cyclic = cyclic
				this.isDefault = isDefault
			}
		}
		val system = systemDictionary.getEntries().map { entry(it, true) }
		val users = userDictionary.getEntries().filter { it.uuid != open?.uuid }.map { entry(it, false) }
			.sortedBy { (it.name as String).lowercase() }
		toJson((system + users).toJsArray())
	}

	/** Makes the components of library [uuid] available in the open project or library. */
	fun addImport(uuid: String) = ctx.guarded(Unit) {
		checkEditing()
		val open = current()
		val id = UUID(uuid)
		require(id != open.uuid && !open.importedLibraryIds.contains(id))
		open.libraryService.addImport(open, id)
		ctx.notifyChanged()
	}

	/**
	 * Removes library [uuid] from the imports of the open project or library, unless its circuits use components
	 * of it (desktop: `RemoveLibraryAction`).
	 */
	fun removeImport(uuid: String) = ctx.guarded(Unit) {
		checkEditing()
		val open = current()
		val id = UUID(uuid)
		val target = open.expandedImports.libraries.firstOrNull { it.uuid == id } ?: return@guarded
		ctx.saveNow()
		val evaluation = open.libraryService.evaluateLibraryReferences(open, target)
		if (evaluation.hasNonSystemReferences) {
			val key = if (open is Project) "library.removeImport.stateReferenceFromProject.txt" else "library.removeImport.staleReferenceFromLibrary.txt"
			throw IllegalArgumentException(Translations.getString(key).replace("\n", " "))
		}
		val service: AbstractLibraryManagementService = if (open is Project) ProjectModule.projectManagementService else management
		service.removeImport(id, evaluation.systemReferences)
		ctx.notifyChanged()
	}

	/** A preference stored in [LibraryPreferences] (desktop: `libraryPreferencesProvider`). */
	private class Preference(
		val key: String,
		val labelKey: String,
		val min: Int? = null,
		val max: Int? = null,
		val choices: List<Pair<String, String>>? = null,
	) {
		fun describe(preferences: LibraryPreferences): dynamic {
			val label = Translations.getString(labelKey)
			val type = if (choices != null) "choice" else "integer"
			val value: Any? = try {
				if (choices != null) preferences.getString(key) else preferences.getInt(key)
			} catch (_: Throwable) {
				null
			}
			val choiceList = choices?.map { (id, text) -> obj { this.id = id; this.label = text } }?.toJsArray()
			val k = key
			return obj {
				this.key = k
				this.label = label
				this.type = type
				this.value = value
				this.min = min
				this.max = max
				this.choices = choiceList
			}
		}

		fun set(preferences: LibraryPreferences, value: dynamic) {
			if (choices != null) {
				val id = value as String
				require(choices.any { it.first == id })
				preferences.set(key, id)
			} else {
				val number = (value as Number).toInt()
				require((min == null || number >= min) && (max == null || number <= max)) { "$number is out of range" }
				preferences.set(key, number)
			}
		}
	}

	companion object {
		const val EXPORT_FORMAT = "antares-library"

		private val PREFERENCES by lazy {
			listOf(
				Preference(CurrentDefaultPropagationDelay.PROP_DEFAULT_PROPAGATION_DELAY, "antares.preference.defaultPropagationDelay", 1, 1_000_000),
				Preference(
					UndefinedGateInputBehavior.PROP_UNDEFINED_GATE_INPUT_BEHAVIOR,
					"antares.preference.undefinedGateInputBehavior.name",
					choices = UndefinedGateInputBehavior.entries.map { it.customName to it.toString() },
				),
				Preference(CurrentSwitchPropagationDelay.PROP_DEFAULT_DELAY, "antares.preference.SwitchPropDelay", 0, 1_000_000),
				Preference(InconsistentNetError.PROP_ALLOWED_DURATION, "graph.preferences.InconsistentNetError.allowedDuration", 0, 1_000_000),
			)
		}
	}
}
