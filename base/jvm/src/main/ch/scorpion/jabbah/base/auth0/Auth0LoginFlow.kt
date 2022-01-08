package ch.scorpion.jabbah.base.auth0

import ch.scorpion.jabbah.base.auth0.FlowState.*
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import kotlinx.coroutines.CoroutineScope
import org.apache.commons.codec.binary.Base64
import org.apache.commons.lang3.RandomStringUtils
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.SecureRandom

/** The login parameters as configured in Auth0 for the current application.*/
data class LoginParams(
	val domain: String,
	val clientId: String,
	val redirectUrl: String
)

/** Information created during [Auth0LoginFlow] and used again in later steps of the flow.*/
data class FlowInfo(
	val verifier: String,
	val challenge: String,
	val state: String,
	val authorizationUrl: String)

/** Represents the various steps of [Auth0LoginFlow]. Can be used by a UI to inform the user about these steps.*/
enum class FlowState {
	Startup,
	Initializing,
	EnterCredentials,
	Done
}

/**
 * Performs a login with Auth0 for this desktop application using the system browser for entering credentials.
 * Starts a Spark Server for listening on the loop-back interface for the redirect from Auth0
 * that contains the access code for fetching the access token from Auth0.
 *
 * Source: https://github.com/eduramiba/javafx-example-app-auth0-login
 */
class Auth0LoginFlow(
	scope: CoroutineScope,
	params: LoginParams,
	private val stateChangeHandler: () -> Unit
) {

	companion object {
		private val LOG by logger(Auth0LoginFlow::class)

		const val PROP_AUTH0_DOMAIN = "base.auth0.domain"
		const val PROP_AUTH0_CLIENT_ID = "base.auth0.clientId"
		const val PROP_AUTH0_REDIRECT_URL = "base.auth0.redirectUrl"
	}

	private val flowInfo: FlowInfo = createFlowInfo(params)

	private val redirectListener = Auth0RedirectListener(scope, params, flowInfo)

	private val auth0SessionListener: (Auth0SessionEvent) -> Unit = {
		if (Auth0Session.exists) {
			state = Done
		}
	}

	var state: FlowState = Startup
		private set(value) {
			if (field != value) {
				field = value
				stateChangeHandler()
			}
		}

	init {
		BaseModule.eventBus.register(Auth0SessionEvent::class, auth0SessionListener)
	}

	fun dispose() {
		redirectListener.stop()
		BaseModule.eventBus.unregister(auth0SessionListener)
	}

	/**
	 * Forwards to the next [FlowState].
	 * This call can be blocking and should be run in a coroutine.
	 */
	fun nextState() {
		state = when (state) {
			Startup -> {
				state = Initializing
				redirectListener.start()
				login()
				EnterCredentials
			}
			EnterCredentials -> {
				Done
			}
			else -> state
		}
	}

	fun stop() {
		dispose()
	}

	private fun login() {
		openBrowser(flowInfo.authorizationUrl)
	}

	private fun createFlowInfo(params: LoginParams): FlowInfo {
		val verifier = createCodeVerifier()
		val challenge = createCodeChallenge(verifier)
		val state = RandomStringUtils.randomAlphanumeric(8)
		val authorizationUrl = createAuthorizationUrl(params, challenge, state)

		LOG.info("Challenge: $challenge")

		return FlowInfo(verifier, challenge, state, authorizationUrl)
	}

	private fun createAuthorizationUrl(params: LoginParams, challenge: String, state: String): String {
		return """
			https://${params.domain}/authorize
			?client_id=${params.clientId}
			&response_type=code
			&code_challenge_method=S256
			&code_challenge=$challenge
			&scope=${URLEncoder.encode("openid profile email", StandardCharsets.US_ASCII)}
			&state=$state
			&redirect_uri=${URLEncoder.encode(params.redirectUrl, StandardCharsets.US_ASCII)}
		""".trimIndent().replace(System.lineSeparator(), "")
	}

	private fun openBrowser(authorizationUrl: String) {
		LOG.info(authorizationUrl)
		ch.scorpion.jabbah.base.System.browse(authorizationUrl, "Login")
	}

	private fun createCodeVerifier(): String {
		val random = SecureRandom()
		val code = ByteArray(32)
		random.nextBytes(code)
		return Base64.encodeBase64URLSafeString(code)
	}

	private fun createCodeChallenge(codeVerifier: String): String {
		val bytes = codeVerifier.toByteArray(StandardCharsets.US_ASCII)
		val md = MessageDigest.getInstance("SHA-256")
		md.update(bytes, 0, bytes.size)
		return Base64.encodeBase64URLSafeString(md.digest())
	}
}