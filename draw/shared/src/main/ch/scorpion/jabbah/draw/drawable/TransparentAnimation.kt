package ch.scorpion.jabbah.draw.drawable

import ch.scorpion.jabbah.animation.*
import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.DrawableContainer

/**
 * Provides various [AnimationTask]s on [Transparent] objects.
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

		private val LOG by logger(TransparentAnimation::class)

		/** The time in milliseconds for a single color period when glowing.*/
		private const val DEF_GLOW_PERIOD = 300.0

		/**
		 * Creates a [TransparentAnimation] that produces a glow effect on the specified [Transparent]
		 * by oscillating its transparency value by the specified frequency forever.
		 */
		fun glow(transparent: Transparent, frequency: Double = DEF_GLOW_PERIOD): TransparentAnimation {
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

		/**
		 * Combines various [AnimationTask]s to fade-in a [Transparent], hold it for a certain time, and fade-out it again.
		 */
		fun fadeInOut(
			transparent: Transparent,
			container: DrawableContainer<in Drawable>,
			fadeInTimeMs: Int = 300,
			holdTimeMs: Int = 2_000,
			fadeOutTimeMs: Int = 600,
			animator: Animator = AnimationModule.animator
		) {
			transparent.transparency = Transparent.FULLY_TRANSPARENT

			val fadeOutAnimation = fadeOut(transparent, fadeOutTimeMs.toDouble())
			fadeOutAnimation.addListener(object : AnimationTaskAdapter() {
				override fun ended(task: AnimationTask) {
					LOG.debug("remove transparent")
					container.remove(transparent)
				}
			})
			animator.schedule(fadeOutAnimation)

			val timer = System.createTimer()
			timer.initialize(holdTimeMs) {
				LOG.debug("start fade-out animation")
				fadeOutAnimation.start()
				timer.stop()
			}

			val fadeInAnimation = fadeIn(transparent, fadeInTimeMs.toDouble())
			fadeInAnimation.addListener(object : AnimationTaskAdapter() {
				override fun ended(task: AnimationTask) {
					LOG.debug("start timer")
					timer.start()
				}
			})
			animator.schedule(fadeInAnimation)
			LOG.debug("start fade-in animation")
			fadeInAnimation.start()
		}
	}
}