package io.antarescircuit.jabbah.graph.login

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
