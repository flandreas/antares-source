package ch.scorpion.jabbah.edit.auth

class DesktopUserHolder(
	override val user: DesktopUser
) : UserHolder<DesktopUser>

/** Represents the current user of the application.*/
data class DesktopUser(
	override val identity: UserIdentity,
	override val name: String,
	override val isDeveloper: Boolean
) : User {

	companion object {
		val developer = DesktopUser(UserIdentity("5ecf330b-e395-4e17-88b0-0883834b384a"), "developer", true)
		val anybody = DesktopUser(UserIdentity("7e840175-f9c4-4886-a221-ef91e3493e27"), "anybody", false)
	}
}