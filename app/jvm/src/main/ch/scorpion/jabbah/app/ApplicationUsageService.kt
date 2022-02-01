package ch.scorpion.jabbah.app

import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.base.Settings
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.auth.EditAuthModule
import java.math.BigInteger
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

interface ApplicationUsageService {

	/**
	 * Makes a remote call to increment the usage counter for the current application
	 * and the specified user identifier.
	 *
	 * @param applicationId a unique ID of the application using this service. If `null`,
	 * the application ID is determined by the implementation itself
	 * @param userIdentifier an identifier allowing to keep track of how many different users
	 * are using the application. If `null`, implementations have to create their own identifier
	 * that has to conform with current data privacy laws.
	 */
	fun registerUsage(applicationId: String? = null, userIdentifier: String? = null)

	/**
	 * Calls [registerUsage] if the last registration happened more than one day ago.
	 * This allows for tracking accurate usage data even if user keep the application running
	 * for a long period of time, without ever restarting it.
	 *
	 * Call this method from withing the using application at a location that represents a typical
	 * user interaction, such as opening a new document.
	 */
	fun keepAlive(applicationId: String? = null, userIdentifier: String? = null)
}

class RailwayAppUsageServiceImpl(
	private val properties: Properties = BaseModule.properties,
	private val settings: Settings = BaseModule.settings
) : ApplicationUsageService {

	companion object {
		const val PROP_PING_URL = "ch.scorpion.jabbah.app.ApplicationUsageServiceImpl.pingUrl"
		const val PROP_PING_APPLICATION_ID = "ch.scorpion.jabbah.app.ApplicationUsageServiceImpl.applicationId"
		const val PROP_USER_IDENTIFIER = "ch.scorpion.jabbah.app.ApplicationUsageServiceImpl.userId"

		// 1 day
		private const val KEEP_ALIVE_MILLIS = 60 * 60 * 24 * 1_000
	}

	private var lastPingMillis: Long? = null

	override fun registerUsage(applicationId: String?, userIdentifier: String?) {
		// Register even if call fails to avoid retrying too often
		lastPingMillis = System.currentTimeMillis()

		if (EditAuthModule.userHolder.user.isDeveloper) {
			return
		}

		try {
			sendPingRequest(applicationId, userIdentifier)
		} catch (e: Throwable) {
			// Ignore
		}
	}

	override fun keepAlive(applicationId: String?, userIdentifier: String?) {
		if (lastPingMillis == null || System.currentTimeMillis() - lastPingMillis!! >= KEEP_ALIVE_MILLIS) {
			registerUsage(applicationId, userIdentifier)
		}
	}

	private fun sendPingRequest(applicationId: String?, userIdentifier: String?) {
		val pingUrl = properties.getString(PROP_PING_URL)
		val projectId = applicationId ?: URLEncoder.encode(properties.getString(PROP_PING_APPLICATION_ID), StandardCharsets.UTF_8)
		val identifier = URLEncoder.encode(userIdentifier ?: getIdentifier(), StandardCharsets.UTF_8)

		val client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2).build()

		val request = HttpRequest
			.newBuilder()
			.POST(HttpRequest.BodyPublishers.ofString("project=$projectId&identifier=$identifier"))
			.uri(URI.create(pingUrl))
			.setHeader("User-Agent", "Java")
			.header("Content-Type", "application/x-www-form-urlencoded")
			.build()

		client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
	}

	private fun getIdentifier(): String {
		val userIdString = if (settings.containsKey(PROP_USER_IDENTIFIER)) {
			settings.get(PROP_USER_IDENTIFIER)
		} else {
			val uuid = java.util.UUID.randomUUID().toString()
			settings.set(PROP_USER_IDENTIFIER, uuid)
			uuid
		}
		return hash(userIdString)
	}

	private fun hash(value: String): String {
		val msgDigest = MessageDigest.getInstance("SHA-1")
		val inputDigest = msgDigest.digest(value.toByteArray())
		val inputDigestBigInt = BigInteger(1, inputDigest)
		return inputDigestBigInt.toString(16).padStart(32, '0')
	}
}