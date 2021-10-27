package ch.scorpion.jabbah.draw.view

import ch.scorpion.jabbah.base.collection.indexOfFirstOrNull
import ch.scorpion.jabbah.base.event.PropertyOwner
import ch.scorpion.jabbah.base.event.PropertyOwnerImpl
import ch.scorpion.jabbah.base.geom.Dimension2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.draw.View

/**
 * [ViewSpace] defined the [Rectangle2D] area that is visible to the user and therefore
 * available to display content.
 *
 * The [ViewSpace] can be temporarily reduced by overlays (or similar things) being displayed
 * over the [View].
 *
 * All coordinates and sizes are expressed in the [View] coordinate system.
 *
 * Currently only supports top reductions. Reductions at the other three sides can be
 * added accordingly when needed. Reacting the changes of [viewDimension] is not needed
 * until reductions at the right right and bottom edge are supported.
 */
class ViewSpace(
	viewDimension: Dimension2D,
	private val propertyOwner: PropertyOwner<Rectangle2D> = PropertyOwnerImpl()
) : PropertyOwner<Rectangle2D> by propertyOwner {

	companion object {
		const val PROP_AREA = "ViewSpace.area"
	}

	var viewDimension: Dimension2D = viewDimension
		set(value) {
			if (field != value) {
				field = value
				updateArea()
			}
		}

	private var topReductions = mutableListOf<Int>()

	var area: Rectangle2D = Rectangle2D(0, 0, viewDimension.widthInt, viewDimension.heightInt)
		private set

	val widthInt: Int get() = area.widthInt
	val heightInt: Int get() = area.heightInt

	init {
		propertyOwner.source = this
		updateArea()
	}

	fun reduceTop(height: Int) {
		topReductions.add(height)
		updateArea()
	}

	fun removeTopReduction(height: Int) {
		topReductions.indexOfFirstOrNull { it == height }?.let { topReductions.removeAt(it) }
		updateArea()
	}

	private fun updateArea() {
		val oldValue = area
		val topReduction = topReductions.maxOrNull() ?: 0
		area = Rectangle2D(0, topReduction, viewDimension.widthInt, viewDimension.heightInt - topReduction)
		fire(PROP_AREA, oldValue, area)
	}
}