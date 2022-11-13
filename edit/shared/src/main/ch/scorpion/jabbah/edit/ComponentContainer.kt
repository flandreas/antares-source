package ch.scorpion.jabbah.edit

import ch.scorpion.jabbah.draw.DrawableContainer
import ch.scorpion.jabbah.io.Storable

/**
 * A storable [DrawableContainer] that contains [Component]s.
 */
interface ComponentContainer<T : Component> : DrawableContainer<T>, Storable {

    /** Returns the [Component] with the specified identification, if present.*/
    fun getWithId(id: Int): T?

	/** Returns the [Components][Component] with the specified identifications. */
	fun getWidthIds(ids: Collection<Int>): Collection<T>

	/**
	 * Returns the position of a [Component] in the stacking order of this [ComponentContainer],
	 * where 0 represents the topmost position.
	 * @throws NoSuchElementException if this [ComponentContainer] doesn't contain a [Component] with [componentId]
	 */
	fun getStackingOrderPosition(componentId: Int): Int

	/**
	 * Sets the stacking order position of the specified [Component], where 0 represents the topmost position.
	 * @throws NoSuchElementException if this [ComponentContainer] doesn't contain a [Component] with [componentId]
	 * @throws IndexOutOfBoundsException if `position` is not a valid stacking order position
	 */
	fun setStackingOrderPosition(position: Int, componentId: Int)

	/**
	 * Converts a [Collection] of [Component]s into a [List] of [StackingOrderPosition]
	 * that is ascending sorted by stacking order positions, i.e. starting with the topmost [Component] at
	 * stacking order position 0.
	 */
	fun getStackingOrderPositions(componentIds: Collection<Int>): List<StackingOrderPosition>
}

data class StackingOrderPosition(
	val position: Int,
	val componentId: Int
) : Comparable<StackingOrderPosition> {

	override fun compareTo(other: StackingOrderPosition): Int {
		return this.position.compareTo(other.position)
	}
}