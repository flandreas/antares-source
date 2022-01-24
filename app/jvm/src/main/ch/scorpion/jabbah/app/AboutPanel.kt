package ch.scorpion.jabbah.app

import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.ActionWrapperSwing
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.swing.DialogBuilder
import ch.scorpion.jabbah.base.swing.UiUtil
import java.awt.Component
import java.awt.Frame
import javax.swing.*

/** Displays [AboutInfo] of the [Application].*/
class AboutPanel(
	info: AboutInfo,
	private val closeHandler: () -> Unit
) : JPanel() {

	companion object {

		private const val INSET = 50

		fun showAsDialog(application: Application) {
			DialogBuilder<AboutPanel>(Frame.getFrames()[0])
				.content { dialog -> AboutPanel(application.aboutInfo) { dialog.dispose() } }
				.defaultButton { it.closeButton }
				.title("${Translations.getString("application.action.about.name")} ${application.aboutInfo.name}")
				.nonResizable()
				.show()
		}
	}

	val closeButton = JButton(ActionWrapperSwing(CloseAction()))

	init {
		buildUI(info)
	}

	private fun buildUI(info: AboutInfo) {
		layout = BoxLayout(this, BoxLayout.PAGE_AXIS)
		border = BorderFactory.createEmptyBorder(INSET, INSET, 20, INSET)

		if (info.iconPath != null) {
			addIcon(info.iconPath)
		}

		addText(info)
		add(Box.createVerticalStrut(50))
		addCloseButton()
	}

	private fun addIcon(iconPath: String) {
		add(JLabel(UiUtil.themedIcon(iconPath)))
	}

	private fun addText(info: AboutInfo) {
		addName(info)
		addClaim(info)
		addVersion(info)
		add(Box.createVerticalStrut(20))
		addDisclaimer(info)
	}

	private fun addName(info: AboutInfo) {
		addLabel("<h1>${info.name}</h1>")
	}

	private fun addClaim(info: AboutInfo) {
		addLabel("<h3>${info.claim}</h3")
	}

	private fun addVersion(info: AboutInfo) {
		addLabel("<font color=\"gray\">Version ${info.version}</font>")
	}

	private fun addDisclaimer(info: AboutInfo) {
		addLabel(info.disclaimer)
	}

	private fun addLabel(formattedText: String) {
		val label = JLabel("<html>${formattedText}", null, JLabel.CENTER)
		label.alignmentX = Component.CENTER_ALIGNMENT
		add(label)
	}

	private fun addCloseButton() {
		add(closeButton)
	}

	private inner class CloseAction : AbstractAction("file.action.close") {
		override fun execute(event: ActionEvent) {
			closeHandler.invoke()
		}
	}
}