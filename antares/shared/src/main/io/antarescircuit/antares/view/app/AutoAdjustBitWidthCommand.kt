package io.antarescircuit.antares.view.app

import io.antarescircuit.antares.model.signal.BitWidth
import io.antarescircuit.antares.model.vertice.AdjustableBitWidth
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.edit.Undoable
import io.antarescircuit.jabbah.edit.command.AbstractCommand
import io.antarescircuit.jabbah.graph.view.GraphView

/**
 * Doesn't need to be [Undoable], because it is always executed in the
 * context of other non-undoable connection commands.
 */
class AutoAdjustBitWidthCommand(
    editor: Editor,
    private val targetId: Int,
    private val portId: Int,
    private val bitWidth: BitWidth
) : AbstractCommand("antares.autoAdjustBitWidth.command.name", editor) {

    private val adjustableBitWidth: AdjustableBitWidth get() = (editor!!.drawing as GraphView).graph!!.withId(targetId) as AdjustableBitWidth

    override fun execute() {
        try {
            adjustableBitWidth.adjustBitWidth(portId, bitWidth)
        } catch (e: Exception) {
            throw IllegalArgumentException("${Translations.getString("antares.autoAdjustBitWidth.command.name")} - ${e.message ?: ""}", e)
        }
    }
}