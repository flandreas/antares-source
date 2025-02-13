package ch.scorpion.jabbah.edit.app

import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.ActionWrapperSwing
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.swing.DialogBuilder
import ch.scorpion.jabbah.base.ui.Clipboard
import ch.scorpion.jabbah.base.ui.UIBasics
import ch.scorpion.jabbah.edit.Component
import java.awt.BorderLayout
import java.awt.Frame
import javax.swing.*

/**
 * Displays the IDs of all selected [Component]s as a comma-separated list
 * in a dialog.
 */
class DisplayIdsAction(
    eventBus: EventBus = BaseModule.eventBus,
    private val filter: (Component) -> Boolean = { true },
) : AbstractSelectionAwareAction("edit.action.displayIds", eventBus) {

    override val opensDialog: Boolean get() = true

    override fun execute(event: ActionEvent) {
        DisplayIdsPanel.showAsDialog(
            selection
                .filter { filter.invoke(it) }
                .map { it.id }
                .sorted()
                .joinToString(",")
        )
    }
}

internal class DisplayIdsPanel(
    idList: String,
    private val closeHandler: () -> Unit
) : JPanel() {

    companion object {
        fun showAsDialog(
            idList: String,
            parent: Frame = Frame.getFrames()[0]
        ) {
            DialogBuilder<DisplayIdsPanel>(parent)
                .title(Translations.getString("edit.action.displayIds.title"))
                .content { dialog -> DisplayIdsPanel(idList) { dialog.dispose() } }
                .defaultButton { it.closeButton }
                .nonResizable()
                .show()
        }
    }

    private val textArea = JTextArea()
    private val closeAction = CloseAction()
    private val copyToClipboardAction = CopyToClipboardAction()
    private val closeButton = JButton(ActionWrapperSwing(closeAction))

    init {
        buildUI(idList)
    }

    private fun buildUI(idList: String) {
        layout = BorderLayout(10, 10)
        border = UIBasics.createDialogBorder()
        add(createContentComponent(idList), BorderLayout.CENTER)
        add(createButtonPanel(), BorderLayout.SOUTH)
    }

    private fun createContentComponent(idList: String): JComponent {
        textArea.isEditable = false
        textArea.lineWrap = true
        textArea.text = idList
        textArea.columns = 30
        textArea.rows = 4
        val scrollPane = JScrollPane(textArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER)
        return scrollPane
    }

    private fun createButtonPanel(): JPanel {
        val buttonPanel = JPanel()
        buttonPanel.layout = BoxLayout(buttonPanel, BoxLayout.LINE_AXIS)
        buttonPanel.add(JButton(ActionWrapperSwing(copyToClipboardAction)))
        buttonPanel.add(Box.createHorizontalGlue())
        buttonPanel.add(closeButton)
        return buttonPanel
    }

    private inner class CloseAction : AbstractAction("file.action.close") {
        override fun execute(event: ActionEvent) {
            closeHandler.invoke()
        }
    }

    private inner class CopyToClipboardAction : AbstractAction("base.action.copyToClipboard") {
        override fun execute(event: ActionEvent) {
            Clipboard.setStringContents(textArea.text)
            closeHandler.invoke()
        }
    }
}