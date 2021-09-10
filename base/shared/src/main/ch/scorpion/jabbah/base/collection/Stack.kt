package ch.scorpion.jabbah.base.collection

/**
 * A simple stack implementation.
 */
class Stack<T> {

    val items: MutableList<T> by lazy { mutableListOf()}

    val size: Int get() = items.size

    val empty: Boolean get() = items.isEmpty()

	fun getItem(index: Int): T = items[index]

    /** Pushes an item onto the top of this stack.*/
    fun push(item: T) {
        items.add(item)
    }

    fun pop(): T {
	    if (items.isEmpty()) {
		    throw EmptyStackException()
	    }
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

	fun optionalPeek(): T? = if (empty) null else items.last()

    fun clear() {
        items.clear()
    }

	fun contains(item: T): Boolean = items.contains(item)
}

class EmptyStackException : Throwable("empty stack")
