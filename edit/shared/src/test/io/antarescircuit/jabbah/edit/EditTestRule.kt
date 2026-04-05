package io.antarescircuit.jabbah.edit

import io.antarescircuit.jabbah.edit.auth.DesktopUser
import io.antarescircuit.jabbah.edit.auth.DesktopUserHolder
import io.antarescircuit.jabbah.edit.auth.EditAuthModule
import io.antarescircuit.jabbah.edit.module.EditModule
import io.antarescircuit.jabbah.edit.select.EditSelectModule
import io.antarescircuit.jabbah.edit.select.SelectionModelFactory
import io.antarescircuit.jabbah.edit.select.SimpleSelectionModelProvider

/**
 * Basic setup of unit tests in the [io.antarescircuit.jabbah.edit] package.
 */
object EditTestRule  {

    fun configure(smFactory: SelectionModelFactory = SelectionModelMockFactory()) {
        EditModule.reset()

        EditModule.require()
	    EditSelectModule.selectionModelFactory = smFactory
	    EditSelectModule.selectionModelProvider = SimpleSelectionModelProvider(EditSelectModule.selectionModelFactory)
	    EditAuthModule.userHolder = DesktopUserHolder(DesktopUser.developer)
    }
}