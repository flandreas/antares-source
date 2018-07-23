package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.base.checkArgument
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.exception.NoSuchElementException
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.DrawingViewContent
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeView

data class NavigationStackEntry<T: Drawing<*>>(
	val subGraphVerticeView: SubGraphVerticeView<*>? = null,
	val content: DrawingViewContent<T>
) {
	fun dispose() {
		content.dispose()
	}
}

/**
 * Represents the stack of [DrawingViewContent]s the user created while navigation through the hierarchy of
 * [Drawing]s.
 * Posts a [NavigationStackEvent] whenever the head [DrawingViewContent] has changed.
 */
class NavigationStack<T: Drawing<*>>(val eventBus: EventBus = BaseModule.eventBus) {

    /**
     * Holds the [NavigationStackEntries][NavigationStackEntry] that make up the stack. The last element is the stack head.
     * i.e. the currently displayed [DrawingViewContent].
     */
    private val entries: MutableList<NavigationStackEntry<T>> = mutableListOf()

    val size: Int get() = entries.size

    /** Holds the root [NavigationStackEntry].*/
    var rootEntry: NavigationStackEntry<T>?
        get() {
            if (entries.size > 0) {
                return entries[0]
            }
            return null
        }
        set(value) {
            checkArgument(value != null)
	        entries.clear()
            push(value!!)
        }

    fun iterator(): Iterator<NavigationStackEntry<T>> = entries.iterator()

    /** Returns the [NavigationStackEntry] at the head of this [NavigationStack] without removing it.*/
    fun peek(): NavigationStackEntry<T> {
        if (entries.isEmpty()) {
            throw NoSuchElementException("empty")
        }
        return entries[entries.size - 1]
    }

    /** Adds the specified [NavigationStackEntry] to the top of this [NavigationStack].*/
    fun push(entry: NavigationStackEntry<T>) {
        entries.add(entry)
        postNavigationStackEvent(isExpansion = true, entries = mutableListOf(entry))
    }

    /**
     * Removes the [DrawingViewContent] at the head of this [NavigationStack] and returns it.
     * @return the former head of the stack.
     */
    fun pop(): NavigationStackEntry<T> {
        val formerHead = removeHead()
        postNavigationStackEvent(isExpansion = false, entries = mutableListOf(formerHead))
        return formerHead
    }

    /** Navigates back to the specified [NavigationStackEntry]. */
    fun navigateBackTo(entry: NavigationStackEntry<T>, quickMode: Boolean = false) {
        if (!entries.contains(entry)) {
            throw NoSuchElementException()
        }
	    val removedEntries = mutableListOf<NavigationStackEntry<*>>()
        while (entries[entries.size - 1] != entry) {
	        removedEntries.add(0, removeHead())
        }
        postNavigationStackEvent(isExpansion = false, entries = removedEntries, quickMode = quickMode)
    }

    /** Finds the first [NavigationStackEntry] that fulfills the specified condition, if any.*/
    fun find(condition: (NavigationStackEntry<T>) -> Boolean): NavigationStackEntry<T>? {
        return entries.firstOrNull(condition)
    }

    private fun removeHead(): NavigationStackEntry<T> {
        val entry = peek()
        entries.removeAt(entries.size - 1)
        entry.dispose()
	    return entry
    }

    private fun postNavigationStackEvent(isExpansion: Boolean, entries: List<NavigationStackEntry<*>>, quickMode: Boolean = false) {
        eventBus.post(NavigationStackEvent(isExpansion, this, entries, quickMode))
    }
}

/**
 * Posted by {@link NavigationStack} whenever its head has changed.
 * @property isExpansion `true` if the [NavigationStack] has been expanded, `false` if it has been reduced
 * @property navigationStack the [NavigationStack] where this [NavigationStackEvent] comes from
 * @property entries the list of [NavigationStackEntry] that have been added (in case of an expansion) or removed
 *      (in case of a reduction). The last element of the list is current (in case of an expansion) or the former
 *      (in case of a reduction) head.
 * @property quickMode `true` if the user wishes that the resulting view changes happen quickly, for example
 *      without time-consuming animations.
 */
data class NavigationStackEvent(
	val isExpansion: Boolean,
	val navigationStack: NavigationStack<*>,
	val entries: List<NavigationStackEntry<*>>,
	val quickMode: Boolean = false
)