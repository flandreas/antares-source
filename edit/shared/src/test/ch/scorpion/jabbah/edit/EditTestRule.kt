package ch.scorpion.jabbah.edit

import ch.scorpion.jabbah.edit.auth.DesktopUser
import ch.scorpion.jabbah.edit.auth.DesktopUserHolder
import ch.scorpion.jabbah.edit.auth.EditAuthModule
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.edit.select.EditSelectModule
import ch.scorpion.jabbah.edit.select.SelectionModelFactory
import ch.scorpion.jabbah.edit.select.SimpleSelectionModelProvider

/**
 * Basic setup of unit tests in the [ch.scorpion.jabbah.edit] package.
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