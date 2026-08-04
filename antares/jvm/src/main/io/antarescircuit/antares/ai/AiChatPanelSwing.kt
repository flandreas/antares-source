package io.antarescircuit.antares.ai

import io.antarescircuit.jabbah.base.AbstractAction
import io.antarescircuit.jabbah.base.Action
import io.antarescircuit.jabbah.base.ActionWrapperSwing
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.event.ActionEvent
import io.antarescircuit.jabbah.base.preferences.PreferencesDialogPanel
import io.antarescircuit.jabbah.base.swing.UiUtil
import io.antarescircuit.jabbah.base.ui.UIBasics
import io.antarescircuit.jabbah.edit.Editor
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Dimension
import java.awt.Font
import java.awt.Frame
import java.awt.Insets
import java.awt.event.KeyEvent
import javax.swing.*
import javax.swing.text.SimpleAttributeSet
import javax.swing.text.StyleConstants

/**
 * The chat view of the circuit assistant, shown as a sidebar pane next to the circuit editor.
 *
 * The panel is a pure view: it renders the transcript and forwards user input to
 * [AiChatController], which owns the conversation and all circuit changes.
 */
class AiChatPanelSwing(
	editorProvider: () -> Editor?,
	private val controller: AiChatController = AiChatController(editorProvider)
) : JPanel(), AiChatController.Listener {

	companion object {
		private const val TRANSCRIPT_ROWS = 12
		private const val INPUT_ROWS = 3
	}

	private val transcript = JTextPane()

	private val input = JTextArea(INPUT_ROWS, 20)

	private val status = JLabel(" ")

	private val sendAction = SendAction()

	private val stopAction = StopAction()

	/** Offered in the title bar of the sidebar pane.*/
	val clearAction: Action = ClearAction()

	/** Offered in the title bar of the sidebar pane.*/
	val settingsAction: Action = SettingsAction()

	init {
		controller.listener = this
		buildUI()
		updateActions(busy = false)
		appendIntro()
	}

	fun dispose() {
		controller.dispose()
	}

	/** Moves the keyboard focus into the prompt field. */
	fun focusInput() {
		SwingUtilities.invokeLater { input.requestFocusInWindow() }
	}

	/** ---- [AiChatController.Listener] */

	override fun onUserMessage(text: String) {
		append(Translations.getString("antares.ai.role.you"), text, Role.User)
	}

	override fun onAssistantMessage(text: String) {
		append(Translations.getString("antares.ai.role.assistant"), text, Role.Assistant)
	}

	override fun onError(text: String) {
		append(Translations.getString("antares.ai.role.error"), text, Role.Error)
	}

	override fun onInfo(text: String) {
		append(null, text, Role.Info)
	}

	override fun onBusyChanged(busy: Boolean) {
		status.text = if (busy) Translations.getString("antares.ai.thinking") else " "
		updateActions(busy)
		if (!busy) {
			focusInput()
		}
	}

	override fun confirmDestructive(deletions: Int, clearsCircuit: Boolean): Boolean {
		val message = if (clearsCircuit) {
			Translations.getString("antares.ai.confirmClear.msg")
		} else {
			Translations.getString("antares.ai.confirmDelete.msg", deletions)
		}
		return JOptionPane.showConfirmDialog(
			this,
			message,
			Translations.getString("antares.ai.title"),
			JOptionPane.YES_NO_OPTION,
			JOptionPane.WARNING_MESSAGE
		) == JOptionPane.YES_OPTION
	}

	/** ---- [AiChatPanelSwing] */

	private fun buildUI() {
		layout = BorderLayout(0, UIBasics.ROW_GAP)
		border = BorderFactory.createEmptyBorder(6, 6, 6, 6)

		transcript.isEditable = false
		transcript.margin = Insets(4, 4, 4, 4)
		val transcriptScroll = JScrollPane(transcript)
		transcriptScroll.preferredSize = Dimension(280, TRANSCRIPT_ROWS * 18)
		transcriptScroll.verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED

		add(transcriptScroll, BorderLayout.CENTER)
		add(buildInputPanel(), BorderLayout.SOUTH)
	}

	private fun buildInputPanel(): JPanel {
		val panel = JPanel(BorderLayout(0, UIBasics.ROW_GAP))
		val examples = JPanel()
		examples.layout = BoxLayout(examples, BoxLayout.PAGE_AXIS)
		examples.add(createExampleButton("halfAdder"))
		examples.add(Box.createVerticalStrut(UIBasics.BUTTON_GAP))
		examples.add(createExampleButton("resultLeds"))
		panel.add(examples, BorderLayout.NORTH)

		input.lineWrap = true
		input.wrapStyleWord = true
		input.toolTipText = Translations.getString("antares.ai.input.tooltip")
		installInputKeyBindings()

		val inputScroll = JScrollPane(input)
		inputScroll.preferredSize = Dimension(280, INPUT_ROWS * 20)
		panel.add(inputScroll, BorderLayout.CENTER)
		panel.add(buildButtonPanel(), BorderLayout.SOUTH)

		return panel
	}

	private fun createExampleButton(example: String): JButton {
		val prompt = Translations.getString("antares.ai.example.$example.prompt")
		return JButton(Translations.getString("antares.ai.example.$example.name")).apply {
			alignmentX = Component.LEFT_ALIGNMENT
			toolTipText = prompt
			addActionListener {
				input.text = prompt
				focusInput()
			}
		}
	}

	private fun buildButtonPanel(): JPanel {
		val panel = JPanel()
		panel.layout = BoxLayout(panel, BoxLayout.LINE_AXIS)

		status.font = status.font.deriveFont(Font.ITALIC)
		panel.add(status)
		panel.add(Box.createHorizontalGlue())
		UIBasics.addButtons(panel, JButton(ActionWrapperSwing(sendAction)), JButton(ActionWrapperSwing(stopAction)))

		return panel
	}

	/** Enter sends the prompt, Shift+Enter inserts a line break. */
	private fun installInputKeyBindings() {
		val sendKey = "antares.ai.send"
		input.inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), sendKey)
		input.actionMap.put(sendKey, object : javax.swing.AbstractAction() {
			override fun actionPerformed(e: java.awt.event.ActionEvent?) {
				send()
			}
		})
		input.inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.SHIFT_DOWN_MASK), "insert-break")
	}

	private fun send() {
		if (controller.isBusy) {
			return
		}
		val text = input.text
		if (text.isBlank()) {
			return
		}
		input.text = ""
		controller.send(text)
	}

	/**
	 * Takes the busy state as a parameter instead of reading [AiChatController.isBusy], which is
	 * stale at both edges of a request: not yet busy when [onBusyChanged] fires before the job is
	 * assigned, and still busy while the finishing coroutine reports completion.
	 */
	private fun updateActions(busy: Boolean) {
		sendAction.enabled = !busy
		stopAction.enabled = busy
		input.isEnabled = !busy
	}

	private fun appendIntro() {
		onInfo(Translations.getString("antares.ai.intro", OpenRouterConfig.MODEL))
		if (OpenRouterConfig.keySource() == OpenRouterConfig.KeySource.None) {
			onInfo(Translations.getString("antares.ai.error.noApiKey", OpenRouterConfig.ENV_API_KEY))
		}
	}

	private enum class Role { User, Assistant, Error, Info }

	private fun append(label: String?, text: String, role: Role) {
		val document = transcript.styledDocument

		val labelStyle = SimpleAttributeSet()
		StyleConstants.setBold(labelStyle, true)
		val textStyle = SimpleAttributeSet()
		when (role) {
			Role.Error -> {
				StyleConstants.setForeground(labelStyle, UiUtil.errorTextColor)
				StyleConstants.setForeground(textStyle, UiUtil.errorTextColor)
			}
			Role.Info -> {
				StyleConstants.setItalic(textStyle, true)
				UIManager.getColor("Label.disabledForeground")?.let { StyleConstants.setForeground(textStyle, it) }
			}
			else -> Unit
		}

		if (document.length > 0) {
			document.insertString(document.length, "\n", textStyle)
		}
		label?.let { document.insertString(document.length, "$it\n", labelStyle) }
		document.insertString(document.length, "${text.trim()}\n", textStyle)

		transcript.caretPosition = document.length
	}

	private inner class SendAction : AbstractAction("antares.ai.action.send") {
		override fun execute(event: ActionEvent) {
			send()
		}
	}

	private inner class StopAction : AbstractAction("antares.ai.action.stop") {
		override fun execute(event: ActionEvent) {
			controller.cancel()
		}
	}

	private inner class ClearAction : AbstractAction("antares.ai.action.clear") {
		override fun execute(event: ActionEvent) {
			controller.clearConversation()
			transcript.styledDocument.remove(0, transcript.styledDocument.length)
			appendIntro()
		}
	}

	private inner class SettingsAction : AbstractAction("antares.ai.action.settings", opensDialog = true) {
		override fun execute(event: ActionEvent) {
			PreferencesDialogPanel.showAsDialog(name, Frame.getFrames()[0])
		}
	}
}
