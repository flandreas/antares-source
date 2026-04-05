package io.antarescircuit.jabbah.graph.view.connect

import io.antarescircuit.jabbah.edit.Command
import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.graph.model.Port
import io.antarescircuit.jabbah.graph.model.Net

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

    /**
     * Same as [handle], but used in scenarios where 2 [Ports][Port] are directory connected,
     * and the [Net] between them doesn't yet exist.
     */
    fun handle(editor: Editor, port: Port<*>, otherPort: Port<*>): Command?
}