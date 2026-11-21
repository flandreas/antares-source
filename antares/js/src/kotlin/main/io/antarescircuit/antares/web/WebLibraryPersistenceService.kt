package io.antarescircuit.antares.web

import io.antarescircuit.jabbah.base.UUID
import io.antarescircuit.jabbah.draw.graphics.Image
import io.antarescircuit.jabbah.draw.graphics.ImageType
import io.antarescircuit.jabbah.draw.module.DrawModule
import io.antarescircuit.jabbah.edit.model.image.ImageIdentification
import io.antarescircuit.jabbah.graph.GraphQuota
import io.antarescircuit.jabbah.graph.MetaGraph
import io.antarescircuit.jabbah.graph.MetaGraphBundle
import io.antarescircuit.jabbah.graph.library.Library
import io.antarescircuit.jabbah.graph.library.LibraryIdentification
import io.antarescircuit.jabbah.graph.library.LibraryPersistenceService
import io.antarescircuit.jabbah.graph.library.dictionary.LibraryDictionary
import io.antarescircuit.jabbah.graph.library.dictionary.LibraryDictionaryPersistenceService
import io.antarescircuit.jabbah.io.DomXmlReader
import io.antarescircuit.jabbah.io.StorableCloner
import io.antarescircuit.jabbah.io.StoreXmlReader

/**
 * Synchronous key/value file storage provided by the web application.
 * The web app keeps all files in memory and persists writes asynchronously (e.g. to IndexedDB).
 * Keys look like `system/<libraryUuid>/library.xml` or `projects/<libraryUuid>/<metaGraphUuid>.cir`.
 */
@JsExport
class WebFileStore(
	val read: (path: String) -> String?,
	val write: (path: String, content: String) -> Unit,
	val remove: (path: String) -> Unit,
)

/**
 * Implements [LibraryPersistenceService] on top of a [WebFileStore],
 * using the same file layout as the desktop application (`library.xml` and `<uuid>.cir` files).
 */
class WebLibraryPersistenceService(
	private val store: WebFileStore,
	private val root: String,
) : LibraryPersistenceService {

	private fun libraryPath(uuid: UUID) = "$root/${uuid.id}/library.xml"

	private fun metaGraphPath(library: Library, uuid: UUID) = "$root/${library.uuid.id}/${uuid.id}.cir"

	override fun loadMetaGraphXML(library: Library, uuid: UUID): String =
		store.read(metaGraphPath(library, uuid))
			?: throw IllegalArgumentException("Circuit ${uuid.id} not found in library ${library.uuid.id}")

	override fun loadMetaGraph(library: Library, uuid: UUID): MetaGraph =
		StoreXmlReader(DomXmlReader(loadMetaGraphXML(library, uuid))).readStorable() as MetaGraph

	override fun storeMetaGraph(library: Library, metaGraph: MetaGraph) =
		store.write(metaGraphPath(library, metaGraph.uuid), StorableCloner.serialize(metaGraph))

	override fun deleteMetaGraph(library: Library, uuid: UUID) =
		store.remove(metaGraphPath(library, uuid))

	override fun loadLibrary(libraryId: LibraryIdentification): Library {
		val xml = store.read(libraryPath(libraryId.uuid))
			?: throw IllegalArgumentException("Library ${libraryId.uuid.id} not found")
		return StoreXmlReader(DomXmlReader(xml)).readStorable() as Library
	}

	override fun storeLibrary(library: Library) =
		store.write(libraryPath(library.uuid), StorableCloner.serialize(library))

	override fun deleteLibrary(libraryId: LibraryIdentification) =
		store.remove(libraryPath(libraryId.uuid))

	override suspend fun importLibrary(inputPath: String, currentLibraryCount: Int, quota: GraphQuota): Library =
		throw UnsupportedOperationException("Import libraries through the web editor's file menu")

	override fun importTemporaryLibrary(libraryId: LibraryIdentification, temporaryPath: String) =
		throw UnsupportedOperationException()

	override fun exportLibrary(libraryId: LibraryIdentification, outputPath: String) =
		throw UnsupportedOperationException()

	override fun exportLibraryTemporarily(libraryId: LibraryIdentification): String =
		throw UnsupportedOperationException()

	override fun exportMetaGraphBundle(bundle: MetaGraphBundle, outputPath: String) =
		throw UnsupportedOperationException()

	/** In the web editor, [inputPath] is the XML of the bundle file unpacked from the ZIP file (see `WebExport`). */
	override fun importMetaGraphBundle(inputPath: String): MetaGraphBundle =
		StoreXmlReader(DomXmlReader(inputPath)).readStorable() as MetaGraphBundle

	/** Images are stored as `data:` URLs, in files named like the desktop's image files (`<uuid>.<extension>`). */
	private fun imagePath(libraryUuid: UUID, imageUuid: UUID, imageType: ImageType) =
		"$root/${libraryUuid.id}/${imageFileName(imageUuid, imageType)}"

	/** Returns the `data:` URL of an image. */
	internal fun readImage(libraryUuid: UUID, imageUuid: UUID, imageType: ImageType): String =
		store.read(imagePath(libraryUuid, imageUuid, imageType))
			?: throw IllegalArgumentException("Image ${imageUuid.id} not found in library ${libraryUuid.id}")

	internal fun deleteImage(libraryUuid: UUID, imageUuid: UUID, imageType: ImageType) =
		store.remove(imagePath(libraryUuid, imageUuid, imageType))

	override fun loadImage(library: Library, imageUuid: UUID, imageType: ImageType): Image =
		DrawModule.imageLoader.loadUserImage(readImage(library.uuid, imageUuid, imageType), imageType)

	/** Stores an image; in the web editor, [inputPath] is the image's `data:` URL. */
	override fun importImage(library: Library, imageId: ImageIdentification, inputPath: String) {
		require(inputPath.startsWith("data:")) { "Images must be imported as data URL" }
		store.write(imagePath(library.uuid, imageId.uuid, imageId.imageType), inputPath)
	}

	companion object {
		fun imageFileName(imageUuid: UUID, imageType: ImageType) = "${imageUuid.id}.${imageType.fileExtension}"
	}
}

/** Stores a [LibraryDictionary] as `<root>/dictionary.xml` in a [WebFileStore]. */
class WebLibraryDictionaryPersistenceService(
	private val store: WebFileStore,
	root: String,
) : LibraryDictionaryPersistenceService {

	private val path = "$root/dictionary.xml"

	override val directoryExists: Boolean get() = store.read(path) != null

	override fun ensureLibraryDirectory() {}

	override fun load(): LibraryDictionary =
		store.read(path)?.let { StoreXmlReader(DomXmlReader(it)).readStorable() as LibraryDictionary }
			?: LibraryDictionary()

	override fun store(dictionary: LibraryDictionary) =
		store.write(path, StorableCloner.serialize(dictionary))
}

/** All persistence services of the web editor, backed by one [WebFileStore]. */
class WebPersistence(store: WebFileStore) {
	val systemLibraries = WebLibraryPersistenceService(store, SYSTEM)
	val systemDictionary = WebLibraryDictionaryPersistenceService(store, SYSTEM)
	val userLibraries = WebLibraryPersistenceService(store, USER)
	val userDictionary = WebLibraryDictionaryPersistenceService(store, USER)
	val projects = WebLibraryPersistenceService(store, PROJECTS)
	val projectDictionary = WebLibraryDictionaryPersistenceService(store, PROJECTS)

	companion object {
		const val SYSTEM = "system"
		const val USER = "user"
		const val PROJECTS = "projects"
	}
}
