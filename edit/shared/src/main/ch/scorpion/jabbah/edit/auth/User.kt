package ch.scorpion.jabbah.edit.auth

import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.UUID

/**
 * Unique identification of a [User].
 *
 * Cannot be [UUID] because [User] identities can also be created by third-party
 * identity providers like Auth0.
 */
data class UserIdentity(val id: String) {

	companion object {
		fun random(): UserIdentity = UserIdentity(System.createUUID(null).toString())
	}

	override fun toString(): String = id
}

/** Represents the current user of the application.*/
data class User(
	val identity: UserIdentity,
	val name: String,
	val isDeveloper: Boolean
) {

	companion object {
		val developer = User(UserIdentity("5ecf330b-e395-4e17-88b0-0883834b384a"), "developer", true)
		val anybody = User(UserIdentity("7e840175-f9c4-4886-a221-ef91e3493e27"), "anybody", false)
	}
}