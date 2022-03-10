package ch.scorpion.antares.view.expression

import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.ActionWrapperSwing
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.swing.DialogBuilder
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Frame
import javax.swing.*

/**
 * A [JPanel] for entering parameters for a new boolean expression.
 */
class NewBooleanExpressionPanel(
	private val closeHandler: () -> Unit
) : JPanel() {

	companion object {
		fun showAsDialog(parent: Frame): String? {
			val builder = DialogBuilder<NewBooleanExpressionPanel>(parent)
				.content { dialog -> NewBooleanExpressionPanel { dialog.dispose() } }
				.title(Translations.getString("library.newBooleanExpression.title"))
				.defaultButton { it.okButton }
				.nonResizable()
				.show()

			return builder.content.result
		}
	}

	private val okAction = OkAction()
	private val okButton = createButton(okAction)
	private val cancelAction = CancelAction()

	private val nameLabel = JLabel(Translations.getString("library.booleanExpression.name"))
	private val nameField = JTextField(20)

	private var result: String? = null

	init {
		buildUI()
	}

	private fun buildUI() {
		layout = BorderLayout(10, 10)
		border = BorderFactory.createEmptyBorder(15, 15, 15, 15)

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

		val buttonPanel = JPanel()
		buttonPanel.layout = BoxLayout(buttonPanel, BoxLayout.LINE_AXIS)
		buttonPanel.add(Box.createHorizontalGlue())
		buttonPanel.add(createButton(cancelAction))
		buttonPanel.add(Box.createHorizontalStrut(5))
		buttonPanel.add(okButton)

		add(contentPanel, BorderLayout.CENTER)
		add(buttonPanel, BorderLayout.SOUTH)
	}

	private fun createButton(action: Action): JButton = JButton(ActionWrapperSwing(action))

	private inner class OkAction : AbstractAction("base.action.ok") {
		override fun execute(event: ActionEvent) {
			result = nameField.text
			closeHandler()
		}
	}

	private inner class CancelAction : AbstractAction("base.action.cancel") {
		override fun execute(event: ActionEvent) {
			result = null
			closeHandler()
		}
	}
}