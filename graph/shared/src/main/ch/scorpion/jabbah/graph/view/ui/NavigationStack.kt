package ch.scorpion.jabbah.graph.view.ui

import ch.scorpion.jabbah.base.checkArgument
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.exception.NoSuchElementException
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.graph.view.GraphElementView
import ch.scorpion.jabbah.graph.view.GraphView

/**
 * Represents the stack of [GraphView]s the user created while navigation through the hierarchy of
 * [GraphView]s.
 * Posts a [NavigationStackEvent] whenever the head [GraphView] has changed.
 */
class NavigationStack(val eventBus: EventBus) {
    constructor(): this(BaseModule.eventBus)

    /**
     * Holds the [GraphView]s that make up the stack. The last element is the stack head.
     * i.e. the currently displayed [GraphView].
     */
    private val list: MutableList<GraphView<GraphElementView<*>>> = mutableListOf()

    val size: Int get() = list.size

    /** Holds the root [GraphView].*/
    var rootGraphView: GraphView<GraphElementView<*>>?
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

    fun iterator(): Iterator<GraphView<GraphElementView<*>>> = list.iterator()

    /** Returns the [GraphView] at the head of this [NavigationStack] without removing it.*/
    fun peek(): GraphView<GraphElementView<*>> {
        if (list.isEmpty()) {
            throw NoSuchElementException("empty")
        }
        return list[list.size - 1]
    }

    /** Adds the specified [GraphView] to the top of this [NavigationStack].*/
    fun push(graphView: GraphView<GraphElementView<*>>) {
        list.add(graphView)
        postNavigationStackEvent()
    }

    /**
     * Removes the [GraphView] at the head of this [NavigationStack] and returns it.
     * @return the former head of the stack.
     */
    fun pop(): GraphView<GraphElementView<*>> {
        val formerHead = peek()
        removeHead()
        postNavigationStackEvent()
        return formerHead
    }

    /** Navigates back to the specified [GraphView]. */
    fun navigateBackTo(graphView: GraphView<GraphElementView<*>>) {
        if (!list.contains(graphView)) {
            throw NoSuchElementException()
        }
        while (list[list.size - 1] != graphView) {
            removeHead()
        }
        postNavigationStackEvent()
    }

    private fun removeHead() {
        val view = peek()
        list.removeAt(list.size - 1)
        view.dispose()
    }

    private fun postNavigationStackEvent() {
        eventBus.post(NavigationStackEvent(this))
    }
}

/** Posted by {@link NavigationStack} whenever its head has changed.*/
data class NavigationStackEvent(val navigationStack: NavigationStack)