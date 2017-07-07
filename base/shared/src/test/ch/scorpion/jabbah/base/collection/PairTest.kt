package ch.scorpion.jabbah.base.collection

import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for [Pair].
 */
class PairTest {

    @Test
    fun cartesianProduct() {
        val product = Pair.cartesianProduct(setOf(1, 2), setOf(1, 2))

        assertThat(product.size, `is`(4))
        assertThat(product.contains(Pair(1, 1)), `is`(true))
        assertThat(product.contains(Pair(1, 2)), `is`(true))
        assertThat(product.contains(Pair(2, 1)), `is`(true))
        assertThat(product.contains(Pair(2, 2)), `is`(true))
    }

    @Test
    fun cartesianProductOfEmptyFirstSet() {
        val product = Pair.cartesianProduct(setOf(), setOf(1, 2))
        assertThat(product.size, `is`(0))
    }

    @Test
    fun cartesianProductOfEmptySecondSet() {
        val product = Pair.cartesianProduct(setOf(1, 2), setOf())
        assertThat(product.size, `is`(0))
    }

}