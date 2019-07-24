package ch.scorpion.jabbah.draw.graphics

import ch.scorpion.jabbah.base.exception.IllegalArgumentException
import ch.scorpion.jabbah.base.exception.IllegalStateException
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.style.Theme

/**
 * A provider of [ReferenceColorSequence]s that supports replacing reference [CompositeColor]s
 * due to exchanging [Theme]s.
 */
object ReferenceColorSequenceProvider {

	private val LOG by logger(ReferenceColorSequenceProvider::class)

	private val colors = mutableListOf<CompositeColor>()

	/** Provides a new [ReferenceColorSequence] by creating a new instance with its own color cycle.*/
	fun provide(): ReferenceColorSequence = FlexibleReferenceColorSequenceImpl()

	/** Replaces all registered reference [CompositeColor]s with the specified ones.*/
	fun replaceColors(colors: List<CompositeColor>) {
		if (colorCount > 0 && colorCount != colors.size) {
			LOG.error("ReferenceColorSequenceProvider: inconsistent replacement color count old=$colorCount, new=${colors.size}")
			throw IllegalArgumentException("inconsistent color count")
		}

		val replacements = (0 until colors.size)
			.filter { it < colorCount }
			.map { ReferenceColorReplacement(oldColor = this.colors[it], newColor = colors[it]) }
		this.colors.clear()
		this.colors.addAll(colors)
		BaseModule.eventBus.post(ReferenceColorEvent(replacements))
	}

	/** Returns the number of registered [CompositeColor]s of this [ReferenceColorSequenceProvider].*/
	val colorCount: Int get() = colors.size

	/** Returns the registered [CompositeColor] with the specified index. */
	fun getColor(index: Int): CompositeColor = colors[index]

	/** Returns the index of the specified registered reference [CompositeColor], or -1 if not found. */
	fun indexOf(color: CompositeColor): Int = colors.indexOf(color)

	/** Removes all registered [CompositeColor] without replacement.*/
	fun clear() {
		this.colors.clear()
		BaseModule.eventBus.post(ReferenceColorEvent(listOf()))
	}
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
 * @Deprecated Use [FlexibleReferenceColorSequenceImpl] instead
 */
class ReferenceColorSequenceImpl(private val colors: List<CompositeColor>) : ReferenceColorSequence {

	private val usages = colors.map { Usage(it, 0) }

	private inner class Usage(val color: CompositeColor, var count: Int) : Comparable<Usage> {
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

/**
 * An implementation of [ReferenceColorSequence] that accesses the colors in [ReferenceColorSequenceProvider].
 * Therefore, it is robust when changing the current [Theme] and its reference [CompositeColor]s.
 */
class FlexibleReferenceColorSequenceImpl : ReferenceColorSequence {

	private val usages = (0 until ReferenceColorSequenceProvider.colorCount).map { Usage(it, 0) }

	private inner class Usage(val index: Int, var count: Int) : Comparable<Usage> {

		override fun compareTo(other: Usage): Int {
			if (this.count != other.count) {
				return this.count.compareTo(other.count)
			}
			return index.compareTo(other.index)
		}
	}

	override fun next(): CompositeColor {
		val usage = usages.sorted().first()
		usage.count++
		return ReferenceColorSequenceProvider.getColor(usage.index)
	}

	override fun free(color: CompositeColor) {
		val index = ReferenceColorSequenceProvider.indexOf(color)
		val usage = usages.find { it.index == index && it.count > 0 } ?: throw IllegalStateException("nothing to free")
		usage.count--
	}

}

/** Represents the replacement of a reference [CompositeColor] to a new one. */
data class ReferenceColorReplacement(val oldColor: CompositeColor, val newColor: CompositeColor)

/** Posted by [ReferenceColorSequenceProvider] when the reference [CompositeColor]s have been replaced.*/
data class ReferenceColorEvent(val replacements: Collection<ReferenceColorReplacement>) {

	/** Returns the replacement reference [CompositeColor] for the specified old reference [CompositeColor].*/
	fun getNewColorFor(oldColor: CompositeColor): CompositeColor? =
		replacements.filter { it.oldColor == oldColor }.map { it.newColor }.firstOrNull()
}
