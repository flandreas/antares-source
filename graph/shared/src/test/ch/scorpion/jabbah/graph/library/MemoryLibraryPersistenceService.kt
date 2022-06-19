package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.graph.GraphQuota
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.MetaGraphBundle
import ch.scorpion.jabbah.io.StorableCloner

/**
 * Stores [Libraries][Library] in-memory for testing purposes.
 * */
class MemoryLibraryPersistenceService(
	private val libraryAccessor: () -> Library? = { LibraryModule.libraryHolder.library }
) : LibraryPersistenceService {

	/** Maps the [UUID] of a [MetaGraph] to the [MetaGraph]. */
	private val map = mutableMapOf<UUID, MetaGraph>()

	override fun loadMetaGraphXML(library: Library, uuid: UUID): String {
		throw UnsupportedOperationException("not implemented")
	}

	override fun loadMetaGraph(library: Library, uuid: UUID): MetaGraph {
		return StorableCloner.clone(map[uuid]!!)
	}

	override fun storeMetaGraph(library: Library, metaGraph: MetaGraph) {
		map[metaGraph.uuid] = StorableCloner.clone(metaGraph)
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

	override fun importLibrary(inputPath: String, currentLibraryCount: Int, quota: GraphQuota): Library {
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
}