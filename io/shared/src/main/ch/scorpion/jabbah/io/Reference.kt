package ch.scorpion.jabbah.io

/**
 * Used for deferring the resolving of references to [Storable]s.
 */
data class Reference(
	val name: String? = null,
	val referenceId: Int = 0,
	val additionalInfo: Any? = null,
	val resolveAfter: List<Int>? = null,
	val dummy: Boolean = false
)