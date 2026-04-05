package io.antarescircuit.jabbah.graph.library

import io.antarescircuit.jabbah.base.UUID
import io.antarescircuit.jabbah.draw.graphics.Image
import io.antarescircuit.jabbah.draw.graphics.ImageType
import io.antarescircuit.jabbah.edit.model.image.ImageIdentification
import io.antarescircuit.jabbah.graph.GraphQuota
import io.antarescircuit.jabbah.graph.MetaGraph
import io.antarescircuit.jabbah.graph.MetaGraphBundle
import io.antarescircuit.jabbah.io.StorableCloner

/**
 * Stores [Libraries][Library] in-memory for testing purposes.
 * */
class MemoryLibraryPersistenceService(
	private val libraryAccessor: () -> Library? = { LibraryModule.libraryHolder.library }
) : LibraryPersistenceService {

	/** Maps the [UUID] of a [MetaGraph] to the [MetaGraph]. */
	private val map = mutableMapOf<UUID, String>()

	override fun loadMetaGraphXML(library: Library, uuid: UUID): String {
		return map[uuid]!!
	}

	override fun loadMetaGraph(library: Library, uuid: UUID): MetaGraph {
		return StorableCloner.deserialize(map[uuid]!!) as MetaGraph
	}

	override fun storeMetaGraph(library: Library, metaGraph: MetaGraph) {
		map[metaGraph.uuid] = StorableCloner.serialize(metaGraph)
	}

	override fun deleteMetaGraph(library: Library, uuid: UUID) {
		map.remove(uuid)
	}

	override fun loadLibrary(libraryId: LibraryIdentification): Library {
		return libraryAccessor.invoke()!!
	}

	override fun storeLibrary(library: Library) {
		// empty
	}

	override fun deleteLibrary(libraryId: LibraryIdentification) {
		// empty
	}

	override suspend fun importLibrary(inputPath: String, currentLibraryCount: Int, quota: GraphQuota): Library {
		throw UnsupportedOperationException("not implemented")
	}

	override fun importTemporaryLibrary(libraryId: LibraryIdentification, temporaryPath: String) {
		throw UnsupportedOperationException("not implemented")
	}

	override fun exportLibrary(libraryId: LibraryIdentification, outputPath: String) {
		throw UnsupportedOperationException("not implemented")
	}

	override fun exportLibraryTemporarily(libraryId: LibraryIdentification): String {
		throw UnsupportedOperationException("not implemented")
	}

	override fun exportMetaGraphBundle(bundle: MetaGraphBundle, outputPath: String) {
		throw UnsupportedOperationException("not implemented")
	}

	override fun importMetaGraphBundle(inputPath: String): MetaGraphBundle {
		throw UnsupportedOperationException("not implemented")
	}

	override fun loadImage(library: Library, imageUuid: UUID, imageType: ImageType): Image {
		throw UnsupportedOperationException("not implemented")
	}

	override fun importImage(library: Library, imageId: ImageIdentification, inputPath: String) {
		throw UnsupportedOperationException("not implemented")
	}
}