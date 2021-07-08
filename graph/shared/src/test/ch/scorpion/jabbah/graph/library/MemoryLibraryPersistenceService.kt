package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.UUID
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

	override fun loadMetaGraph(library: Library, uuid: UUID): MetaGraph {
		return StorableCloner.clone(map[uuid]!!)
	}

	override fun storeMetaGraph(library: Library, metaGraph: MetaGraph) {
		map[metaGraph.uuid] = StorableCloner.clone(metaGraph)
	}

	override fun deleteMetaGraph(library: Library, uuid: UUID) {
		map.remove(uuid)
	}

	override fun loadLibrary(uuid: UUID): Library {
		return libraryAccessor.invoke()!!
	}

	override fun storeLibrary(library: Library) {
		// empty
	}

	override fun deleteLibrary(uuid: UUID) {
		// empty
	}

	override fun importLibrary(inputPath: String): UUID {
		throw UnsupportedOperationException("not implemented")
	}

	override fun importTemporaryLibrary(uuid: UUID, temporaryPath: String) {
		throw UnsupportedOperationException("not implemented")
	}

	override fun exportLibrary(uuid: UUID, outputPath: String) {
		throw UnsupportedOperationException("not implemented")
	}

	override fun exportLibraryTemporarily(uuid: UUID): String {
		throw UnsupportedOperationException("not implemented")
	}

	override fun exportMetaGraphBundle(bundle: MetaGraphBundle, outputPath: String) {
		throw UnsupportedOperationException("not implemented")
	}

	override fun importMetaGraphBundle(inputPath: String): MetaGraphBundle {
		throw UnsupportedOperationException("not implemented")
	}
}