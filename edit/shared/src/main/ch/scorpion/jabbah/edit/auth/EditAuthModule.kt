package ch.scorpion.jabbah.edit.auth

import ch.scorpion.jabbah.base.AbstractModule

/**
 * Module definitions for the [ch.scorpion.jabbah.edit.auth] module.
 */
object EditAuthModule : AbstractModule() {

	val userHolder: UserHolder = UserHolderImpl()

	override fun initialize() {
		// empty
	}
}