@file:JsModule("@auth0/auth0-react")
@file:JsNonModule
package ch.scorpion.jabbah.base.auth0

import react.FC
import react.Props

external class AppState {
	val returnTo: String?
}

external interface Auth0ProviderOptions : Props {
	var domain: String
	var clientId: String
	var redirectUri: String?
	var audience: String?
	var onRedirectCallback: ((AppState) -> Unit)?
}

external val Auth0Provider: FC<Auth0ProviderOptions>