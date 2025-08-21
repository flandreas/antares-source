package ch.scorpion.antares.view.app

import ch.scorpion.antares.model.net.DigitalNet
import ch.scorpion.antares.model.port.DigitalPort
import ch.scorpion.antares.model.vertice.AdjustableBitWidth
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.Command
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.graph.model.Port
import ch.scorpion.jabbah.graph.view.connect.ConnectionEstablishedHandler

object AutoAdjustBitWidth : ConnectionEstablishedHandler {

    override fun handle(editor: Editor, port: Port<*>): Command? {
        if (port.net == null) {
            return null
        }
        if (!BaseModule.properties.getBoolean(DigitalPort.PROP_ADJUST_BIT_WIDTH)) {
            return null
        }
        if (port !is DigitalPort || port.net !is DigitalNet) {
            return null
        }

        val net = port.net as DigitalNet
        val netBitWidth = net.establishedBitWidthBesidesPort(port)
        if (netBitWidth != null && port.bitWidth != netBitWidth) {
            // Note: This Port has already been added to [net]
            val youngestPort = net.youngestAdjustablePortBesides(port)
            val adjustedPort = if (youngestPort != null && youngestPort.owner!!.id > port.owner!!.id) {
                youngestPort
            } else if (port.owner is AdjustableBitWidth) {
                port
            } else {
                null
            }
            if (adjustedPort != null) {
                val newBitWidth = if (adjustedPort === port) {
                    netBitWidth
                } else {
                    port.bitWidth
                }
                return AutoAdjustBitWidthCommand(
                    editor,
                    adjustedPort.owner!!.id,
                    adjustedPort.portId,
                    newBitWidth
                )
            }
        }

        return null
    }
}