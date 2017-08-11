package ch.scorpion.jabbah.graph.view.ui

import ch.scorpion.jabbah.base.checkArgument
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.exception.NoSuchElementException
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.DrawingViewContent

/**
 * Represents the stack of [DrawingViewContent]s the user created while navigation through the hierarchy of
 * [Drawing]s.
 * Posts a [NavigationStackEvent] whenever the head [DrawingViewContent] has changed.
 *
 * TODO Refactoring: Move down to edit package
 */
class NavigationStack<T: Drawing<*>>(val eventBus: EventBus = BaseModule.eventBus) {

    /**
     * Holds the [DrawingViewContent]s that make up the stack. The last element is the stack head.
     * i.e. the currently displayed [DrawingViewContent].
     */
    private val list: MutableList<DrawingViewContent<T>> = mutableListOf()

    val size: Int get() = list.size

    /** Holds the root [DrawingViewContent].*/
    var rootContent: DrawingViewContent<T>?
        get() {
            if (list.size > 0) {
                return list[0]
            }
            return null
        }
        set(value) {
            checkArgument(value != null)
            list.clear()
            push(value!!)
        }

    fun iterator(): Iterator<DrawingViewContent<T>> = list.iterator()

    /** Returns the [DrawingViewContent] at the head of this [NavigationStack] without removing it.*/
    fun peek(): DrawingViewContent<T> {
        if (list.isEmpty()) {
            throw NoSuchElementException("empty")
        }
        return list[list.size - 1]
    }

    /** Adds the specified [DrawingViewContent] to the top of this [NavigationStack].*/
    fun push(content: DrawingViewContent<T>) {
        list.add(content)
        postNavigationStackEvent()
    }

    /**
     * Removes the [DrawingViewContent] at the head of this [NavigationStack] and returns it.
     * @return the former head of the stack.
     */
    fun pop(): DrawingViewContent<T> {
        val formerHead = peek()
        removeHead()
        postNavigationStackEvent()
        return formerHead
    }

    /** Navigates back to the specified [DrawingViewContent]. */
    fun navigateBackTo(content: DrawingViewContent<T>) {
        if (!list.contains(content)) {
            throw NoSuchElementException()
        }
        while (list[list.size - 1] != content) {
            removeHead()
        }
        postNavigationStackEvent()
    }

    /** Finds the first [DrawingViewContent] that fulfills the specified condition, if any.*/
    fun find(condition: (DrawingViewContent<T>) -> Boolean): DrawingViewContent<T>? {
        return list.firstOrNull(condition)
    }

    private fun removeHead() {
        val content = peek()
        list.removeAt(list.size - 1)
        content.dispose()
    }

    private fun postNavigationStackEvent() {
        eventBus.post(NavigationStackEvent(this))
    }
}

/** Posted by {@link NavigationStack} whenever its head has changed.*/
data class NavigationStackEvent(val navigationStack: NavigationStack<*>)