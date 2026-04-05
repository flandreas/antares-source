package io.antarescircuit.jabbah.edit.auth

import io.antarescircuit.jabbah.base.AbstractModule

/**
 * Module definitions for the [io.antarescircuit.jabbah.edit.auth] module.
 */
object EditAuthModule : AbstractModule() {

	lateinit var userHolder: UserHolder<User>

	override fun initialize() {}

	override fun resetDependencies() {}
}