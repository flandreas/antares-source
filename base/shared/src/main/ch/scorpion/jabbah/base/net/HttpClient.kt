package ch.scorpion.jabbah.base.net

import io.ktor.client.*

//expect fun httpClient(config: HttpClientConfig<*>.() -> Unit = {}): HttpClient

expect val httpClient: HttpClient