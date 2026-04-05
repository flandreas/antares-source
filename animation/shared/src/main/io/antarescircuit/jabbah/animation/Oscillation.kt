package io.antarescircuit.jabbah.animation

/**
 * A [io.antarescircuit.jabbah.animation.Sequence] that loops forever over the specified [sequence] followed by its
 * reversed [io.antarescircuit.jabbah.animation.CloneableSequence].
 * Note that in this implementation, the boundary values are returned twice whenever
 * the direction in the [Oscillation] changes.
 */
class Oscillation<out T>(
	sequence: io.antarescircuit.jabbah.animation.CloneableSequence<T>
) : io.antarescircuit.jabbah.animation.Sequence<T> {

	private var currentSequence: io.antarescircuit.jabbah.animation.CloneableSequence<T> = sequence

	override val size: Double = sequence.size

	override fun getNext(distance: Double): T? {
		val next = currentSequence.getNext(distance)
		if (next != null) {
			return next
		}
		currentSequence = currentSequence.clone(reversed = true)
		return currentSequence.getNext(distance)
	}

	override fun getCurrent(): T? = currentSequence.getCurrent()
}