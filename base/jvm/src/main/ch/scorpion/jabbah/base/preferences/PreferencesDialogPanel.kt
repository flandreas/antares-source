package ch.scorpion.jabbah.base.preferences

import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.ActionWrapperSwing
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.PropertyChangeEvent
import ch.scorpion.jabbah.base.event.PropertyChangeListener
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.swing.DialogBuilder
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.Frame
import javax.swing.JButton
import javax.swing.JPanel


/** An action for showing [PreferencesDialogPanel] within a dialog.*/
class PreferencesAction : AbstractAction("base.preferences.action") {
	override fun execute(event: ActionEvent) {
		PreferencesDialogPanel.showAsDialog()
	}
}

class PreferencesDialogPanel(
	private val treePanel: PreferencesTreePanel = PreferencesTreePanel(),
	private val closeHandler: () -> Unit
) : JPanel() {

	companion object {

		private val LOG by logger(PreferencesDialogPanel::class)

		fun showAsDialog(
			parent: Frame = Frame.getFrames()[0],
			treePanel: PreferencesTreePanel = PreferencesTreePanel()
		) {
			LOG.debug("Show preferences")
			DialogBuilder<PreferencesDialogPanel>(parent)
				.content { dialog -> PreferencesDialogPanel(treePanel) { dialog.dispose() } }
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
			LOG.debug("Apply preferences")
			treePanel.applyChanges()
		}
	}

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

	private fun buildButtonPanel(): JPanel {
		val panel = JPanel(FlowLayout(FlowLayout.RIGHT))
		panel.add(applyButton)
		panel.add(JButton(ActionWrapperSwing(closeAction)))
		return panel
	}
}