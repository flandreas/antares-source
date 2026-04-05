package io.antarescircuit.jabbah.draw.view.find

/**
 * A UI object allowing the user to search within the contents it displays.
 */
interface Searchable {

	/**
	 * Determines if this object supports searching.
	 * Used with abstract base classes implementing this interface, while not all
	 * implementations might effectively implement the functionality.
	 */
	val canSearch: Boolean get() = false

	fun showSearchBar() {
		if (!canSearch) {
			throw IllegalStateException("Searchable can't search")
		}
		throw UnsupportedOperationException("No implementation of Searchable.showSearchBar")
	}

	fun hideSearchBar() {}

	fun execute(request: SearchRequest) {
		if (!canSearch) {
			throw IllegalStateException("Searchable can't search")
		}
		throw UnsupportedOperationException("No implementation of Searchable.execute")
	}
}