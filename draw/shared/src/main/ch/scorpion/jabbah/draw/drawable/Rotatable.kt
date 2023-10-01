package ch.scorpion.jabbah.draw.drawable

import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rotation
import ch.scorpion.jabbah.draw.DrawableContainer

/**
 * The direction to which a [Rotatable] is rotated in a single user interaction.
 * Supports only 90 degree step rotations.
 *
 * @property rotation the [Rotation] angle covered with one rotation step to this [RotationDirection]
 */
enum class RotationDirection(val rotation: Rotation) {
	Clockwise(Rotation.R270),
	CounterClockwise(Rotation.R90);

	companion object {
		fun of(clockwise: Boolean): RotationDirection = if (clockwise) {
			Clockwise
		} else {
			CounterClockwise
		}

		fun notOf(clockwise: Boolean): RotationDirection = if (clockwise) {
			CounterClockwise
		} else {
			Clockwise
		}
	}
}

/**
 * Represents a [Locatable] that can be rotated interactively.
 */
interface Rotatable : Locatable {

	companion object {
		fun rotate(rotatables: Collection<Rotatable>, direction: RotationDirection, pivot: Point2D? = null) {
			rotatables.forEach { it.prepareRotateBy(rotatables) }
			rotatables.forEach { it.rotate(direction, pivot) }
			rotatables.forEach { it.completeRotateBy() }
		}
	}

	/** The unique identification of this [Rotatable] in its containing [DrawableContainer]. */
	val id: Int

	/** Determines whether this [Rotatable] uses its [rotation] property.*/
	val useRotation: Boolean

	/**
	 * Holds the geometrical rotation property of this [Rotatable]. This is automatically accounted for when the [Rotatable]
	 * is drawn, or when its bounding box is calculated.
	 * @throws IllegalArgumentException when this property is set although [useRotation] is `false`
	 */
	var rotation: Rotation

	/**
	 * Determines whether this [Rotatable] can be interactively rotated by the user in terms of
	 * [rotate] together with all objects in [selection], which doesn't necessarily require [useRotation] to be `true`.
	 * Some [Rotatable]s may only be rotated if other objects they are attached to are also rotated.
	 */
	fun isRotatableWith(selection: Collection<*>): Boolean

	/** Informs this [Rotatable] that it is about to be rotated with other [Rotatables][Rotatable]. */
	fun prepareRotateBy(components: Collection<Rotatable>) {}

	/** Informs this [Rotatable] that rotating previously announced by [prepareRotateBy] has been completed for all [Rotatables][Rotatable]. */
	fun completeRotateBy() {}

	/**
	 * Increases the current [Rotation] of this [Rotatable] by 90 degrees to the specified [RotationDirection].
	 *
	 * The default implementation of this method will be to adjust the [rotation] property, which
	 * will lead to an exception if [useRotation] is `false`. However, some [Rotatable] implementation
	 * might have to adjust their geometry when being rotated, so they will implement a different behaviour
	 * of rotation, perhaps one that is based more on orientation [Direction] that on [Rotation] angle.
	 *
	 * @param direction the [RotationDirection] to which this [Rotatable] gets rotated 90 degrees
	 * @param pivot the rotation center. If `null`, this [Rotatable]'s [location] is used.
	 */
	fun rotate(direction: RotationDirection, pivot: Point2D? = null)
}