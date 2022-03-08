package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.HierarchyVisitor
import ch.scorpion.jabbah.app.Savable
import ch.scorpion.jabbah.edit.model.text.description.Namable
import ch.scorpion.jabbah.io.Storable

/**
 * Represents an item in a [Library].
 */
interface LibraryItem : Storable, Namable {

    val library: Library?

    /**
     * Determines whether this [LibraryItem] is fixed.
     * Fixed [LibraryItem]s are provided by the application and cannot be changed or deleted by the user.
     */
    val isFixed: Boolean

    /**
     * Returns the path of an icon that represents this [LibraryItem] and is suitable to be displayed in a
     * graphical representation of the [Library] that contains this [LibraryItem], such as a tree.
     */
    val iconPath: String?

	/**
	 * The path of an icon to be used if this [LibraryItem] is the one whose content
	 * is currently open in the using UI.
	 */
	val activeIconPath: String? get() = iconPath

    fun accept(visitor: HierarchyVisitor): Boolean

    /** Asks this [LibraryItem] to remember a reference to the [Library] to which it belongs. */
    fun bindTo(library: Library)

	/** Asks this [LibraryItem] to initiate opening its content in the environment. */
	fun open(eventBus: EventBus) {}

    /**
     * Called when the head representation of this [LibraryItem] is not used any more.
     * Implementations should reset any head references, such as unregister from [EventBus],
     * but *not* delete any physical assets associated with them.
     */
    fun dispose()
}

/**
 * A [LibraryItem] that contains (a references) a [Storable] that can be saved within
 * its own [Savable]. In order to conform with undoable snapshot recovery, such objects
 * must react to changing references to that [Storable].
 *
 * @param T the type of the referenced [Storable]
 */
interface UndoableStateLibraryItem<T : Storable> : LibraryItem {

	/**
	 * Asks this [UndoableStateLibraryItem] to update its reference to its [Storable],
	 * e.g. as a consequence of recovery from an undoable snapshot.
	 */
	fun updateStorable(storable: T)
}