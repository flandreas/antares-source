package io.antarescircuit.jabbah.base.collection

/**
 * A simple implementation of a queue whose items are sorted according to their natural ordering.
 * The head of the queue is the least element.
 *
 *
 * TODO Remove and replace by corresponding class in Kotlin standard library once it its available.
 */
class PriorityQueue<T: Comparable<T>> {

    private val list = mutableListOf<T>()

    val size: Int get() = list.size
    val isEmpty: Boolean get() = list.isEmpty()

    /** Returns the head element without removing it from the queue.*/
    fun peek(): T? {
        if (size > 0) {
            return list[0]
        }
        return null
    }

    fun add(elem: T) {
        list.add(elem)
        list.sort()
    }

    /** Removes the head element from the queue and returns it.*/
    fun remove(): T {
        if (size == 0) {
            throw NoSuchElementException("empty queue")
        }
        return list.removeAt(0)
    }

    fun remove(elem: T) {
        list.remove(elem)
    }

    fun elements(): ImmutableList<T> {
        return list.toImmutableList()
    }

    fun clear() {
        list.clear()
    }
}