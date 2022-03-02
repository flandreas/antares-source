package ch.scorpion.antares.model.quinemccluskey

internal fun Int.toBinary(n: Int): String =
	toString(2).padStart(n, '0')

internal fun <T> Iterable<T>.powerSet(): Sequence<List<T>> = this.toList().powerSet()

internal fun <T> Collection<T>.powerSet(): Sequence<List<T>> = powerSet(this, sequenceOf(emptyList()))

private tailrec fun <T> powerSet(left: Collection<T>, acc: Sequence<List<T>>): Sequence<List<T>> = when {
	left.isEmpty() -> acc
	else -> powerSet(left.drop(1), acc + acc.map { it + left.first() })
}