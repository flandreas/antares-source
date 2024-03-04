package ch.scorpion.jabbah.graph.login

import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.ActionWrapperSwing
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.swing.DataFormPanel
import ch.scorpion.jabbah.base.swing.DialogBuilder
import ch.scorpion.jabbah.base.swing.UiUtil
import ch.scorpion.jabbah.base.ui.UIBasics
import ch.scorpion.jabbah.graph.module.GraphModuleJvm
import kotlinx.coroutines.runBlocking
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Frame
import javax.swing.*

/**
 * Requests username/password from user in order to login to remote Akrab server.
 */
class LoginPanel(
    private val dialogCloser: () -> Unit
) : JPanel() {

    companion object {
        fun showAsDialog() {
            DialogBuilder<LoginPanel>(Frame.getFrames()[0])
                .content { dialog -> LoginPanel { dialog.dispose() } }
                .defaultButton { it.loginButton }
                .title(Translations.getString("graph.action.login.name"))
                .nonResizable()
                .show()
        }
    }

    private val usernameField = JTextField()
    private val passwordField = JPasswordField()
    private val messageLabel = JLabel(" ")

    private val loginAction = LoginAction()
    private val loginButton = JButton(ActionWrapperSwing(loginAction))
    private val cancelAction = CancelAction()
    private val cancelButton = JButton(ActionWrapperSwing(cancelAction))

    init {
        UiUtil.selectAllOnFocusGained(usernameField)
        UiUtil.selectAllOnFocusGained(passwordField)
        buildUI()
    }

    private fun buildUI() {
        layout = BorderLayout(10, 30)
        border = UIBasics.createDialogBorder()
        add(buildContentPanel(), BorderLayout.CENTER)
        add(buildButtonPanel(), BorderLayout.SOUTH)
    }

    private fun buildContentPanel(): JPanel {
        val panel = JPanel(BorderLayout(0, 0))
        panel.add(buildFieldsPanel(), BorderLayout.CENTER)
        messageLabel.border = BorderFactory.createEmptyBorder(0, DataFormPanel.INSET, 0, 0)
        messageLabel.foreground = UiUtil.errorTextColor
        panel.add(messageLabel, BorderLayout.SOUTH)
        return panel
    }

    private fun buildFieldsPanel(): JPanel {
        val form = DataFormPanel()

        usernameField.preferredSize = Dimension(200, usernameField.preferredSize.height)
        passwordField.preferredSize = Dimension(200, passwordField.preferredSize.height)

        form.addLabeledRow(Translations.getString("graph.action.login.username"), usernameField)
        form.addLabeledRow(Translations.getString("graph.action.login.password"), passwordField)
        form.addFiller()

        return form
    }

    private fun buildButtonPanel(): JPanel {
        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.LINE_AXIS)
        panel.add(Box.createHorizontalGlue())
        UIBasics.addButtons(panel, loginButton, cancelButton)
        return panel
    }

    private inner class LoginAction : AbstractAction("graph.action.login") {
        override fun execute(event: ActionEvent) {
            runBlocking {
                if (!GraphModuleJvm.loginService.login(LoginRequest(usernameField.text, String(passwordField.password)))) {
                    messageLabel.text = Translations.getString("graph.action.login.unsuccessful.text")
                    usernameField.requestFocusInWindow()
                } else {
                    dialogCloser()
                    JOptionPane.showMessageDialog(
                        Frame.getFrames()[0],
                        Translations.getString("graph.action.login.successful.text"),
                        this@LoginAction.name,
                        JOptionPane.INFORMATION_MESSAGE)
                }
            }
        }
    }

    private inner class CancelAction : AbstractAction("base.action.cancel") {
        override fun execute(event: ActionEvent) {
            dialogCloser()
        }
    }
}