package ch.scorpion.jabbah.base.collection

import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert
import org.junit.Test

/**
 * Unit test for [ConcatIterator].
 */
class ConcatIteratorTest {

    @Test
    fun shouldConcatenate() {
        val iter = ConcatIterator(listOf(1, 2).iterator(), listOf(3, 4).iterator())
        assertThat(iter.next(), `is`(1))
        assertThat(iter.next(), `is`(2))
        assertThat(iter.next(), `is`(3))
        assertThat(iter.next(), `is`(4))
        assertThat(iter.hasNext(), `is`(false))
    }

    @Test
    fun shouldConcatenateMultipleAdditionals() {
        val iter = ConcatIterator(listOf(1, 2).iterator(), listOf(3, 4).iterator(), listOf(5).iterator())
        assertThat(iter.next(), `is`(1))
        assertThat(iter.next(), `is`(2))
        assertThat(iter.next(), `is`(3))
        assertThat(iter.next(), `is`(4))
        assertThat(iter.next(), `is`(5))
        assertThat(iter.hasNext(), `is`(false))
    }

    @Test
    fun shouldNotNeedAdditionalIterators() {
        val iter = ConcatIterator(listOf(1, 2).iterator())
        assertThat(iter.next(), `is`(1))
        assertThat(iter.next(), `is`(2))
        assertThat(iter.hasNext(), `is`(false))
    }

    @Test
    fun shouldntHaveNext() {
        assertThat(ConcatIterator(listOf<Int>().iterator()).hasNext(), `is`(false))
    }
}