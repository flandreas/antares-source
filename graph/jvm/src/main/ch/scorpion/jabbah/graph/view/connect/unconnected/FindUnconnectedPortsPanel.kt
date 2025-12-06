package ch.scorpion.jabbah.graph.view.connect.unconnected

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.swing.EGBL
import java.awt.Component
import java.awt.Frame
import javax.swing.JComboBox
import javax.swing.JLabel
import javax.swing.JOptionPane
import javax.swing.JPanel

/**
 * A [JPanel] for collecting parameters for calling [FindUnconnectedPortsService].
 */
class FindUnconnectedPortsPanel : JPanel() {

    companion object {

        private const val SETTING_TYPE = "graph.unconnectedPortsPanel.type"

        fun showAsDialog(parent: Component = Frame.getFrames()[0]): FindUnconnectedPortsType? {
            val panel = FindUnconnectedPortsPanel()
            val result = when (
                JOptionPane.showConfirmDialog(
                    parent,
                    panel,
                    Translations.getString("graph.action.findUnconnectedPorts.title"),
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE)
            ) {
                JOptionPane.OK_OPTION -> panel.typeField.selectedItem as FindUnconnectedPortsType
                else -> null
            }
            panel.dispose()
            return result
        }
    }

    private val typeLabel = JLabel(Translations.getString("graph.action.findUnconnectedPorts.type") + ":")
    private val typeField = JComboBox<FindUnconnectedPortsType>()

    init {
        FindUnconnectedPortsType.entries.forEach { typeField.addItem(it) }
        buildUI()
        typeField.selectedItem = FindUnconnectedPortsType.withCustomName(
            BaseModule.settings.getString(SETTING_TYPE, FindUnconnectedPortsType.Inputs.customName))
    }

    private fun dispose() {
        BaseModule.settings.set(SETTING_TYPE, (typeField.selectedItem as FindUnconnectedPortsType).customName)
    }

    private fun buildUI() {
        val inset = 5
        var row = 0
        layout = EGBL.getLayout()

        // Type

        EGBL.add(
            this,
            typeLabel,
            0, row,
            1, 1,
            0.0, 0.0,
            EGBL.WEST,
            EGBL.NONE,
            0, inset, 0, 0
        )

        EGBL.add(
            this,
            typeField,
            1, row++,
            EGBL.REMAINDER, 1,
            0.0, 0.0,
            EGBL.WEST,
            EGBL.HORIZONTAL,
            0, 10, 0, inset
        )

        // Filler

        EGBL.add(
            this,
            JPanel(),
            2, row,
            EGBL.REMAINDER, EGBL.REMAINDER,
            1.0, 1.0,
            EGBL.NORTHWEST,
            EGBL.BOTH
        )
    }
}