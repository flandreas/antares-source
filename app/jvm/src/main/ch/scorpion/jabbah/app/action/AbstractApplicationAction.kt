package ch.scorpion.jabbah.app.action

import ch.scorpion.jabbah.app.Application
import ch.scorpion.jabbah.app.DesktopApplication
import ch.scorpion.jabbah.base.AbstractAction

/**
 * An abstract base class for application level [Action]s.
 */
abstract class AbstractApplicationAction(
    baseName: String,
    protected val application: DesktopApplication
): AbstractAction(baseName)