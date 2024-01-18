package ch.scorpion.jabbah.graph.login

import ch.scorpion.jabbah.base.net.httpClient
import ch.scorpion.jabbah.base.logger
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import java.net.URL

interface LoginService {
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
        if (response.status != HttpStatusCode.OK) {
            return false
        }

        val token = response.bodyAsText()
        Session.establish(SessionData(request.username, token))

        LOG.info("Successfully logged in to Akrab")

        return true
    }

    override fun logout() {
        LOG.userTrail("Logout")
        Session.drop()
    }
}