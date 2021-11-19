@file:JsModule("@auth0/auth0-react")
@file:JsNonModule
package ch.scorpion.jabbah.base.auth0

import react.Props
import kotlin.js.Promise

external interface Auth0ContextInterface {
	fun getAccessTokenSilently(): Promise<String>
}

external fun useAuth0(props: Props? = definedExternally): Auth0ContextInterface