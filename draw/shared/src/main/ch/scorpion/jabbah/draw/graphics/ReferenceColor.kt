package ch.scorpion.jabbah.draw.graphics

import ch.scorpion.jabbah.base.exception.IllegalStateException

object ReferenceColorSequenceProvider {

    private val colors = mutableListOf<CompositeColor>()

    fun provide(): ReferenceColorSequence {
        val list = mutableListOf<CompositeColor>()
        list.addAll(colors)
        return ReferenceColorSequence(list)
    }

    /**
     * Adds a new [CompositeColor] to extend the predefined sequence of [CompositeColor]s
     * to be sequenced by provided [ReferenceColorSequence]s. This method is typically called
     * during system initialization.
     */
    fun registerColor(color: CompositeColor) {
        colors.add(color)
    }
}

/**
 * Fetches the next referencing [CompositeColor] according to the order of the specified [List].
 * Starts over with the first [CompositeColor] of the sequence after the last one has been fetched.
 */
class ReferenceColorSequence(private val colors: List<CompositeColor>) {

    private val usages = colors.map { Usage(it, 0)}

    private inner class Usage(val color: CompositeColor, var count: Int): Comparable<Usage> {
        override fun compareTo(other: Usage): Int {
            if (this.count != other.count) {
                return this.count.compareTo(other.count)
            }
            return colors.indexOf(this.color).compareTo(colors.indexOf(other.color))
        }
    }

    fun next(): CompositeColor {
        val usage = usages.sorted().first()
        usage.count++
        return usage.color
    }

    /**
     * Frees the previously fetched referencing [CompositeColor] when it is not used any more.
     * The next call of [next] will again fetch this freed reference0 [CompositeColor].
     */
    fun free(color: CompositeColor) {
        val usage = usages.find { it.color == color && it.count > 0 } ?: throw IllegalStateException("nothing to free")
        usage.count--
    }
}