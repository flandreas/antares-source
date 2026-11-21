package io.antarescircuit.antares.web

import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.UUID
import io.antarescircuit.jabbah.base.richtext.RichText
import io.antarescircuit.jabbah.draw.graphics.EmbeddedImageJs
import io.antarescircuit.jabbah.draw.graphics.ImageType
import io.antarescircuit.jabbah.edit.model.image.ImageIdentification
import io.antarescircuit.jabbah.edit.module.EditModule
import io.antarescircuit.jabbah.graph.library.Library
import io.antarescircuit.jabbah.graph.library.LibraryModule
import io.antarescircuit.jabbah.graph.model.image.ImageLibraryElement
import io.antarescircuit.jabbah.graph.project.Project
import io.antarescircuit.jabbah.graph.project.ProjectModule

/**
 * Images of a project (desktop: `ImportImageAction`, `ImageGraphDesktopItemSwing`).
 * The web editor stores images as `data:` URLs in its file store, see [WebLibraryPersistenceService.importImage].
 * Images are placed into circuits as image components by dragging their project item onto the canvas.
 */
@JsExport
class WebImages internal constructor(private val ctx: WebEditorContext) {

	/**
	 * Imports an image given as `data:` URL (PNG, JPEG, GIF or SVG) into the open project.
	 * Returns the new image's UUID, or "" on failure.
	 */
	fun importImage(name: String, dataUrl: String): String = ctx.guarded("") {
		val project = ctx.project ?: return@guarded ""
		require(name.isNotBlank()) { Translations.getString("edit.property.name.empty.error") }
		val type = typeOf(dataUrl)
		ctx.saveNow()
		val element = project.libraryService.importImage(dataUrl, type, name.trim(), project)
		ctx.notifyChanged()
		element.storable.uuid.id
	}

	/** Describes image [uuid] of the open project as JSON: uuid, name, type (file extension) and url (a `data:` URL). */
	fun describe(uuid: String): String = ctx.guarded("null") {
		val element = element(uuid) ?: return@guarded "null"
		val lib = element.library ?: return@guarded "null"
		val id = element.storable
		val name = RichText.stripToPlainText(id.name.value)
		val extension = id.imageType.fileExtension
		val url = persistenceOf(lib).readImage(lib.uuid, id.uuid, id.imageType)
		toJson(obj {
			this.uuid = uuid
			this.name = name
			this.type = extension
			this.url = url
		})
	}

	/** Replaces the picture of image [uuid] with [dataUrl], keeping its UUID, so circuits using it show the new picture. */
	fun replaceImage(uuid: String, dataUrl: String) = ctx.guarded(Unit) {
		val element = element(uuid) ?: return@guarded
		val lib = element.library ?: return@guarded
		val old = element.storable
		val new = ImageIdentification(old.uuid, typeOf(dataUrl), old.name)
		val persistence = persistenceOf(lib)
		persistence.importImage(lib, new, dataUrl)
		if (new.imageType != old.imageType) persistence.deleteImage(lib.uuid, old.uuid, old.imageType)
		element.updateStorable(new)
		// The loaded image is cached by the library (and used by components), so it is updated in place
		(element.image as? EmbeddedImageJs)?.load(dataUrl)
		lib.libraryService.updateLibraryItem(lib, element)
		ctx.editor.view.repaint()
		ctx.notifyChanged()
	}

	private fun element(uuid: String): ImageLibraryElement? = ctx.library().item(uuid) as? ImageLibraryElement

	private fun persistenceOf(library: Library): WebLibraryPersistenceService =
		(if (library is Project) ProjectModule.projectLibraryPersistenceService else LibraryModule.userLibraryPersistenceService)
			as WebLibraryPersistenceService

	private fun typeOf(dataUrl: String): ImageType {
		require(dataUrl.startsWith("data:")) { "Not an image" }
		val mime = dataUrl.substring(5).substringBefore(',').substringBefore(';')
		return ImageType.withMimeType(mime)
			?: throw IllegalArgumentException(Translations.getString("library.action.importImage.typeNotSupported.msg", mime))
	}
}

/**
 * An image chosen among the images of the open project, e.g. the on/off images of an image switch
 * (desktop: `ImageIdentificationPropertyJvm`).
 */
internal fun <B : Any> WebPropertyBuilder<B>.image(key: String, baseKey: String, get: (B) -> UUID?, set: (B, UUID?) -> Unit) {
	val ids = EditModule.imageRepository.getAllImageIds()
	val labels = ids.associate { it.uuid to RichText.stripToPlainText(it.name.value) }
	choice(
		key, baseKey, ids.map { it.uuid }, get, set,
		id = { it.id }, label = { labels[it] ?: it.id }, nullLabel = "None", group = WebGroups.APPEARANCE,
	)
}
