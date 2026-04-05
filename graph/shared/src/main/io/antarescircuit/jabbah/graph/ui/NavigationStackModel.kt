package io.antarescircuit.jabbah.graph.ui

import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.draw.view.ZoomedPointTranslation
import io.antarescircuit.jabbah.edit.Drawing
import io.antarescircuit.jabbah.edit.DrawingViewContent
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.vertice.SubGraphVerticeView

/**
 * An entry in a [NavigationStack] that represents a [DrawingViewContent].
 *
 * @property subGraphVerticeView the [SubGraphVerticeView] whose opening lead to this entry
 * @property content the [DrawingViewContent] to be displayed if this entry is at the "head"
 * of the [NavigationStack]
 * @param voyageOrigin used to capture the origin camera settings when diving into [subGraphVerticeView]
 */
data class NavigationStackEntry<T : GraphView>(
	val subGraphVerticeView: SubGraphVerticeView<*>? = null,
	val content: DrawingViewContent<T>,
	var voyageOrigin: ZoomedPointTranslation? = null
) {
	val name: String get() = SubGraphVerticeView.getDescribingName(subGraphVerticeView?.label, content.drawing.graph!!)

	fun dispose() {
		content.dispose()
	}
}

/**
 * Posted by [NavigationStack] whenever its head has changed.
 *
 * @property isExpansion `true` if the [NavigationStack] has been expanded,`false` if it has been reduced
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

/**
 * Represents the stack of [DrawingViewContent]s the user created while navigation
 * through the hierarchy of [GraphView]s.
 *
 * The first [NavigationStackEntry] is called "root entry", while the last one
 * is called "head". The "head" entry contains the [DrawingViewContent] currently
 * to be displayed.
 *
 * Posts a [NavigationStackEvent] whenever the head [DrawingViewContent] has changed.
 */
class NavigationStack<T : GraphView>(
	val eventBus: EventBus = BaseModule.eventBus
) {

	/**
	 * Holds the [NavigationStackEntries][NavigationStackEntry] that make up the stack. The last element is the stack head.
	 * i.e. the currently displayed [DrawingViewContent].
	 */
	private val entries: MutableList<NavigationStackEntry<T>> = mutableListOf()

	val size: Int get() = entries.size

	var rootEntry: NavigationStackEntry<T>?
		get() {
			if (entries.size > 0) {
				return entries[0]
			}
			return null
		}
		set(value) {
			require(value != null)
			entries.clear()
			push(value)
		}

	fun dispose() {
		entries.forEach { it.dispose() }
	}

	fun iterator(): Iterator<NavigationStackEntry<T>> = entries.iterator()

	/** Returns the [NavigationStackEntry] at the head of this [NavigationStack] without removing it.*/
	fun peek(): NavigationStackEntry<T> {
		if (entries.isEmpty()) {
			throw NoSuchElementException("empty")
		}
		return entries[entries.size - 1]
	}

	/**
	 * Adds the specified [NavigationStackEntry] to the top of this [NavigationStack],
	 * making it the new "head" entry
	 .*/
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

	/**
	 * Navigates back to the specified [NavigationStackEntry], which will make it
	 * the new "head" entry.
	 */
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

	/** Finds the first [DrawingViewContent] that fulfills the specified condition, if any.*/
	fun find(condition: (DrawingViewContent<T>) -> Boolean): DrawingViewContent<T>? {
		return entries.firstOrNull { condition.invoke(it.content) }?.content
	}

	/** Executes the specified action for all [DrawingViewContent]s.*/
	fun forAllContents(action: (DrawingViewContent<Drawing<*>>) -> Unit) {
		entries.forEach { action.invoke(it.content as DrawingViewContent<Drawing<*>>) }
	}

	fun entry(index: Int): NavigationStackEntry<T>? {
		if (index < 0 || index >= entries.size) {
			return null
		}
		return entries[index]
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
