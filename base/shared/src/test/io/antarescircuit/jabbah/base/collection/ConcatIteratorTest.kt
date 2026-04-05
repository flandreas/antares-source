package io.antarescircuit.jabbah.base.collection

import kotlin.test.*

/**
 * Unit test for [ConcatIterator].
 */
class ConcatIteratorTest {

    @Test
    fun shouldConcatenate() {
        val iter = ConcatIterator(listOf(1, 2).iterator(), listOf(3, 4).iterator())
        assertEquals(1, iter.next())
        assertEquals(2, iter.next())
        assertEquals(3, iter.next())
        assertEquals(4, iter.next())
        assertFalse(iter.hasNext())
    }

    @Test
    fun shouldConcatenateMultipleAdditionals() {
        val iter = ConcatIterator(listOf(1, 2).iterator(), listOf(3, 4).iterator(), listOf(5).iterator())
        assertEquals(1, iter.next())
        assertEquals(2, iter.next())
        assertEquals(3, iter.next())
        assertEquals(4, iter.next())
        assertEquals(5, iter.next())
        assertFalse(iter.hasNext())
    }

    @Test
    fun shouldNotNeedAdditionalIterators() {
        val iter = ConcatIterator(listOf(1, 2).iterator())
        assertEquals(1, iter.next())
        assertEquals(2, iter.next())
        assertFalse(iter.hasNext())
    }

    @Test
    fun shouldNotHaveNext() {
	    assertFalse(ConcatIterator(listOf<Int>().iterator()).hasNext())
    }
}