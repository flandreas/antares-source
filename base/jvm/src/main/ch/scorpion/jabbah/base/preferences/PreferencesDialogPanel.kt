package ch.scorpion.jabbah.base.preferences

import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.ActionWrapperSwing
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.PropertyChangeEvent
import ch.scorpion.jabbah.base.event.PropertyChangeListener
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.swing.DialogBuilder
import ch.scorpion.jabbah.base.swing.UiUtil
import ch.scorpion.jabbah.base.ui.HelpAction
import ch.scorpion.jabbah.base.ui.UIBasics
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Frame
import javax.swing.*


/** An action for showing [PreferencesDialogPanel] within a dialog.*/
class PreferencesAction : AbstractAction("base.preferences.action") {
	override fun execute(event: ActionEvent) {
		PreferencesDialogPanel.showAsDialog()
	}
}

class PreferencesDialogPanel(
	private val messageDisplay: PreferencesMessageDisplay,
	private val treePanel: PreferencesTreePanel = PreferencesTreePanel(messageDisplay),
	private val closeHandler: () -> Unit
) : JPanel() {

	companion object {

		private val LOG by logger(PreferencesDialogPanel::class)

		fun showAsDialog(parent: Frame = Frame.getFrames()[0]) {
			val messageDisplay = MyMessageDisplay()
			val treePanel = PreferencesTreePanel(messageDisplay)
			LOG.userTrail("Show preferences")
			DialogBuilder<PreferencesDialogPanel>(parent)
				.content { dialog -> PreferencesDialogPanel(messageDisplay, treePanel) { dialog.dispose() } }
				.defaultButton { it.applyButton }
				.title(Translations.getString("base.preferences.title.name"))
				.preferredSize(Dimension(800, 600))
				.resizable()
				.show()
		}
	}

	private val closeAction = object : AbstractAction("base.action.close") {
		override fun execute(event: ActionEvent) {
			closeHandler.invoke()
		}
	}

	private val applyAction = object : AbstractAction("base.action.apply") {
		override fun execute(event: ActionEvent) {
			LOG.userTrail("Apply preferences")
			treePanel.applyChanges()
		}
	}

	private val helpAction = HelpAction("/misc/preferences")

	val applyButton = JButton(ActionWrapperSwing(applyAction))

	init {
		applyAction.enabled = false
		treePanel.addPropertyChangeListener(object : PropertyChangeListener<Boolean> {
			override fun propertyChanged(e: PropertyChangeEvent<Boolean>) {
				applyAction.enabled = treePanel.changed
			}
		})
		buildUI()
	}

	private fun buildUI() {
		layout = BorderLayout()
		add(treePanel, BorderLayout.CENTER)
		add(buildButtonPanel(), BorderLayout.SOUTH)
	}

	/** ---- [PreferencesDialogPanel] */

	private fun buildButtonPanel(): JPanel {
		val panel = JPanel()
		panel.layout = BoxLayout(panel, BoxLayout.LINE_AXIS)
		panel.border = UIBasics.createDialogBorder()
		panel.add(UiUtil.createToolBarButton(helpAction))
		panel.add(Box.createHorizontalStrut(16))
		panel.add(messageDisplay as JComponent)
		panel.add(Box.createHorizontalGlue())
		UIBasics.addButtons(panel, applyButton, JButton(ActionWrapperSwing(closeAction)))
		return panel
	}

	private class MyMessageDisplay : JLabel(), PreferencesMessageDisplay {

		init {
			foreground = UiUtil.errorTextColor
		}

		override fun showMessage(message: String) {
			this.text = message
		}

		override fun hideMessage() {
			this.text = ""
		}
	}
}