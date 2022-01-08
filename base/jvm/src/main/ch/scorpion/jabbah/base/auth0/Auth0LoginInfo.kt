package ch.scorpion.jabbah.base.auth0

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Created upon successful completion of [Auth0LoginFlow] and registered
 * in [Auth0Session]. Contains the access token to be used for calling
 * protected REST services in the backend.
 *
 * Serializable in order to be deserialized from an Auth0's response JSON string.
 */
@Serializable
data class Auth0LoginInfo(
	@SerialName("access_token") val accessToken: String,
	@SerialName("id_token") val idToken: String,
	@SerialName("token_type") val tokenType: String,
	@SerialName("expires_in") val expiresIn: Long)