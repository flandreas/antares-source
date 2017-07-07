package ch.scorpion.jabbah.base.collection

/**
 * Represents a pair of objects of the same type.
 * @param T the type of paired objects.
 */
data class Pair<T>(val first: T, val second: T) {

    companion object {
        fun <T> cartesianProduct(a: Set<T>, b: Set<T>): Set<Pair<T>> {
            val product = mutableSetOf<Pair<T>>()
            for (first in a) {
                for (second in b) {
                    product.add(Pair(first, second))
                }
            }
            return product
        }
    }
}