package ch.scorpion.jabbah.base.auth0

import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.event.EventBus

data class Auth0SessionEvent(val loginInfo: Auth0LoginInfo?)

/**
 * Established after successful execution of an [Auth0LoginFlow].
 * Holds [Auth0LoginInfo] used for calling protected backend services.
 *
 * Posts [Auth0SessionEvent] on the system's [EventBus] when the session is established or dropped.
 */
object Auth0Session {

	private val LOG by logger(Auth0Session::class)

	val exists: Boolean get() = loginInfo != null

	var loginInfo: Auth0LoginInfo? = null
		private set(value) {
			if (field != value) {
				field = value
				BaseModule.eventBus.post(Auth0SessionEvent(value))
			}
		}

	fun establish(loginInfo: Auth0LoginInfo) {
		this.loginInfo = loginInfo
		LOG.info("Established Auth0Session")
	}

	fun drop() {
		LOG.info("Dropping Auth0Session")
		loginInfo = null
	}
}