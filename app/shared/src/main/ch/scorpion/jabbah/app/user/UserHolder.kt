package ch.scorpion.jabbah.app.user

import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.logger

/** Holds the one and only current [User].*/
class UserHolder(
	u: User? = null,
	private val eventBus: EventBus = BaseModule.eventBus
) {

	companion object {
		private val LOG by logger(UserHolder::class)
	}

	var u: User? = u
		set(value) {
			if (field != value) {
				LOG.debug("setting current User")
				val oldValue = field
				field = value
				if (oldValue != null) {
					eventBus.post(CurrentUserEvent(field!!))
				}
			}
		}

	val user: User get() = u!!
}

/** Posted on [EventBus] when the current [User] has changed.*/
data class CurrentUserEvent(val user: User)