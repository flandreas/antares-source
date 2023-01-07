package ch.scorpion.jabbah.animation

/**
 * A [Sequence] that repeats forever the specified [Sequence].
 */
class Repetition<out T>(
	sequence: CloneableSequence<T>
) : Sequence<T> {

	private var currentSequence: CloneableSequence<T> = sequence

	override val size: Double = sequence.size

	override fun hasNext(): Boolean = true

	override fun getNext(distance: Double): T {
		if (!currentSequence.hasNext()) {
			currentSequence = currentSequence.clone(false)
		}
		return currentSequence.getNext(distance)
	}

	override fun getCurrent(): T = currentSequence.getCurrent()
}