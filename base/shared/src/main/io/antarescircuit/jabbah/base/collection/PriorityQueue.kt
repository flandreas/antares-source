package io.antarescircuit.jabbah.base.collection

/**
 * A stable binary min-heap. Insertion and removal of the head take O(log n), and peeking takes O(1).
 * Elements with equal priority are returned in insertion order.
 */
class PriorityQueue<T : Comparable<T>> {

	private class Entry<T : Comparable<T>>(val value: T, val order: Long) : Comparable<Entry<T>> {
		override fun compareTo(other: Entry<T>): Int {
			val priority = value.compareTo(other.value)
			return if (priority != 0) priority else order.compareTo(other.order)
		}
	}

	private val heap = mutableListOf<Entry<T>>()
	private var nextOrder = 0L

	val size: Int get() = heap.size
	val isEmpty: Boolean get() = heap.isEmpty()

	/** Returns the least element without removing it. */
	fun peek(): T? = heap.firstOrNull()?.value

	fun add(elem: T) {
		heap.add(Entry(elem, nextOrder++))
		siftUp(heap.lastIndex)
	}

	/** Removes and returns the least element. */
	fun remove(): T {
		if (isEmpty) {
			throw NoSuchElementException("empty queue")
		}
		return removeAt(0)
	}

	/** Removes the first matching element in priority order. Finding it takes O(n). */
	fun remove(elem: T) {
		var index = -1
		for (i in heap.indices) {
			if (heap[i].value == elem && (index < 0 || heap[i] < heap[index])) {
				index = i
			}
		}
		if (index >= 0) {
			removeAt(index)
		}
	}

	/** Finds the least matching element in O(n), without sorting or copying the queue. */
	fun find(predicate: (T) -> Boolean): T? {
		var result: Entry<T>? = null
		for (entry in heap) {
			if (predicate(entry.value) && (result == null || entry < result)) result = entry
		}
		return result?.value
	}

	/** Returns a sorted snapshot, primarily for diagnostics. */
	fun elements(): ImmutableList<T> = heap.sorted().map { it.value }.toImmutableList()

	fun clear() {
		heap.clear()
		nextOrder = 0
	}

	private fun removeAt(index: Int): T {
		val result = heap[index].value
		val last = heap.removeAt(heap.lastIndex)
		if (index < heap.size) {
			heap[index] = last
			if (index > 0 && heap[index] < heap[(index - 1) / 2]) {
				siftUp(index)
			} else {
				siftDown(index)
			}
		}
		if (isEmpty) {
			nextOrder = 0
		}
		return result
	}

	private fun siftUp(start: Int) {
		var index = start
		val entry = heap[index]
		while (index > 0) {
			val parent = (index - 1) / 2
			if (entry >= heap[parent]) break
			heap[index] = heap[parent]
			index = parent
		}
		heap[index] = entry
	}

	private fun siftDown(start: Int) {
		var index = start
		val entry = heap[index]
		while (index < heap.size / 2) {
			var child = index * 2 + 1
			if (child + 1 < heap.size && heap[child + 1] < heap[child]) child++
			if (entry <= heap[child]) break
			heap[index] = heap[child]
			index = child
		}
		heap[index] = entry
	}
}
