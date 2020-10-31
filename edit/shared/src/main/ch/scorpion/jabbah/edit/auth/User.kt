package ch.scorpion.jabbah.edit.auth

import ch.scorpion.jabbah.base.UUID

/** Represents the current user of the application.*/
data class User(
	val uuid: UUID,
	val name: String,
	val isDeveloper: Boolean
) {

	companion object {
		val developer = User(UUID("5ecf330b-e395-4e17-88b0-0883834b384a"), "developer", true)
		val anybody = User(UUID("7e840175-f9c4-4886-a221-ef91e3493e27"), "anybody", false)
	}
}