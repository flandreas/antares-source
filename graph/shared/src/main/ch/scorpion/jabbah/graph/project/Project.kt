package ch.scorpion.jabbah.graph.project

import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.library.Library
import ch.scorpion.jabbah.graph.library.LibraryImpl
import ch.scorpion.jabbah.io.*

class Project : Storable {

    private var description: String = ""
    private var contents: Library = defaultContent()

    companion object {

        private fun defaultContent(): Library {
            val library = LibraryImpl("contents")
            library.addContainerElement(MetaGraph())
            return library
        }
    }

    /** ---- [Storable] */

    override var storableId: Int = 0

    override fun write(writer: StoreWriter) {
        writer.writeString("description", description)
        writer.writeStorable("contents", contents)
    }

    override fun read(reader: StoreReader) {
        description = reader.readString("description")
        contents = reader.readStorable("contents") as Library
    }

    override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) {
        // empty
    }

    override fun getStorableChildren(): Iterator<Storable> {
        return listOf(contents).iterator()
    }
}