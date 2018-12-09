package ch.scorpion.jabbah.edit

import com.l2fprod.common.beans.editor.AbstractPropertyEditor
import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.ActionWrapperSwing
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.edit.model.text.TextProperty
import java.awt.*
import java.awt.Component
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.*
import javax.swing.table.TableCellRenderer

class TextPropertyRenderer: TableCellRenderer {

	companion object {
		private val LOG by logger(TextPropertyRenderer::class)
	}

    private val textArea: JTextArea = JTextArea()

    init {
        textArea.rows = 4
        textArea.lineWrap = true
        textArea.isEditable = false
    }

    override fun getTableCellRendererComponent(table: JTable?, value: Any?, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int): Component {
        if (value is String) {
			textArea.text = value
		} else if (value is TextProperty) {
			textArea.text = value.text
		}
		return textArea
    }
}

/**
 * An editor that provides more space for editing a [TextProperty] by using a multi-row [JTextArea].
 * Additionally contains a button to open a non-modal dialog that provides even more space.
 */
class TextPropertyEditor() : AbstractPropertyEditor() {

    companion object {
	    private val LOG by logger(TextPropertyEditor::class)

        // Holds the single [JDialog] instance across all [TextPropertyEditor] instances.
        private var dialog: JDialog? = null
    }

    private val editorTextArea: JTextArea = JTextArea()
    private val button = JButton()

    init {
        editorTextArea.rows = 4
        editorTextArea.lineWrap = true
        editorTextArea.isEditable = true

        buildUI()
    }

    override fun getValue(): Any {
	    LOG.debug("TextProperty: get value ${editorTextArea.text}")
        return TextProperty(editorTextArea.text)
    }

    override fun setValue(value: Any?) {
	    LOG.debug("TextProperty: set value $value")
        editorTextArea.text = (value as TextProperty).text
    }

    private fun buildUI() {
        val panel = JPanel()
        panel.background = Color.WHITE
        panel.layout = BoxLayout(panel, BoxLayout.LINE_AXIS)

        val scrollPane = JScrollPane(editorTextArea)
        scrollPane.alignmentY = Component.TOP_ALIGNMENT
        scrollPane.horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        scrollPane.verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
        panel.add(scrollPane)

        button.isEnabled = dialog == null
        button.alignmentY = Component.TOP_ALIGNMENT
        button.icon = ImageIcon(TextPropertyEditor::class.java.getResource("/img/openInPopup-20.png"))
        button.border = BorderFactory.createEmptyBorder(0, 0, 0, 0)
        button.toolTipText = Translations.getString("edit.action.editText.tooltip")
        button.addActionListener { showDialog() }
        panel.add(button)

        editor = panel
    }

    private fun showDialog() {
        val frame = SwingUtilities.getWindowAncestor(editor) as JFrame
        dialog = JDialog(frame, false)
        dialog!!.addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent?) {
                dialog = null
                button.isEnabled = true
            }

            override fun windowClosed(e: WindowEvent?) {
                dialog = null
                button.isEnabled = true
            }
        })
        dialog!!.title = "Property"
        dialog!!.contentPane.add(DialogContentPanel() {
	        dialog!!.dispose()
        })
        dialog!!.pack()
        dialog!!.setLocationRelativeTo(frame)
        dialog!!.isVisible = true

        button.isEnabled = false
    }

    private inner class DialogContentPanel(
            private val closeHandler: () -> Unit
    ) : JPanel() {

        private val dialogTextArea = JTextArea(editorTextArea.text)

        init {
            layout = BorderLayout()

            dialogTextArea.wrapStyleWord = true
            dialogTextArea.lineWrap = true

            val scrollPane = JScrollPane()
            scrollPane.setViewportView(dialogTextArea)
            scrollPane.horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
            scrollPane.verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
            scrollPane.preferredSize = Dimension(400, 500)
            add(scrollPane, BorderLayout.CENTER)

            val buttonPanel = JPanel(FlowLayout(FlowLayout.CENTER))
            buttonPanel.add(JButton(ActionWrapperSwing(object : AbstractAction("edit.action.ok") {
                override fun execute(event: ch.scorpion.jabbah.base.event.ActionEvent) {
	                editorTextArea.text = dialogTextArea.text
                    closeHandler.invoke()
	                editorTextArea.requestFocus()
                }
            })))
            buttonPanel.add(JButton(ActionWrapperSwing(object : AbstractAction("edit.action.cancel") {
                override fun execute(event: ch.scorpion.jabbah.base.event.ActionEvent) {
                    closeHandler.invoke()
                }
            })))
            add(buttonPanel, BorderLayout.SOUTH)
        }
    }
}