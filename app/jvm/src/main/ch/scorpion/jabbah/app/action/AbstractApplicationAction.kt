package ch.scorpion.jabbah.app.action

import ch.scorpion.jabbah.app.DesktopApplication
import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.Translations

/**
 * An abstract base class for application level [Action]s.
 */
abstract class AbstractApplicationAction(
    name: String,
    description: String? = null,
    accelerator: String? = null,
    protected val application: DesktopApplication
): AbstractAction(name, description, accelerator) {

    constructor(
            baseName: String,
            application: DesktopApplication
    ) : this(
            Translations.getString("$baseName.name"),
            null,
            null,
            application)
}