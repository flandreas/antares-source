package io.antarescircuit.jabbah.draw.drawable

import io.antarescircuit.jabbah.base.geom.Rotation
import io.antarescircuit.jabbah.base.geom.Direction

/**
 * A [Rotatable] whose rotation is expressed as [Direction] instead of [Rotation].
 */
interface Orientable : Rotatable {

	/** Determines whether an object implementing this [Orientable] interface uses its [orientation] property.*/
	val useOrientation: Boolean get() = useRotation

	/**
	 * The [Direction] into which this [Orientable] is oriented.
	 * Gets transformed to the corresponding [Rotation] value.
	 */
	var orientation: Direction
		get() = Direction.of(rotation)
		set(value) {
			rotation = value.rotation
		}
}