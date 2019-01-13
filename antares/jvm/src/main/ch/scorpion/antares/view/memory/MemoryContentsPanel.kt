package ch.scorpion.antares.view.memory

import ch.scorpion.antares.model.memory.Addressable
import ch.scorpion.antares.model.memory.Memory
import ch.scorpion.antares.model.memory.MemoryDump
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.edit.CommandManager
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Frame
import java.awt.event.ActionEvent
import java.nio.file.Files
import java.nio.file.Paths
import javax.swing.*


/**
 * Displays the contents of a [Memory].
 */
class MemoryContentsPanel(
	private val memory: Memory,
	private val addressable: Addressable,
	private val cmdManager: CommandManager,
	readonly: Boolean = false,
	private val closeHandler: () -> Unit
) : JPanel() {

	companion object {
        private val LOG by logger(MemoryContentsPanel::class)
		private const val PREF_WIDTH = 800
		private const val PREF_HEIGHT = 500

		fun showAsDialog(
			parent: Frame = Frame.getFrames()[0],
			name: String,
			memory: Memory,
			addressable: Addressable,
			cmdManager: CommandManager,
			readonly: Boolean
		) {
			val dialog = JDialog(parent, true)
			val contentsPanel = MemoryContentsPanel(memory, addressable, cmdManager, readonly) {
				dialog.isVisible = false
			}
			dialog.title = Translations.getString("antares.action.memory.contents.title", name)
			dialog.contentPane.add(contentsPanel)
			dialog.pack()
			dialog.setLocationRelativeTo(parent)
			dialog.isVisible = true
		}
	}

	private val memoryDisplayPanel = MemoryDisplayPanel(addressable)

    init {
        buildUI(readonly)
    }

    private fun buildUI(readonly: Boolean) {
        layout = BorderLayout()

        val contentsView = JPanel(BorderLayout())
        contentsView.preferredSize = Dimension(PREF_WIDTH, PREF_HEIGHT)
        contentsView.add(memoryDisplayPanel)
        add(contentsView, BorderLayout.CENTER)

	    val buttonPanel = JPanel()
	    buttonPanel.border = BorderFactory.createEmptyBorder(10, 10, 10, 10)
	    buttonPanel.layout = BoxLayout(buttonPanel, BoxLayout.LINE_AXIS)
	    buttonPanel.add(Box.createHorizontalGlue())

	    if (!readonly) {
		    buttonPanel.add(JButton(ImportAction()))
	    }
        buttonPanel.add(JButton(ExportAction()))
	    if (!readonly) {
		    buttonPanel.add(JButton(ClearAction()))
	    }
	    buttonPanel.add(JButton(CloseAction()))

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
                cmdManager.execute(MemoryContentsCommand(memory, addressable.dataWidth, fileChooser.selectedFile!!.absolutePath))
	            memoryDisplayPanel.refresh()
            }
        }
    }

	private inner class ClearAction : AbstractAction(Translations.getString("antares.action.memory.clear.name")) {
		override fun actionPerformed(e: ActionEvent?) {
			cmdManager.execute(MemoryClearCommand(memory, addressable.dataWidth))
			memoryDisplayPanel.refresh()
		}
	}

    private inner class ExportAction : AbstractAction(Translations.getString("antares.action.memory.export.name")) {
        override fun actionPerformed(e: ActionEvent?) {
            val fileChooser = JFileChooser()
            if (fileChooser.showSaveDialog(this@MemoryContentsPanel) == JFileChooser.APPROVE_OPTION) {
                val contents = MemoryDump.write(memory, addressable.dataWidth)
                try {
                    Files.write(Paths.get(fileChooser.selectedFile.absolutePath), contents.toByteArray())
                } catch (e: Throwable) {
                    LOG.error("Error while exporting memory to '${fileChooser.selectedFile.absolutePath}'")
                }
            }
        }
    }
}