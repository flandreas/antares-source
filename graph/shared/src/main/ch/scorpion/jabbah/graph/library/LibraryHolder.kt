package ch.scorpion.jabbah.graph.library

/**
 * Holds the one and only [Library].
 */
class LibraryHolder(l: Library? = null) {

    var l: Library? = l
        set(value) {
            field?.dispose()
            field = value
        }

    val library: Library get() = l!!
}