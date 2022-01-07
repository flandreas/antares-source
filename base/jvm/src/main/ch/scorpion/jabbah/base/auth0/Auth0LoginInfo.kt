package ch.scorpion.jabbah.base.auth0

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Auth0LoginInfo(
	@SerialName("access_token") val accessToken: String,
	@SerialName("id_token") val idToken: String,
	@SerialName("token_type") val tokenType: String,
	@SerialName("expires_in") val expiresIn: Long)