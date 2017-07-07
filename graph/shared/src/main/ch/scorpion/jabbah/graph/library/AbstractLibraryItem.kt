package ch.scorpion.jabbah.graph.library

/**
 * Abstract base implementation of the [LibraryItem] interface
 */
abstract class AbstractLibraryItem(
    override val iconPath: String?
) : LibraryItem {

    @Suppress("unused") constructor(): this(null)

    protected var library: Library? = null

    override fun bindTo(library: Library) {
        this.library = library
    }

    override fun dispose() {
        library = null
    }
}