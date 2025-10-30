package ch.scorpion.antares.view.app

import ch.scorpion.antares.model.net.DigitalNet
import ch.scorpion.antares.model.port.DigitalPort
import ch.scorpion.antares.model.signal.BitWidth
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
            return handleImpl(editor, port, youngestPort, netBitWidth)
        }

        return null
    }

    override fun handle(editor: Editor, port: Port<*>, otherPort: Port<*>): Command? {
        if (!BaseModule.properties.getBoolean(DigitalPort.PROP_ADJUST_BIT_WIDTH)) {
            return null
        }
        if (port !is DigitalPort || otherPort !is DigitalPort) {
            return null
        }

        return handleImpl(editor, port, otherPort, otherPort.bitWidth)
    }

    private fun handleImpl(editor: Editor, port: DigitalPort, otherPort: DigitalPort?, netBitWidth: BitWidth): Command? {
        val adjustedPort = if (otherPort != null && otherPort.owner!!.id > port.owner!!.id) {
            otherPort
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
        return null
    }
}