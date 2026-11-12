package io.antarescircuit.jabbah.draw.view.find

/** Keeps a stable, wrapping cursor while the user visits search matches. */
class SearchNavigation<T> {
	private var matches: List<T> = emptyList()
	private var index = -1

	val result: SearchResult get() = SearchResult(matches.size, index + 1)

	fun reset(matches: List<T>) {
		this.matches = matches
		index = -1
	}

	fun move(forward: Boolean, isValid: (T) -> Boolean = { true }): T? {
		val current = matches.getOrNull(index)
		matches = matches.filter(isValid)
		index = if (current == null) -1 else matches.indexOf(current)
		if (matches.isEmpty()) return null
		index = if (index < 0) {
			if (forward) 0 else matches.lastIndex
		} else {
			(index + (if (forward) 1 else -1) + matches.size) % matches.size
		}
		return matches[index]
	}
}
