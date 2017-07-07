package ch.scorpion.jabbah.edit

import ch.scorpion.jabbah.base.AbstractAction

/**
 * A base [Action] that keeps a reference to an [Editor].
 */
abstract class AbstractEditAction(baseName: String, val editor: Editor) : AbstractAction(baseName)