package ch.scorpion.jabbah.animation

/**
 * A [Sequence] that loops forever over the specified [sequence] followed by its
 * reversed [ReversibleSequence].
 * Note that in this implementation, the boundary values are returned twice whenever
 * the direction in the [Oscillation] changes.
 */
class Oscillation<out T>(
	sequence: ReversibleSequence<T>
) : Sequence<T> {

	private var currentSequence: ReversibleSequence<T> = sequence

	override val size: Double = sequence.size

	override fun hasNext(): Boolean = true

	override fun getNext(distance: Double): T {
		if (!currentSequence.hasNext()) {
			currentSequence = currentSequence.clone(reversed = true)
		}
		return currentSequence.getNext(distance)
	}

	override fun getCurrent(): T = currentSequence.getCurrent()
}