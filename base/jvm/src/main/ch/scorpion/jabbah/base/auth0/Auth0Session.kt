package ch.scorpion.jabbah.base.auth0

import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule

data class Auth0SessionEvent(val loginInfo: Auth0LoginInfo?)

object Auth0Session {

	private val LOG by logger(Auth0Session::class)

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