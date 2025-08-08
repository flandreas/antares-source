package ch.scorpion.jabbah.graph.view.connect

import ch.scorpion.jabbah.edit.Command
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.graph.model.Port
import ch.scorpion.jabbah.graph.model.Net

/**
 * Applies additional logic when a [Port] is manually connected to a [Net].
 * Note that this is ONLY to be used on the UI layer where new connections are being made by the user.
 */
interface ConnectionEstablishedHandler {

    /**
     * Encapsulates the additional logic in a [Command] to be included in the
     * undo/redo transaction containing the rest of the connection changes.
     *
     * @param port the [Port] being connected
     * @return the [Command] containing the additional logic, or `null` if no such logic is required
     */
    fun handle(editor: Editor, port: Port<*>): Command?
}