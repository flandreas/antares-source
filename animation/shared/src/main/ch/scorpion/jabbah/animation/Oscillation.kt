package ch.scorpion.jabbah.animation

/**
 * A [Sequence] that loops forever over the specified [sequence] followed by its
 * reversed [CloneableSequence].
 * Note that in this implementation, the boundary values are returned twice whenever
 * the direction in the [Oscillation] changes.
 */
class Oscillation<out T>(
	sequence: CloneableSequence<T>
) : Sequence<T> {

	private var currentSequence: CloneableSequence<T> = sequence

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