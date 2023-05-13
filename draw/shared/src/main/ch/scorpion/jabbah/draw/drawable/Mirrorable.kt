package ch.scorpion.jabbah.draw.drawable

import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.graphics.Graphics2D

/**
 * A [Drawable] that can possibly be mirrored horizontally and vertically.
 * Mirroring involves updating the geometry of a [Drawable], because the underlying
 * [Graphics2D] doesn't support on-the-fly mirroring  while painting.
 */
interface Mirrorable : Drawable {

	/**
	 * Determines whether this [Mirrorable] can be mirrored horizontally and vertically.
	 * Beside simply implementing the interface, this property supports dynamic behaviour.
	 */
	val canMirror: Boolean get() = true

	/**
	 * Mirrors the geometry of this [Drawable] at the vertical axis defined by the specified x-coordinate.
	 * @throws UnsupportedOperationException if [canMirror] is `false`
	 */
	fun mirrorHorizontally(x: Double)

	/**
	 * Mirrors the geometry of this [Drawable] at the horizontal axis defined by the specified y-coordinate.
	 * @throws UnsupportedOperationException if [canMirror] is `false`
	 */
	fun mirrorVertically(y: Double)

}