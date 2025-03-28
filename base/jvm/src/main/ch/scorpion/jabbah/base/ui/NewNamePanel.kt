package ch.scorpion.jabbah.base.ui

import ch.scorpion.jabbah.base.*
import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.swing.DialogBuilder
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Frame
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

/**
 * A [JPanel] for entering the name of a new object.
 */
class NewNamePanel(
    private val closeHandler: () -> Unit
) : JPanel() {

    companion object {

        /**
         * Display the [NewNamePanel] as a modal dialog with OK and Cancel button.
         * @return the entered name, or `null` if the user cancelled the dialog
         */
        fun showAsDialog(
            title: String,
            parent: Frame = Frame.getFrames()[0]
        ): String? {
            val builder = DialogBuilder<NewNamePanel>(parent)
                .content { dialog -> NewNamePanel { dialog.dispose() } }
                .title(title)
                .defaultButton { it.okButton }
                .nonResizable()
                .show()

            return builder.content.result
        }
    }

    private val okAction = OkAction()
    private val okButton = createButton(okAction)
    private val cancelAction = CancelAction()

    private val nameLabel = JLabel(Translations.getString("base.element.name.name"))
    private val nameField = JTextField(20)

    private var result: String? = null

    init {
        buildUI()
        updateOkAction()

        nameField.document.addDocumentListener(object : DocumentListener {
            override fun insertUpdate(e: DocumentEvent?) { updateOkAction() }
            override fun removeUpdate(e: DocumentEvent?) { updateOkAction() }
            override fun changedUpdate(e: DocumentEvent?) { updateOkAction() }
        })
    }

    private fun updateOkAction() {
        okAction.enabled = StringUtils.isNotBlank(nameField.text)
    }

    private fun buildUI() {
        layout = BorderLayout(10, 10)
        border = UIBasics.createDialogBorder()

        val contentPanel = JPanel()
        contentPanel.layout = BoxLayout(contentPanel, BoxLayout.PAGE_AXIS)

        nameLabel.horizontalAlignment = SwingConstants.RIGHT
        nameLabel.alignmentX = Component.LEFT_ALIGNMENT
        nameField.maximumSize = nameField.preferredSize
        nameField.alignmentX = Component.LEFT_ALIGNMENT
        contentPanel.add(nameLabel)
        contentPanel.add(Box.createVerticalStrut(2))
        contentPanel.add(nameField)
        contentPanel.add(Box.createVerticalStrut(12))

        val buttonPanel = JPanel()
        buttonPanel.layout = BoxLayout(buttonPanel, BoxLayout.LINE_AXIS)
        buttonPanel.add(Box.createHorizontalGlue())
        UIBasics.addButtons(buttonPanel, okButton, createButton(cancelAction))

        val iconPanel = JPanel()
        iconPanel.layout = BoxLayout(iconPanel, BoxLayout.PAGE_AXIS)
        iconPanel.add(JLabel(UIManager.getIcon("OptionPane.questionIcon")), BorderLayout.WEST)
        iconPanel.add(Box.createGlue())

        add(iconPanel, BorderLayout.WEST)
        add(contentPanel, BorderLayout.CENTER)
        add(buttonPanel, BorderLayout.SOUTH)
    }

    private fun createButton(action: Action): JButton = JButton(ActionWrapperSwing(action))

    private inner class OkAction : AbstractAction("base.action.ok") {
        override fun execute(event: ActionEvent) {
            result = nameField.text
            closeHandler()
        }
    }

    private inner class CancelAction : AbstractAction("base.action.cancel") {
        override fun execute(event: ActionEvent) {
            result = null
            closeHandler()
        }
    }
}