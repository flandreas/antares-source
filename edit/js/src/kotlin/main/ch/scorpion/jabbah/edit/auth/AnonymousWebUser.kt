package ch.scorpion.jabbah.edit.auth

object AnonymousWebUserHolder : UserHolder<AnonymousWebUser> {

	override val user: AnonymousWebUser get() = AnonymousWebUser
}

object AnonymousWebUser : User {

	override val identity: UserIdentity = UserIdentity("anonymous")

	override val name: String = "webUser"

	override val isDeveloper: Boolean = false
}