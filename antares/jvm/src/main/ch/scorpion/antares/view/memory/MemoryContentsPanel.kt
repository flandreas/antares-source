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
    private val cmdManager: CommandManager,
    private val closeHandler: () -> Unit
) : JPanel() {

	companion object {
        private val LOG by logger(MemoryContentsPanel::class)
		private const val PREF_WIDTH = 800
		private const val PREF_HEIGHT = 500
	}

	private val memoryDisplayPanel = MemoryDisplayPanel(memory, addressWidth, dataWidth)

    init {
        buildUI()
    }

    private fun buildUI() {
        layout = BorderLayout()

        val contentsView = JPanel(BorderLayout())
        contentsView.preferredSize = Dimension(PREF_WIDTH, PREF_HEIGHT)
        contentsView.add(memoryDisplayPanel)
        add(contentsView, BorderLayout.CENTER)

        val buttonPanel = JPanel(FlowLayout(FlowLayout.LEFT))
	    buttonPanel.add(JButton(CloseAction()))
        buttonPanel.add(JButton(ImportAction()))
        buttonPanel.add(JButton(ExportAction()))
	    buttonPanel.add(JButton(ClearAction()))
        add(buttonPanel, BorderLayout.SOUTH)
    }

	private inner class CloseAction : AbstractAction(Translations.getString("file.action.close.name")) {
		override fun actionPerformed(e: ActionEvent?) {
			closeHandler.invoke()
		}
	}

    private inner class ImportAction : AbstractAction(Translations.getString("antares.action.memory.import.name")) {
        override fun actionPerformed(e: ActionEvent?) {
            val fileChooser = JFileChooser()
            if (fileChooser.showOpenDialog(this@MemoryContentsPanel) == JFileChooser.APPROVE_OPTION) {
                cmdManager.execute(MemoryContentsCommand(memory, dataWidth, fileChooser.selectedFile!!.absolutePath))
	            memoryDisplayPanel.refresh()
            }
        }
    }

	private inner class ClearAction : AbstractAction(Translations.getString("antares.action.memory.clear.name")) {
		override fun actionPerformed(e: ActionEvent?) {
			cmdManager.execute(MemoryClearCommand(memory, dataWidth))
			memoryDisplayPanel.refresh()
		}
	}

    private inner class ExportAction : AbstractAction(Translations.getString("antares.action.memory.export.name")) {
        override fun actionPerformed(e: ActionEvent?) {
            val fileChooser = JFileChooser()
            if (fileChooser.showSaveDialog(this@MemoryContentsPanel) == JFileChooser.APPROVE_OPTION) {
                val contents = MemoryDump.write(memory, dataWidth)
                try {
                    Files.write(Paths.get(fileChooser.selectedFile.absolutePath), contents.toByteArray())
                } catch (e: Throwable) {
                    LOG.error("Error while exporting memory to '${fileChooser.selectedFile.absolutePath}'")
                }
            }
        }
    }
}