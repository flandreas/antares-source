package ch.scorpion.jabbah.base.collection

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Unit tests for [Pair].
 */
class PairTest {

    @Test
    fun cartesianProduct() {
        val product = Pair.cartesianProduct(setOf(1, 2), setOf(1, 2))

        assertEquals(4, product.size)
        assertTrue(product.contains(Pair(1, 1)))
        assertTrue(product.contains(Pair(1, 2)))
        assertTrue(product.contains(Pair(2, 1)))
        assertTrue(product.contains(Pair(2, 2)))
    }

    @Test
    fun cartesianProductOfEmptyFirstSet() {
        val product = Pair.cartesianProduct(setOf(), setOf(1, 2))
	    assertEquals(0, product.size)
    }

    @Test
    fun cartesianProductOfEmptySecondSet() {
        val product = Pair.cartesianProduct(setOf(1, 2), setOf())
        assertEquals(0, product.size)
    }

}