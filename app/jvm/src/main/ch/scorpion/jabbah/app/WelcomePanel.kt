package ch.scorpion.jabbah.app

import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.ActionWrapperSwing
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.swing.DialogBuilder
import ch.scorpion.jabbah.base.swing.UiUtil
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Frame
import javax.swing.*

class WelcomePanel(
	private val closeHandler: () -> Unit
) : JPanel() {

	companion object {
		fun showAsDialog(application: Application, parent: Frame = Frame.getFrames()[0]) {
			DialogBuilder<WelcomePanel>(parent)
				.content { dialog -> WelcomePanel { dialog.dispose() } }
				.title(Translations.getString("application.welcome.dialog.title", application.displayName))
				.defaultButton { it.okButton }
				.preferredSize(Dimension(350, 300))
				.nonResizable()
				.show()
		}
	}

	private val okAction = OkAction()
	private val okButton = JButton(ActionWrapperSwing(okAction))

	init {
		buildUI()
	}

	private fun buildUI() {
		layout = BorderLayout(10, 10)
		border = BorderFactory.createEmptyBorder(15, 20, 15, 20)

		val contentPanel = JPanel()
		contentPanel.layout = BoxLayout(contentPanel, BoxLayout.PAGE_AXIS)

		addTextPane(BorderLayout.CENTER)
		addButtonPanel(BorderLayout.SOUTH)
	}

	private fun addTextPane(@Suppress("SameParameterValue") constraints: Any) {
		val textPane = UiUtil.createHtmlEditorPane(
			Translations.getString("application.welcome.text"),
			"application.welcome.action.name")
		add(textPane, constraints)
	}

	private fun addButtonPanel(@Suppress("SameParameterValue") constraints: Any) {
		val buttonPanel = JPanel()
		buttonPanel.layout = BoxLayout(buttonPanel, BoxLayout.LINE_AXIS)
		buttonPanel.add(Box.createHorizontalGlue())
		buttonPanel.add(okButton)
		add(buttonPanel, constraints)
	}

	private inner class OkAction : AbstractAction("base.action.ok") {
		override fun execute(event: ActionEvent) {
			closeHandler()
		}
	}
}