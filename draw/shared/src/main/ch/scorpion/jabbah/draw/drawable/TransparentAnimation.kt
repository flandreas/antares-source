package ch.scorpion.jabbah.draw.drawable

import ch.scorpion.jabbah.animation.AbstractAnimationTask
import ch.scorpion.jabbah.animation.Sequence
import ch.scorpion.jabbah.animation.DoubleRange
import ch.scorpion.jabbah.animation.SequenceType

/**
 * Creates a glow effect by oscillating the transparency value of a [Transparent].
 */
class TransparentAnimation(
	transparent: Transparent,
	sequence: Sequence<Double>,
	duration: Double
) : AbstractAnimationTask<Double>(
	transparent,
	{
		transparent.transparency = it.toInt()
		transparent.validate()
	},
	sequence,
	duration
) {

	companion object {

		/**
		 * Creates a [TransparentAnimation] that produces a glow effect on the specified [Transparent]
		 * by oscillating its transparency value by the specified frequency forever.
		 */
		fun glow(transparent: Transparent, frequency: Double): TransparentAnimation {
			return TransparentAnimation(transparent, DoubleRange(Transparent.FULLY_OPAQUE.toDouble(), 16.0, SequenceType.OSCILLATION), frequency)
		}

		/**
		 * Creates a [TransparentAnimation] that produces a "fade in" effect on the specified [Transparent]
		 * by raising its transparency value from its current value to maximum value.
		 * @param duration the duration of the animation in milliseconds
		 */
		fun fadeIn(transparent: Transparent, duration: Double): TransparentAnimation {
			return TransparentAnimation(transparent, DoubleRange(Transparent.FULLY_TRANSPARENT.toDouble(), Transparent.FULLY_OPAQUE.toDouble()), duration)
		}

		/**
		 * Creates a [TransparentAnimation] that produces a "fade out" effect on the specified [Transparent]
		 * by reducing its transparency value from its current value to minimum value.
		 * @param duration the duration of the animation in milliseconds
		 */
		fun fadeOut(transparent: Transparent, duration: Double): TransparentAnimation {
			return TransparentAnimation(transparent, DoubleRange(Transparent.FULLY_OPAQUE.toDouble(), Transparent.FULLY_TRANSPARENT.toDouble()), duration)
		}
	}
}