package ch.scorpion.jabbah.graph.project

import kotlinx.serialization.Serializable

@Serializable
data class AkrabApiError(
	val type: String,
	val msg: String? = null
)

class AkrabApiException(val error: AkrabApiError) : Exception()