package ch.scorpion.jabbah.base.auth0

import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.ActionWrapperSwing
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.invocation.InvocationHandler
import ch.scorpion.jabbah.base.swing.DialogBuilder
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Frame
import javax.swing.*

class LoginAction : AbstractAction("base.action.login") {

	override fun execute(event: ActionEvent) {
		Auth0LoginView.showAsDialog()
	}
}

class Auth0LoginView(
	private val closeHandler: () -> Unit
) : JPanel() {

	companion object {
		private const val INSET = 10

		fun showAsDialog() {
			DialogBuilder<Auth0LoginView>(Frame.getFrames()[0])
				.content { dialog -> Auth0LoginView { dialog.dispose() } }
				.title(Translations.getString("base.action.login.name"))
				.defaultButton { it.continueButton }
				.preferredSize(Dimension(300, 200))
				.nonResizable()
				.show()
		}
	}

	private val textArea = JTextArea()
	private val continueAction = ContinueAction()
	private val cancelAction = CancelAction()
	private val continueButton = JButton(ActionWrapperSwing(continueAction))

	// TODO: Extract parameters to property file
	private val loginParams = LoginParams(
		domain = "dev-wq7i977v.eu.auth0.com",
		clientId = "mYdmErbSZxQUtlr9BW2UHUOmxtHN8WNO",
		redirectUrl = "http://127.0.0.1:8899/desktop"
	)

	private val flow = Auth0LoginFlow(loginParams, ::handleStateChanged)

	init {
		buildUI()
		updateText()
	}

	private fun buildUI() {
		layout = BorderLayout(10, 10)
		border = BorderFactory.createEmptyBorder(INSET, INSET, INSET, INSET)

		textArea.isEditable = false
		textArea.rows = 5
		add(textArea, BorderLayout.CENTER)

		val buttonPanel = JPanel()
		//buttonPanel.border = BorderFactory.createEmptyBorder(10, 10, 0, 10)
		buttonPanel.layout = BoxLayout(buttonPanel, BoxLayout.LINE_AXIS)

		buttonPanel.add(Box.createHorizontalGlue())
		buttonPanel.add(JButton(ActionWrapperSwing(cancelAction)))
		buttonPanel.add(Box.createHorizontalStrut(2))
		buttonPanel.add(continueButton)
		add(buttonPanel, BorderLayout.SOUTH)
	}

	private fun handleStateChanged() {
		updateText()
		continueAction.enabled = when (flow.state) {
			FlowState.Welcome -> true
			FlowState.Initializing -> false
			FlowState.Initialized -> true
			FlowState.EnterCredentials -> true
			FlowState.Done -> true
		}
	}

	private fun updateText() {
		textArea.text = when (flow.state) {
			FlowState.Welcome -> "Welcome"
			FlowState.Initializing -> "Initializing"
			FlowState.Initialized -> "Initialized"
			FlowState.EnterCredentials -> "Enter Credentials"
			FlowState.Done -> "Done"
		}
	}

	private fun stop() {
		flow.stop()
		closeHandler()
	}

	private inner class ContinueAction : AbstractAction("base.action.continue") {
		override fun execute(event: ActionEvent) {
			InvocationHandler.invoke {
				if (flow.state == FlowState.Done) {
					stop()
				} else {
					flow.nextState()
				}
			}
		}
	}

	private inner class CancelAction : AbstractAction("base.action.cancel") {
		override fun execute(event: ActionEvent) {
			stop()
		}
	}
}