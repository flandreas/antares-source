package ch.scorpion.jabbah.edit.auth

import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.UUID
import kotlin.js.JsExport

/**
 * Unique identification of a [User].
 *
 * Cannot be [UUID] because [User] identities can also be created by third-party
 * identity providers like Auth0.
 */
@JsExport
data class UserIdentity(val id: String) {

	companion object {
		val DEVELOPER = UserIdentity("5ecf330b-e395-4e17-88b0-0883834b384a")
		val ANYBODY = UserIdentity("7e840175-f9c4-4886-a221-ef91e3493e27")

		fun random(): UserIdentity = UserIdentity(System.createUUID(null).toString())
	}

	override fun toString(): String = id
}

interface User {
	val identity: UserIdentity
	val name: String
	val isDeveloper: Boolean
}

