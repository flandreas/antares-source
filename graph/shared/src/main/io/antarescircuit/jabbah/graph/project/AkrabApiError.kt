package io.antarescircuit.jabbah.graph.project

import kotlinx.serialization.Serializable
import kotlin.js.JsExport

@Serializable
@JsExport
data class AkrabApiError(
	val type: String,
	val msg: String? = null
) {
	companion object {
		const val TYPE_QUOTA = "quota"
		const val TYPE_ERROR = "error"

		fun quota(msg: String): AkrabApiError = AkrabApiError(TYPE_QUOTA, msg)
	}
}

@JsExport
class AkrabApiException(val error: AkrabApiError) : Exception()