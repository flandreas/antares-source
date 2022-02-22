package ch.scorpion.jabbah.graph.project

import kotlinx.serialization.Serializable

@Serializable
data class AkrabApiError(
	val type: String,
	val msg: String? = null
) {
	companion object {
		const val TYPE_QUOTA = "quota"
		const val TYPE_ERROR = "error"
	}
}

class AkrabApiException(val error: AkrabApiError) : Exception()