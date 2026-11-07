package io.antarescircuit.jabbah.base.invocation

import io.antarescircuit.jabbah.base.Bean
import kotlinx.serialization.Serializable

interface UnexpectedErrorService {

    fun buildDescription(versionId: String, stackTrace: String): String

    suspend fun sendUnexpectedError(description: String)

    suspend fun sendErrorDump(path: String): Boolean
}

@Serializable
data class UnexpectedErrorRequest(
    val apiKey: String,
    val description: String
) : Bean
