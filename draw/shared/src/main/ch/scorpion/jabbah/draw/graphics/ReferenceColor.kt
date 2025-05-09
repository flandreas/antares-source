package ch.scorpion.jabbah.draw.graphics

import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.style.Theme
import ch.scorpion.jabbah.draw.style.StyleType

/**
 * A combination of [CompositeColor] used for referencing objects on various backgrounds.
 * @param onBackground used for drawing on objects of [StyleType.BACKGROUND] (and also [StyleType.FIGURE])
 * @param onDark used for drawing on any dark objects. The same as [onBackground] per default.
 */
data class ReferenceColor(
	val onBackground: CompositeColor,
	val onDark: CompositeColor = onBackground
)

/**
 * A provider of [ReferenceColorSequence]s that supports replacing [ReferenceColors][ReferenceColor]
 * due to exchanging [Theme]s.
 */
object ReferenceColorSequenceProvider {

	private val LOG by logger(ReferenceColorSequenceProvider::class)

	private val colors = mutableListOf<ReferenceColor>()

	/** Provides a new [ReferenceColorSequence] by creating a new instance with its own color cycle.*/
	fun provide(): ReferenceColorSequence = FlexibleReferenceColorSequenceImpl()

	/** Replaces all registered [ReferenceColors][ReferenceColor] with the specified ones.*/
	fun replaceColors(colors: List<ReferenceColor>) {
		if (colorCount > 0 && colorCount != colors.size) {
			LOG.error("inconsistent replacement color count old=$colorCount, new=${colors.size}")
			throw IllegalArgumentException("inconsistent color count")
		}

		val replacements = (colors.indices)
			.filter { it < colorCount }
			.map { ReferenceColorReplacement(oldColor = this.colors[it], newColor = colors[it]) }
		this.colors.clear()
		this.colors.addAll(colors)
		BaseModule.eventBus.post(ReferenceColorEvent(replacements))
	}

	/** Returns the number of registered [ReferenceColors][ReferenceColor] of this [ReferenceColorSequenceProvider].*/
	val colorCount: Int get() = colors.size

	/** Returns the registered [ReferenceColor] with the specified index. */
	fun getColor(index: Int): ReferenceColor = colors[index]

	/** Returns the index of the specified registered [ReferenceColor], or -1 if not found. */
	fun indexOf(color: ReferenceColor): Int = colors.indexOf(color)

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

	/** Fetches the next [ReferenceColor] in the sequence. */
	fun next(): ReferenceColor

	/**
	 * Frees the previously fetched referencing [ReferenceColor] when it is not used any more.
	 * The next call of [next] will again fetch this freed [ReferenceColor].
	 */
	fun free(color: ReferenceColor)

	fun reset()
}

/**
 * An implementation of [ReferenceColorSequence] that uses a copy of the colors in [ReferenceColorSequenceProvider].
 * This implementation doesn't react to changes of [ReferenceColors][ReferenceColor] when changing the current [Theme].
 * @Deprecated Use [FlexibleReferenceColorSequenceImpl] instead
 */
class ReferenceColorSequenceImpl(private val colors: List<ReferenceColor>) : ReferenceColorSequence {

	private val usages = colors.map { Usage(it, 0) }

	private inner class Usage(val color: ReferenceColor, var count: Int) : Comparable<Usage> {
		override fun compareTo(other: Usage): Int {
			if (this.count != other.count) {
				return this.count.compareTo(other.count)
			}
			return colors.indexOf(this.color).compareTo(colors.indexOf(other.color))
		}
	}

	override fun next(): ReferenceColor {
		val usage = usages.minOrNull()!!
		usage.count++
		return usage.color
	}

	override fun free(color: ReferenceColor) {
		val usage = usages.find { it.color == color && it.count > 0 } ?: throw IllegalStateException("nothing to free")
		usage.count--
	}

	override fun reset() {
		usages.forEach { it.count = 0 }
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

	override fun next(): ReferenceColor {
		val usage = usages.minOrNull()!!
		usage.count++
		return ReferenceColorSequenceProvider.getColor(usage.index)
	}

	override fun free(color: ReferenceColor) {
		val index = ReferenceColorSequenceProvider.indexOf(color)
		val usage = usages.find { it.index == index && it.count > 0 } ?: throw IllegalStateException("nothing to free")
		usage.count--
	}

	override fun reset() {
		usages.forEach { it.count = 0 }
	}
}

/** Represents the replacement of a [ReferenceColor] to a new one. */
data class ReferenceColorReplacement(val oldColor: ReferenceColor, val newColor: ReferenceColor)

/** Posted by [ReferenceColorSequenceProvider] when the [ReferenceColors][ReferenceColor] have been replaced.*/
data class ReferenceColorEvent(val replacements: Collection<ReferenceColorReplacement>) {

	/** Returns the replacement [ReferenceColor] for the specified old [ReferenceColor].*/
	fun getNewColorFor(oldColor: ReferenceColor): ReferenceColor? =
		replacements.filter { it.oldColor == oldColor }.map { it.newColor }.firstOrNull()
}
