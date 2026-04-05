package io.antarescircuit.antares.view.app

import io.antarescircuit.antares.model.net.DigitalNet
import io.antarescircuit.antares.model.port.DigitalPort
import io.antarescircuit.antares.model.signal.BitWidth
import io.antarescircuit.antares.model.vertice.AdjustableBitWidth
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.edit.Command
import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.graph.model.Port
import io.antarescircuit.jabbah.graph.view.connect.ConnectionEstablishedHandler

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
        val adjustedPort = if (otherPort != null && otherPort.owner is AdjustableBitWidth && otherPort.owner!!.id > port.owner!!.id) {
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