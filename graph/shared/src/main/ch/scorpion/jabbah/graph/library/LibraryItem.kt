package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.HierarchyVisitor
import ch.scorpion.jabbah.io.Storable

/**
 * Represents an item in a [Library].
 */
interface LibraryItem : Storable {

    /**
     * Determines whether this [LibraryItem] is fixed.
     * Fixed [LibraryItem]s are provided by the application and cannot be changed or deleted by the user, which is
     * why there is no need to make them persistent.
     */
    val isFixed: Boolean

    /**
     * Returns the name of this [LibraryItem] to be displayed in the graphical representation of the [LibraryItem].
     */
    val name: String

    /**
     * Returns the path of an icon that represents this [LibraryItem] and is suitable to be displayed in a
     * graphical representation of the [Library] that contains this [LibraryItem], such as a tree.
     */
    val iconPath: String?

    fun accept(visitor: HierarchyVisitor): Boolean

    /** Asks this [LibraryItem] to remember a reference to the [Library] to which it belongs. */
    fun bindTo(library: Library)

    /**
     * Called when the head representation of this [LibraryItem] is not used any more.
     * Implementations should reset any head references, such as unregister from [EventBus],
     * but *not* delete any physical assets associated with them.
     */
    fun dispose()

}