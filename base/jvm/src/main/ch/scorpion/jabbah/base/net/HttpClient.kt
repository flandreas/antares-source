package ch.scorpion.jabbah.base.net

import ch.scorpion.jabbah.base.module.BaseModule
import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.cookies.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

val httpClient: HttpClient by lazy {
    HttpClient(Apache) {
        val timeout = 1_000 * BaseModule.properties.getInt(BaseModule.PROP_CONNECTION_TIMEOUT)
        engine {
            followRedirects = true
            socketTimeout = timeout
            connectTimeout = timeout
            connectionRequestTimeout = timeout
            customizeClient {
                setMaxConnTotal(1000)
                setMaxConnPerRoute(100)
            }
        }
        install(HttpCookies) {}
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
    }
}