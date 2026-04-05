package io.antarescircuit.jabbah.animation

/**
 * A [io.antarescircuit.jabbah.animation.Sequence] that repeats forever the specified [io.antarescircuit.jabbah.animation.Sequence].
 */
class Repetition<out T>(
	sequence: io.antarescircuit.jabbah.animation.CloneableSequence<T>
) : io.antarescircuit.jabbah.animation.Sequence<T> {

	private var currentSequence: io.antarescircuit.jabbah.animation.CloneableSequence<T> = sequence

	override val size: Double = sequence.size

	override fun getNext(distance: Double): T? {
		val next = currentSequence.getNext(distance)
		if (next != null) {
			return next
		}
		currentSequence = currentSequence.clone(false)
		return currentSequence.getNext(distance)
	}

	override fun getCurrent(): T? = currentSequence.getCurrent()
}