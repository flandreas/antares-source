package ch.scorpion.antares.view.addressable

import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.ActionWrapperSwing
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.swing.DialogBuilder
import ch.scorpion.jabbah.base.ui.UIBasics
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Dimension
import java.awt.Frame
import javax.swing.*

class NewMemoryStorablePanel(
    private val closeHandler: () -> Unit
) : JPanel() {

    companion object {

        fun showAsDialog(parent: Frame): MemoryStorable? {
            val builder = DialogBuilder<NewMemoryStorablePanel>(parent)
                .content { dialog -> NewMemoryStorablePanel(closeHandler = { dialog.dispose() }) }
                .title(Translations.getString("library.newMemoryStorable.title"))
                .defaultButton { it.okButton }
                .preferredSize(Dimension(300, 200))
                .nonResizable()
                .show()

            return builder.content.result
        }
    }

    private val okAction = OkAction()
    private val okButton = createButton(okAction)
    private val cancelAction = CancelAction()

    private val nameLabel = JLabel(Translations.getString("library.newMemoryStorable.name"))
    private val bitWidthLabel = JLabel(Translations.getString("library.newMemoryStorable.bitWidth"))

    private val nameField = JTextField(20)

    private val errorLabel = JLabel(" ")

    private var result: MemoryStorable? = null

    init {
        buildUI()
    }

    private fun buildUI() {
        layout = BorderLayout(10, 10)
        border = UIBasics.createDialogBorder()
        add(createContentPanel(), BorderLayout.CENTER)
        add(createButtonPanel(), BorderLayout.SOUTH)
    }

    private fun createContentPanel(): JPanel {
        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.PAGE_AXIS)

        nameLabel.horizontalAlignment = SwingConstants.RIGHT
        nameLabel.alignmentX = Component.LEFT_ALIGNMENT
        nameField.maximumSize = nameField.preferredSize
        nameField.alignmentX = Component.LEFT_ALIGNMENT
        panel.add(nameLabel)
        panel.add(Box.createVerticalStrut(2))
        panel.add(nameField)
        panel.add(Box.createVerticalStrut(12))

        return panel
    }

    private fun createButtonPanel(): JPanel {
        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.LINE_AXIS)
        panel.add(Box.createHorizontalGlue())
        UIBasics.addButtons(panel, okButton, createButton(cancelAction))
        return panel
    }

    private fun createButton(action: Action): JButton = JButton(ActionWrapperSwing(action))

    private fun validateInput(): MemoryStorable {
        if (nameField.text.isBlank()) {
            // TODO I18N
            throw IllegalArgumentException("Name must not be empty")
        }
        return MemoryStorable(nameField.text)
    }

    private inner class OkAction : AbstractAction("base.action.ok") {
        override fun execute(event: ActionEvent) {
            try {
                result = validateInput()
                closeHandler()
            } catch (e: Exception) {
                errorLabel.text = e.message
            }
        }
    }

    private inner class CancelAction : AbstractAction("base.action.cancel") {
        override fun execute(event: ActionEvent) {
            result = null
            closeHandler()
        }
    }
}