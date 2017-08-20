package ch.scorpion.jabbah.draw.graphics

import ch.scorpion.jabbah.base.exception.IllegalStateException

object ReferenceColorSequenceProvider {

    private val colors = mutableListOf<CompositeColor>()

    fun provide(): ReferenceColorSequence {
        val list = mutableListOf<CompositeColor>()
        list.addAll(colors)
        return ReferenceColorSequenceImpl(list)
    }

    /**
     * Adds a new reference [CompositeColor] to extend the predefined sequence of [CompositeColor]s
     * to be sequenced by provided [ReferenceColorSequence]s. This method is typically called
     * during system initialization.
     */
    fun registerColor(color: CompositeColor) {
        colors.add(color)
    }

    /** Replaces all registered reference [CompositeColor]s with the specified ones.*/
    fun replaceColors(colors: List<CompositeColor>) {
        this.colors.clear()
        this.colors.addAll(colors)
    }

    fun colorCount(): Int = colors.size
}

/**
 * Fetches the next referencing [CompositeColor] provided by [ReferenceColorSequenceProvider].
 * Starts over with the first [CompositeColor] of the sequence after the last one has been fetched.
 */
interface ReferenceColorSequence {

    /** Fetches the next reference [CompositeColor] in the sequence. */
    fun next(): CompositeColor

    /**
     * Frees the previously fetched referencing [CompositeColor] when it is not used any more.
     * The next call of [next] will again fetch this freed reference [CompositeColor].
     */
    fun free(color: CompositeColor)
}

/**
 * An implementation of [ReferenceColorSequence] that uses a copy of the colors in [ReferenceColorSequenceProvider].
 * This implementation doesn't react to changes of reference [CompositeColor]s when changing the current [Theme].
 */
private class ReferenceColorSequenceImpl(private val colors: List<CompositeColor>) : ReferenceColorSequence {

    private val usages = colors.map { Usage(it, 0)}

    private inner class Usage(val color: CompositeColor, var count: Int): Comparable<Usage> {
        override fun compareTo(other: Usage): Int {
            if (this.count != other.count) {
                return this.count.compareTo(other.count)
            }
            return colors.indexOf(this.color).compareTo(colors.indexOf(other.color))
        }
    }

    override fun next(): CompositeColor {
        val usage = usages.sorted().first()
        usage.count++
        return usage.color
    }

    override fun free(color: CompositeColor) {
        val usage = usages.find { it.color == color && it.count > 0 } ?: throw IllegalStateException("nothing to free")
        usage.count--
    }
}
