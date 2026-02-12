package ch.scorpion.jabbah.edit.auth

import ch.scorpion.jabbah.base.AbstractModule

/**
 * Module definitions for the [ch.scorpion.jabbah.edit.auth] module.
 */
object EditAuthModule : AbstractModule() {

	lateinit var userHolder: UserHolder<User>

	override fun initialize() {}

	override fun resetDependencies() {}
}