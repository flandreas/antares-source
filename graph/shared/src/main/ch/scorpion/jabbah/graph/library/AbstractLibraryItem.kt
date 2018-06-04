package ch.scorpion.jabbah.graph.library

/**
 * Abstract base implementation of the [LibraryItem] interface
 */
abstract class AbstractLibraryItem(
    override val iconPath: String?
) : LibraryItem {

    @Suppress("unused") constructor(): this(null)

    private var _library: Library? = null

    override val library: Library? get() = _library

    override fun bindTo(library: Library) {
        _library = library
    }

    override fun dispose() {
        _library = null
    }
}