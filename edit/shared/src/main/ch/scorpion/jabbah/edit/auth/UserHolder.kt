package ch.scorpion.jabbah.edit.auth

import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.logger

/** Holds the one and only current [User]. */
interface UserHolder {

	/** Used for setting the current [User]. `lateinit` not possible with custom setter.*/
	var u: User?

	/**
	 * Gets the previously set current [User].
	 * @throws NullPointerException if not set
	 */
	val user: User
}

class UserHolderImpl(
	u: User? = null,
	private val eventBus: EventBus = BaseModule.eventBus
) : UserHolder {

	companion object {
		private val LOG by logger(UserHolder::class)
	}

	override var u: User? = u
		set(value) {
			if (field != value) {
				LOG.trace("setting current User")
				val oldValue = field
				field = value
				if (oldValue != null) {
					eventBus.post(CurrentUserEvent(field!!))
				}
			}
		}

	override val user: User get() = u!!
}

/** Posted on [EventBus] when the current [User] has changed.*/
data class CurrentUserEvent(val user: User)