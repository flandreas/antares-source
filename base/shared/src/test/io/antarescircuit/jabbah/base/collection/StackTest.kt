package io.antarescircuit.jabbah.base.collection

import kotlin.test.*

/**
 * Unit tests for [Stack].
 */
class StackTest {

    var stack = Stack<Int>()

    @Test
    fun test() {
        stack.push(1)
        assertFalse(stack.empty)
        assertEquals(1, stack.size)
        stack.push(2)
        assertEquals(2, stack.size)
        assertEquals(2, stack.peek())
        stack.pop()
        assertEquals(1, stack.size)
        stack.pop()
        assertTrue(stack.empty)
    }
}