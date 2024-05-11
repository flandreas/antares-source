package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.draw.graphics.Image
import ch.scorpion.jabbah.draw.graphics.ImageType
import ch.scorpion.jabbah.draw.module.DrawModule
import ch.scorpion.jabbah.edit.model.image.ImageIdentification
import ch.scorpion.jabbah.graph.GraphQuota
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.MetaGraphBundle

class Akrab2RestProjectPersistenceServiceJs(
    baseUrl: String
) : AbstractAkrab2RestLibraryPersistenceServiceJs(baseUrl, "project") {

    override fun loadLibrary(libraryId: LibraryIdentification): Library =
        loadLibrary(libraryId, "$baseUrl/project/${libraryId.uuid.id}")

    override fun getMetaGraphXMLUrl(library: Library, uuid: UUID): String =
        "$baseUrl/metaGraph/$uuid/xml"

    override fun loadImage(library: Library, imageUuid: UUID, imageType: ImageType): Image {
        return DrawModule.imageLoader.loadUserImage("$baseUrl/image/${imageUuid.id}", imageType)
    }

    override fun importImage(library: Library, imageId: ImageIdentification, inputPath: String) {
        TODO("Not yet implemented")
    }

    override fun storeMetaGraph(library: Library, metaGraph: MetaGraph) {
        TODO("Not yet implemented")
    }

    override fun deleteMetaGraph(library: Library, uuid: UUID) {
        TODO("Not yet implemented")
    }

    override fun storeLibrary(library: Library) {
        TODO("Not yet implemented")
    }

    override fun deleteLibrary(libraryId: LibraryIdentification) {
        TODO("Not yet implemented")
    }

    override suspend fun importLibrary(inputPath: String, currentLibraryCount: Int, quota: GraphQuota): Library {
        TODO("Not yet implemented")
    }

    override fun importTemporaryLibrary(libraryId: LibraryIdentification, temporaryPath: String) {
        TODO("Not yet implemented")
    }

    override fun exportLibrary(libraryId: LibraryIdentification, outputPath: String) {
        TODO("Not yet implemented")
    }

    override fun exportLibraryTemporarily(libraryId: LibraryIdentification): String {
        TODO("Not yet implemented")
    }

    override fun exportMetaGraphBundle(bundle: MetaGraphBundle, outputPath: String) {
        TODO("Not yet implemented")
    }

    override fun importMetaGraphBundle(inputPath: String): MetaGraphBundle {
        TODO("Not yet implemented")
    }
}