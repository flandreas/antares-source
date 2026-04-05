package io.antarescircuit.jabbah.graph.login

import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.net.httpClient
import io.antarescircuit.jabbah.base.logger
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import java.net.URL

interface LoginService {

    /**
     * Tries to log the user in at the remote backend.
     *
     * @return `false` if username or password is wrong
     * @throws IllegalStateException if a technical error occurred, including if no
     * connection to the server could be established
     */
    suspend fun login(request: LoginRequest): Boolean

    fun logout()
}

class LoginServiceJvm(
    private val baseUrl: URL
) : LoginService {

    companion object {
        private val LOG by logger(LoginServiceJvm::class)
    }

    override suspend fun login(request: LoginRequest): Boolean {
        LOG.userTrail("Login as user '${request.username}'")

        val response: HttpResponse = httpClient.post("$baseUrl/login") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        if (response.status == HttpStatusCode.BadRequest) {
            return false
        }
        if (response.status != HttpStatusCode.OK) {
            LOG.error("Status ${response.status} received while login")
            throw IllegalStateException(Translations.getString("base.technicalError.msg.txt", response.status))
        }

        val loginResponse = response.body<LoginResponse>()
        Session.establish(SessionData(request.username, loginResponse.userNickname))

        LOG.info("Successfully logged in to Akrab")

        return true
    }

    override fun logout() {
        LOG.userTrail("Logout")
        Session.drop()
    }

    @Serializable
    private data class LoginResponse(
        val userNickname: String
    )
}