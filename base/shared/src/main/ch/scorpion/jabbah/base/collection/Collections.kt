package ch.scorpion.jabbah.base.collection

import ch.scorpion.jabbah.base.exception.NoSuchElementException

/**
 * Classes and extension function that support immutable projections of collection classes.
 */

class ImmutableList<out T>(private val inner:List<T>) : List<T> by inner
class ImmutableMap<K, out V>(private val inner:Map<K,V>) : Map<K,V> by inner
class ImmutableSet<out T>(private val inner:Set<T>) : Set<T> by inner

/** Creates an immutable projection of a [List].*/
fun <T> List<T>.toImmutableList(): ImmutableList<T> {
    if (this is ImmutableList<T>) {
        return this
    } else {
        return ImmutableList(this)
    }
}

/** Creates an immutable projection of a [Map].*/
@Suppress("unused")
fun <K, V> Map<K, V>.toImmutableMap(): ImmutableMap<K, V> {
    if (this is ImmutableMap<K, V>) {
        return this
    } else {
        return ImmutableMap(this)
    }
}

/** Creates an immutable projection of a [Set].*/
fun <T> Set<T>.toImmutableSet(): ImmutableSet<T> {
    if (this is ImmutableSet<T>) {
        return this
    } else {
        return ImmutableSet(this)
    }
}

inline fun <T> List<T>.indexOfFirstOrNull(predicate: (T) -> Boolean): Int? {
    val index = this.indexOfFirst(predicate)
    return if(index >= 0) index else null
}

class EmptyIterator<out T> : Iterator<T> {

    override fun hasNext(): Boolean = false

    override fun next(): T {
        throw UnsupportedOperationException("not implemented")
    }
}

/** Concatenates an arbitrary amount of [Iterator]s to a single [Iterator].*/
class ConcatIterator<out T>(private var iter: Iterator<T>, vararg val iterators: Iterator<T>) : Iterator<T> {

    private var index: Int = 0

    override fun hasNext(): Boolean {
        return iter.hasNext() || index < iterators.size
    }

    override fun next(): T {
        if (!hasNext()) {
            throw NoSuchElementException()
        }
        if (!iter.hasNext()) {
            iter = iterators[index++]
        }
        return iter.next()
    }
}
