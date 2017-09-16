package ch.scorpion.antares.view.memory

import ch.scorpion.antares.model.memory.Memory
import ch.scorpion.antares.model.memory.MemoryDump
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.edit.CommandManager
import ch.scorpion.jabbah.base.logger
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.event.ActionEvent
import java.nio.file.Files
import java.nio.file.Paths
import javax.swing.AbstractAction
import javax.swing.JButton
import javax.swing.JFileChooser
import javax.swing.JPanel


/**
 * Displays the contents of a [Memory].
 */
class MemoryContentsPanel(
    private val memory: Memory,
    private val addressWidth: BitWidth,
    private val dataWidth: BitWidth,
    private val cmdManager: CommandManager
) : JPanel() {

    private val LOG by logger(MemoryContentsPanel::class)

    init {
        buildUI()
    }

    private fun buildUI() {
        layout = BorderLayout()

        val contentsView = JPanel(BorderLayout())
        contentsView.preferredSize = Dimension(300, 500)
        contentsView.add(MemoryDisplayPanel(memory, addressWidth, dataWidth))
        add(contentsView, BorderLayout.CENTER)

        val buttonPanel = JPanel(FlowLayout(FlowLayout.LEFT))
        buttonPanel.add(JButton(LoadAction()))
        buttonPanel.add(JButton(SaveAction()))
        add(buttonPanel, BorderLayout.SOUTH)
    }

    private inner class LoadAction : AbstractAction(Translations.getString("antares.action.memory.load.name")) {
        override fun actionPerformed(e: ActionEvent?) {
            val fileChooser = JFileChooser()
            if (fileChooser.showOpenDialog(this@MemoryContentsPanel) == JFileChooser.APPROVE_OPTION) {
                cmdManager.execute(MemoryContentsCommand(memory, dataWidth, fileChooser.selectedFile!!.absolutePath))
            }
        }
    }

    private inner class SaveAction : AbstractAction(Translations.getString("antares.action.memory.save.name")) {
        override fun actionPerformed(e: ActionEvent?) {
            val fileChooser = JFileChooser()
            if (fileChooser.showSaveDialog(this@MemoryContentsPanel) == JFileChooser.APPROVE_OPTION) {
                val contents = MemoryDump.write(memory, dataWidth)
                try {
                    Files.write(Paths.get(fileChooser.selectedFile.absolutePath), contents.toByteArray())
                } catch (e: Throwable) {
                    LOG.error("Errow while saving memory to '${fileChooser.selectedFile.absolutePath}'")
                }
            }
        }
    }
}