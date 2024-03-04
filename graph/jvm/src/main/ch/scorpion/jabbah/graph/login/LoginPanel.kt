package ch.scorpion.jabbah.graph.login

import ch.scorpion.jabbah.base.*
import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.module.BaseModule
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
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

/**
 * Requests username/password from user in order to login to remote Akrab server.
 */
class LoginPanel(
    private val dialogCloser: () -> Unit
) : JPanel() {

    companion object {

        /** The name of the boolean setting in [Settings] to store whether the username is to be remembered.*/
        private const val SETTINGS_REMEMBER_USERNAME = "jabbah.graph.login.rememberUsername"

        /** The name of the string setting in [Settings] to store the user name (if "Remember username" is selected).*/
        private const val SETTING_USER_NAME = "jabbah.graph.login.username"

        fun showAsDialog() {
            DialogBuilder<LoginPanel>(Frame.getFrames()[0])
                .content { dialog -> LoginPanel { dialog.dispose() } }
                .defaultButton { it.loginButton }
                .title(Translations.getString("graph.action.login.name"))
                .nonResizable()
                .show()
        }
    }

    private val introLabel = JLabel(Translations.getOptionalString("graph.action.login.intro") ?: "")
    private val usernameField = JTextField()
    private val passwordField = JPasswordField()
    private val messageLabel = JLabel(" ")
    private val rememberUsernameCheckbox = JCheckBox(Translations.getString("graph.action.login.rememberUsername"))

    private val loginAction = LoginAction()
    private val loginButton = JButton(ActionWrapperSwing(loginAction))
    private val cancelAction = CancelAction()
    private val cancelButton = JButton(ActionWrapperSwing(cancelAction))

    private val textFieldListener = object : DocumentListener {
        override fun insertUpdate(e: DocumentEvent?) { updateLoginAction() }
        override fun removeUpdate(e: DocumentEvent?) { updateLoginAction() }
        override fun changedUpdate(e: DocumentEvent?) { updateLoginAction() }
    }

    init {
        UiUtil.selectAllOnFocusGained(usernameField)
        UiUtil.selectAllOnFocusGained(passwordField)
        buildUI()

        if (BaseModule.settings.getBoolean(SETTINGS_REMEMBER_USERNAME, false)) {
            rememberUsernameCheckbox.isSelected = true
            usernameField.text = BaseModule.settings.getString(SETTING_USER_NAME, "")
        }

        if (StringUtils.isNotEmpty(usernameField.text)) {
            System.invokeLater { passwordField.requestFocusInWindow() }
        }

        usernameField.document.addDocumentListener(textFieldListener)
        passwordField.document.addDocumentListener(textFieldListener)

        updateLoginAction()
    }

    private fun updateLoginAction() {
        loginAction.enabled = StringUtils.isNotEmpty(usernameField.text) && StringUtils.isNotEmpty(passwordField.text)
    }

    private fun buildUI() {
        layout = BorderLayout(10, 20)
        border = UIBasics.createDialogBorder()
        add(introLabel, BorderLayout.NORTH)
        add(buildContentPanel(), BorderLayout.CENTER)
        add(buildButtonPanel(), BorderLayout.SOUTH)
    }

    private fun buildContentPanel(): JPanel {
        val panel = JPanel(BorderLayout(0, 0))
        val fieldsPanel = buildFieldsPanel()
        panel.add(fieldsPanel, BorderLayout.NORTH)
        messageLabel.border = BorderFactory.createEmptyBorder(0, fieldsPanel.leftInset, 0, 0)
        messageLabel.foreground = UiUtil.errorTextColor
        panel.add(messageLabel, BorderLayout.CENTER)
        return panel
    }

    private fun buildFieldsPanel(): DataFormPanel {
        val form = DataFormPanel()

        usernameField.preferredSize = Dimension(200, usernameField.preferredSize.height)
        passwordField.preferredSize = Dimension(200, passwordField.preferredSize.height)

        form.addLabeledRow(Translations.getString("graph.action.login.username"), usernameField)
        form.addLabeledRow(Translations.getString("graph.action.login.password"), passwordField)
        form.addRow(rememberUsernameCheckbox)
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

    private fun closeDialogOnSuccess() {
        if (rememberUsernameCheckbox.isSelected) {
            BaseModule.settings.set(SETTING_USER_NAME, usernameField.text)
        }
        BaseModule.settings.set(SETTINGS_REMEMBER_USERNAME, rememberUsernameCheckbox.isSelected)
        dialogCloser()
    }

    private inner class LoginAction : AbstractAction("graph.action.login") {
        override fun execute(event: ActionEvent) {
            runBlocking {
                if (!GraphModuleJvm.loginService.login(LoginRequest(usernameField.text, String(passwordField.password)))) {
                    messageLabel.text = Translations.getString("graph.action.login.unsuccessful.text")
                    usernameField.requestFocusInWindow()
                } else {
                    closeDialogOnSuccess()
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