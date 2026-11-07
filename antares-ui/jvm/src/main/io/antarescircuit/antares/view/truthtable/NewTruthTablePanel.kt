package io.antarescircuit.antares.view.truthtable

import io.antarescircuit.antares.model.module.AntaresModelModule
import io.antarescircuit.antares.model.truthtable.TruthTable
import io.antarescircuit.antares.model.truthtable.TruthTableService
import io.antarescircuit.jabbah.base.AbstractAction
import io.antarescircuit.jabbah.base.Action
import io.antarescircuit.jabbah.base.ActionWrapperSwing
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.event.ActionEvent
import io.antarescircuit.jabbah.base.swing.DialogBuilder
import io.antarescircuit.jabbah.base.swing.UiUtil
import io.antarescircuit.jabbah.base.ui.UIBasics
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Frame
import javax.swing.*

/**
 * A [JPanel] for entering parameters for a new [TruthTable].
 * Performs validation of these parameters using [TruthTableService].
 */
class NewTruthTablePanel(
	private val service: TruthTableService = AntaresModelModule.truthTableService,
	private val closeHandler: () -> Unit
) : JPanel() {

	companion object {

		/**
		 * Asks the user for parameters of a new [TruthTable].
		 * @return the newly created [TruthTable], not yet integrated anywhere
		 */
		fun showAsDialog(title: String, parent: Frame): TruthTable? {
			val builder = DialogBuilder<NewTruthTablePanel>(parent)
				.content { dialog -> NewTruthTablePanel(closeHandler = { dialog.dispose() }) }
				.title(title)
				.defaultButton { it.okButton }
				.nonResizable()
				.show()

			return builder.content.result
		}
	}

	private val okAction = OkAction()
	private val okButton = createButton(okAction)
	private val cancelAction = CancelAction()

	private val nameLabel = JLabel(Translations.getString("library.newTruthTable.name"))
	private val inputLabel = JLabel(Translations.getString("library.newTruthTable.inputNames"))
	private val outputLabel = JLabel(Translations.getString("library.newTruthTable.outputNames"))

	private val nameField = JTextField(20)
	private val inputField = JTextField(20)
	private val outputField = JTextField(20)

	private val errorLabel = JLabel(" ")

	private var result: TruthTable? = null

	init {
		buildUI()
	}

	private fun buildUI() {
		layout = BorderLayout(10, 10)
		border = UIBasics.createDialogBorder()

		val contentPanel = JPanel()
		contentPanel.layout = BoxLayout(contentPanel, BoxLayout.PAGE_AXIS)

		nameLabel.horizontalAlignment = SwingConstants.RIGHT
		nameLabel.alignmentX = Component.LEFT_ALIGNMENT
		nameField.maximumSize = nameField.preferredSize
		nameField.alignmentX = Component.LEFT_ALIGNMENT
		contentPanel.add(nameLabel)
		contentPanel.add(Box.createVerticalStrut(2))
		contentPanel.add(nameField)
		contentPanel.add(Box.createVerticalStrut(12))

		inputLabel.horizontalAlignment = SwingConstants.RIGHT
		inputLabel.alignmentX = Component.LEFT_ALIGNMENT
		inputField.maximumSize = inputField.preferredSize
		inputField.alignmentX = Component.LEFT_ALIGNMENT
		contentPanel.add(inputLabel)
		contentPanel.add(Box.createVerticalStrut(2))
		contentPanel.add(inputField)
		contentPanel.add(Box.createVerticalStrut(12))

		outputLabel.horizontalAlignment = SwingConstants.RIGHT
		outputLabel.alignmentX = Component.LEFT_ALIGNMENT
		outputField.maximumSize = outputField.preferredSize
		outputField.alignmentX = Component.LEFT_ALIGNMENT
		contentPanel.add(outputLabel)
		contentPanel.add(Box.createVerticalStrut(2))
		contentPanel.add(outputField)
		contentPanel.add(Box.createVerticalStrut(12))

		errorLabel.foreground = UiUtil.errorTextColor
		contentPanel.add(errorLabel)
		contentPanel.add(Box.createVerticalGlue())

		val buttonPanel = JPanel()
		buttonPanel.layout = BoxLayout(buttonPanel, BoxLayout.LINE_AXIS)
		buttonPanel.add(Box.createHorizontalGlue())
		UIBasics.addButtons(buttonPanel, okButton, createButton(cancelAction))

		val iconPanel = JPanel()
		iconPanel.layout = BoxLayout(iconPanel, BoxLayout.PAGE_AXIS)
		iconPanel.add(JLabel(UIManager.getIcon("OptionPane.questionIcon")), BorderLayout.WEST)
		iconPanel.add(Box.createGlue())

		add(iconPanel, BorderLayout.WEST)
		add(contentPanel, BorderLayout.CENTER)
		add(buttonPanel, BorderLayout.SOUTH)
	}

	private fun createButton(action: Action): JButton = JButton(ActionWrapperSwing(action))

	private inner class OkAction : AbstractAction("base.action.ok") {
		override fun execute(event: ActionEvent) {
			try {
				result = service.createWithUserInput(nameField.text, inputField.text, outputField.text)
				closeHandler()
			} catch (e: Exception) {
				errorLabel.text = e.message
			}
		}
	}

	private inner class CancelAction : AbstractAction("base.action.cancel") {
		override fun execute(event: ActionEvent) {
			result = null
			closeHandler()
		}
	}
}