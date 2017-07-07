package ch.scorpion.jabbah.base.collection

/**
 * A simple stack implementation.
 * TODO Remove and replace by corresponding class in Kotlin standard library once it its available.
 */
class Stack<T> {

    val items: MutableList<T> by lazy { mutableListOf<T>()}

    val size: Int get() = items.size

    val empty: Boolean get() = items.isEmpty()

    /** Pushes an item onto the top of this stack.*/
    fun push(item: T) {
        items.add(item)
    }

    fun pop(): T {
        val obj = peek()
        items.removeAt(items.size - 1)
        return obj
    }

    fun peek(): T {
        if (items.isEmpty()) {
            throw EmptyStackException()
        }
        return items.last()
    }

    fun clear() {
        items.clear()
    }
}

class EmptyStackException : Throwable("empty stack")
