package io.antarescircuit.jabbah.edit.auth

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
		val developer = DesktopUser(UserIdentity.DEVELOPER, "developer", true)
		val anybody = DesktopUser(UserIdentity.ANYBODY, "anybody", false)
	}
}