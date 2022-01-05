@file:JsModule("@auth0/auth0-react")
@file:JsNonModule
package ch.scorpion.jabbah.base.auth0

import kotlin.js.Promise

external interface LogoutOptions {
	val returnTo: String?
}

external interface Auth0ContextInterface {
	val user: Any?
	val isAuthenticated: Boolean
	fun loginWithRedirect()
	fun logout(options: LogoutOptions)
	fun getAccessTokenSilently(): Promise<String>
}

external val useAuth0: () -> Auth0ContextInterface