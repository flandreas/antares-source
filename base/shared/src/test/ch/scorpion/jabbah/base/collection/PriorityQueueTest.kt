package ch.scorpion.jabbah.base.collection

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Unit tests for [PriorityQueue].
 */
class PriorityQueueTest {

    @Test
    fun shouldSort() {
        val queue = PriorityQueue<Int>()

        queue.add(10)
        assertEquals(10, queue.peek())
        queue.add(9)
        assertEquals(9, queue.peek())
        queue.add(11)
        assertEquals(9, queue.peek())
    }

    @Test
    fun shouldRemove() {
        val queue = PriorityQueue<Int>()
        queue.add(1)
        queue.add(2)
        queue.add(3)

        queue.remove()
        assertEquals(2, queue.peek())
        assertEquals(2, queue.size)
    }
}