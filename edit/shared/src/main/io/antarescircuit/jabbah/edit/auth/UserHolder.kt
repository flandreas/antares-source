package io.antarescircuit.jabbah.edit.auth

/** Holds the one and only current [User]. */
interface UserHolder<out T : User> {

	/** Gets the previously set current [User]. */
	val user: T
}

