package io.antarescircuit.jabbah.graph

/** Defines number restrictions per user for working with the [io.antarescircuit.jabbah.graph] System.*/
data class GraphQuota(
	val maxLibraries: Int,
	val maxGraphPerLibrary: Int
) {
	companion object {
		val UNLIMITED = GraphQuota(
			maxLibraries = Int.MAX_VALUE,
			maxGraphPerLibrary = Int.MAX_VALUE
		)
	}
}

class GraphQuotaException(
	msg: String,
	val translatedMsg: String
) : Exception(msg)