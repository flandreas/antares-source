package ch.scorpion.jabbah.edit

import com.l2fprod.common.beans.editor.AbstractPropertyEditor
import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.ActionWrapperSwing
import ch.scorpion.jabbah.edit.model.text.TextProperty
import java.awt.*
import java.awt.Component
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.beans.PropertyEditor
import javax.swing.*
import javax.swing.table.TableCellRenderer

class TextPropertyRenderer: TableCellRenderer {

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

/** A factory for creating an editor for [TextProperty] to be used with [DynamicPropertyEditorRegistry].*/
class TextPropertyEditorFactory : (PropertyImpl<*>) -> PropertyEditor {

    override fun invoke(p1: PropertyImpl<*>): PropertyEditor {
        return TextPropertyEditor(p1)
    }
}

/**
 * An editor that provides more space for editing a [TextProperty] by using a multi-row [JTextArea].
 * Additionally contains a button to open a non-modal dialog that provides even more space.
 */
class TextPropertyEditor(private val property: PropertyImpl<*>) : AbstractPropertyEditor() {

    companion object {
        // Holds the single [JDialog] instance across all [TextPropertyEditor] instances.
        private var dialog: JDialog? = null
    }

    private val textArea: JTextArea = JTextArea()
    private val button = JButton()

    init {
        textArea.rows = 4
        textArea.lineWrap = true
        textArea.isEditable = true

        buildUI()
    }

    override fun getValue(): Any {
        return TextProperty(textArea.text)
    }

    override fun setValue(value: Any?) {
        textArea.text = (value as TextProperty).text
    }

    private fun buildUI() {
        val panel = JPanel()
        panel.background = Color.WHITE
        panel.layout = BoxLayout(panel, BoxLayout.LINE_AXIS)

        val scrollPane = JScrollPane(textArea)
        scrollPane.alignmentY = Component.TOP_ALIGNMENT
        scrollPane.horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        scrollPane.verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
        panel.add(scrollPane)

        button.isEnabled = dialog == null
        button.alignmentY = Component.TOP_ALIGNMENT
        button.icon = ImageIcon(TextPropertyEditor::class.java.getResource("/img/openInPopup-20.png"))
        button.border = BorderFactory.createEmptyBorder(0, 0, 0, 0)
        // TODO I18N
        button.toolTipText = "Edit text in larger popup dialog"
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
        dialog!!.title = "Property: ${property.displayName}"
        dialog!!.contentPane.add(DialogContentPanel(property, {
            dialog!!.dispose()
        }))
        dialog!!.pack()
        dialog!!.setLocationRelativeTo(frame)
        dialog!!.isVisible = true

        button.isEnabled = false
    }

    private class DialogContentPanel(
            private val property: PropertyImpl<*>,
            private val closeHandler: () -> Unit
    ) : JPanel() {

        private val textArea = JTextArea((property.value as TextProperty).text)

        init {
            layout = BorderLayout()

            textArea.wrapStyleWord = true
            textArea.lineWrap = true

            val scrollPane = JScrollPane()
            scrollPane.setViewportView(textArea)
            scrollPane.horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
            scrollPane.verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
            scrollPane.preferredSize = Dimension(400, 500)
            add(scrollPane, BorderLayout.CENTER)

            val buttonPanel = JPanel(FlowLayout(FlowLayout.CENTER))
            buttonPanel.add(JButton(ActionWrapperSwing(object : AbstractAction("edit.action.ok") {
                override fun execute(event: ch.scorpion.jabbah.base.event.ActionEvent) {
                    property.value = TextProperty(textArea.text)
                    property.writeToBean()
                    closeHandler.invoke()
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