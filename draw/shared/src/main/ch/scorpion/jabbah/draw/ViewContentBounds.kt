package ch.scorpion.jabbah.draw

import ch.scorpion.jabbah.base.event.PropertyOwner
import ch.scorpion.jabbah.base.event.PropertyOwnerImpl
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.base.geom.RectangularShape

/**
 * [ViewContentBounds] defines the bounds of the contents displayed by a [View].
 * The bounds are expressed in model coordinate space.
 *
 * While the content bounds are typically defined by the bounds of the "main" content,
 * such as a drawing displayed by the [View], there are situation where these bounds
 * can be expanded by additional [Drawables][Drawable], such as explanations displayed
 * above or below the main content. In such a scenario, the "main" content bounds
 * would be used to calculate positions above or below it, and the "total" content bounds,
 * which consist of the main content bounds and all expansions, is used to when calculating
 * a zoom factor that allows to display both content types.
 *
 * @property mainBoundsAccessor allows access to the bounds of the main content, typically
 * maintained by the [View].
 */
class ViewContentBounds(
	private val propertyOwner: PropertyOwner<RectangularShape> = PropertyOwnerImpl(),
	private val mainBoundsAccessor: () -> RectangularShape
) : PropertyOwner<RectangularShape> by propertyOwner {

	companion object {
		const val PROP_TOTAL = "ViewContentBounds.total"
	}

	private val expansions = mutableListOf<Drawable>()

	/** Returns the bounds of the main content.*/
	val main: RectangularShape get() = mainBoundsAccessor()

	/**
	 * Returns the bounds of the total content, which is [main] plus the bounding boxes
	 * of all [Drawables][Drawable] added by [expandBy].
	 */
	val total: RectangularShape get() {
		var result = main
		if (expansions.isNotEmpty()) {
			result = Rectangle2D(result)
			expansions.forEach { result.add(it.boundingBox) }
		}
		return result
	}

	init {
		propertyOwner.source = this
	}

	/** Expands the [main] bounds by the bounding box of the specified [Drawable].*/
	fun expandBy(drawable: Drawable) {
		val oldValue = total
		expansions.add(drawable)
		fire(PROP_TOTAL, oldValue, total)
	}

	/** Removes an expansion previously added by [expandBy].*/
	fun removeExpansion(drawable: Drawable) {
		val oldValue = total
		expansions.remove(drawable)
		fire(PROP_TOTAL, oldValue, total)
	}
}