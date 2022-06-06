package ch.scorpion.jabbah.draw.view.find

data class SearchRequest(
	val searchString: String,
	val match: SearchMatch = SearchMatch.EntireWord,
	val ignoreCase: Boolean = true
)
