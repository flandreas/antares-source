package ch.scorpion.jabbah.app

import ch.scorpion.jabbah.base.Translations
import java.awt.Color
import java.awt.Component
import java.awt.Frame
import javax.swing.*

/** Displays [AboutInfo] of the [Application].*/
class AboutPanel(info: AboutInfo) : JPanel() {

	companion object {

		private const val INSET = 50

		fun showAsDialog(application: Application) {
			val info = application.aboutInfo
			val panel = AboutPanel(info)
			val parent = Frame.getFrames()[0]

			val dialog = JDialog(parent, true)
			dialog.title = "${Translations.getString("application.action.about.name")} ${info.name}"
			dialog.contentPane.add(panel)
			dialog.isResizable = false
			//dialog.size = Dimension(400, 400)
			dialog.pack()
			dialog.setLocationRelativeTo(parent)
			dialog.isVisible = true
		}
	}

	init {
		buildUI(info)
	}

	private fun buildUI(info: AboutInfo) {
		layout = BoxLayout(this, BoxLayout.PAGE_AXIS)
		border = BorderFactory.createEmptyBorder(INSET, INSET, INSET, INSET)
		background = Color.WHITE

		if (info.iconPath != null) {
			addIcon(info.iconPath)
		}

		addText(info)
	}

	private fun addIcon(iconPath: String) {
		val label = JLabel(ImageIcon(AboutPanel::class.java.getResource(iconPath)))
		label.alignmentX = Component.CENTER_ALIGNMENT
		add(label)
	}

	private fun addText(info: AboutInfo) {
		addName(info)
		addClaim(info)
		addVersion(info)
		add(Box.createVerticalStrut(50))
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
		addLabel("${info.disclaimer}")
	}

	private fun addLabel(formattedText: String) {
		val label = JLabel("<html>${formattedText}", null, JLabel.CENTER)
		label.alignmentX = Component.CENTER_ALIGNMENT
		add(label)
	}
}