package ch.scorpion.jabbah.base.collection

import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for [PriorityQueue].
 */
class PriorityQueueTest {

    @Test
    fun shouldSort() {
        val queue = PriorityQueue<Int>()

        queue.add(10)
        assertThat(queue.peek(), `is`(10))
        queue.add(9)
        assertThat(queue.peek(), `is`(9))
        queue.add(11)
        assertThat(queue.peek(), `is`(9))
    }

    @Test
    fun shouldRemove() {
        val queue = PriorityQueue<Int>()
        queue.add(1)
        queue.add(2)
        queue.add(3)

        queue.remove()
        assertThat(queue.peek(), `is`(2))
        assertThat(queue.size, `is`(2))
    }
}