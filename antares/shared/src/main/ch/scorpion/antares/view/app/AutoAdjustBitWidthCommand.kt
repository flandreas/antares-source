package ch.scorpion.antares.view.app

import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.vertice.AdjustableBitWidth
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.Undoable
import ch.scorpion.jabbah.edit.command.AbstractCommand
import ch.scorpion.jabbah.graph.view.GraphView

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