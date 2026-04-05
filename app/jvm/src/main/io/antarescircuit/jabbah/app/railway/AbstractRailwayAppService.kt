package io.antarescircuit.jabbah.app.railway

import io.antarescircuit.jabbah.base.Properties
import io.antarescircuit.jabbah.base.Settings
import java.math.BigInteger
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

/**
 * Base implementation of services using Noah's RailwayApp REST API.
 */
abstract class AbstractRailwayAppService(
	protected val properties: Properties,
	protected val settings: Settings
) {

	companion object {
		const val PROP_USER_IDENTIFIER = "io.antarescircuit.jabbah.app.ApplicationUsageServiceImpl.userId"
		const val PROP_PING_APPLICATION_ID = "io.antarescircuit.jabbah.app.ApplicationUsageServiceImpl.applicationId"
	}

	protected fun getIdentifier(): String {
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

	protected fun getProjectId(applicationId: String?): String =
		applicationId ?: URLEncoder.encode(properties.getString(PROP_PING_APPLICATION_ID), StandardCharsets.UTF_8)
}