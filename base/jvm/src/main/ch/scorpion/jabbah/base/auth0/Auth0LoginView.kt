package ch.scorpion.jabbah.base.auth0

import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.ActionWrapperSwing
import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.invocation.InvocationHandler
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.swing.DialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Frame
import javax.swing.*

/**
 * Opens a [Auth0LoginView] and starts a [Auth0LoginFlow].
 */
class LoginLogoutAction : AbstractAction("base.action.login") {

	private val auth0SessionListener: (Auth0SessionEvent) -> Unit = { update() }

	init {
		BaseModule.eventBus.register(Auth0SessionEvent::class, auth0SessionListener)
		update()
	}

	override fun dispose() {
		super.dispose()
		BaseModule.eventBus.unregister(auth0SessionListener)
	}

	override fun execute(event: ActionEvent) {
		if (Auth0Session.exists) {
			Auth0Session.drop()
		} else {
			Auth0LoginView.showAsDialog()
		}
	}

	private fun update() {
		name = if (Auth0Session.exists) {
			Translations.getString("base.action.logout.name")
		} else {
			Translations.getString("base.action.login.name")
		}
	}
}

/**
 * Provides a UI for tracking the various steps of a [Auth0LoginFlow].
 */
class Auth0LoginView(
	properties: Properties = BaseModule.properties,
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
				.onWindowOpened {
					it.nextState()
				}
				.show()
		}
	}

	private val mainScope = MainScope()

	private val textArea = JTextArea()
	private val okAction = OkAction()
	private val cancelAction = CancelAction()
	private val continueButton = JButton(ActionWrapperSwing(okAction))

	private val loginParams = LoginParams(
		domain = properties.getString(Auth0LoginFlow.PROP_AUTH0_DOMAIN),
		clientId = properties.getString(Auth0LoginFlow.PROP_AUTH0_CLIENT_ID),
		redirectUrl = properties.getString(Auth0LoginFlow.PROP_AUTH0_REDIRECT_URL))

	private val flow = Auth0LoginFlow(mainScope, loginParams, ::handleStateChanged)

	init {
		buildUI()
		updateText()
	}

	private fun buildUI() {
		layout = BorderLayout(10, 10)
		border = BorderFactory.createEmptyBorder(INSET, INSET, INSET, INSET)

		textArea.isEditable = false
		textArea.rows = 5
		textArea.wrapStyleWord = true
		textArea.lineWrap = true
		add(textArea, BorderLayout.CENTER)

		val buttonPanel = JPanel()
		buttonPanel.layout = BoxLayout(buttonPanel, BoxLayout.LINE_AXIS)

		buttonPanel.add(Box.createHorizontalGlue())
		buttonPanel.add(JButton(ActionWrapperSwing(cancelAction)))
		buttonPanel.add(Box.createHorizontalStrut(2))
		buttonPanel.add(continueButton)
		add(buttonPanel, BorderLayout.SOUTH)
	}

	private fun handleStateChanged() {
		updateText()
		okAction.enabled = flow.state == FlowState.Done
		cancelAction.enabled = flow.state != FlowState.Done
	}

	private fun updateText() {
		textArea.text = when (flow.state) {
			FlowState.Startup -> ""
			FlowState.Initializing -> Translations.getString("base.action.login.initialize.text")
			FlowState.EnterCredentials -> Translations.getString("base.action.login.credentials.text")
			FlowState.Done -> Translations.getString("base.action.login.done.text")
		}
	}

	private fun stop() {
		flow.stop()
		closeHandler()
	}

	private fun nextState() {
		mainScope.launch(Dispatchers.Main) {
			flow.nextState()
		}
	}

	private inner class OkAction : AbstractAction("base.action.ok") {
		override fun execute(event: ActionEvent) {
			InvocationHandler.invoke {
				if (flow.state == FlowState.Done) {
					stop()
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