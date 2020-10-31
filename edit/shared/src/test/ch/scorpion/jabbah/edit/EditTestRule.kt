package ch.scorpion.jabbah.edit

import ch.scorpion.jabbah.edit.auth.EditAuthModule
import ch.scorpion.jabbah.edit.auth.User
import ch.scorpion.jabbah.edit.module.EditModule

/**
 * Basic setup of unit tests in the [ch.scorpion.jabbah.edit] package.
 */
object EditTestRule  {

    fun configure() {
        EditModule.require()
	    EditAuthModule.userHolder.u = User.developer
    }
}