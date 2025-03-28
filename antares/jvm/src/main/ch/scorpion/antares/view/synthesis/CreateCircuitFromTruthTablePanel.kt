package ch.scorpion.antares.view.synthesis

import ch.scorpion.antares.AntaresModuleJvm
import ch.scorpion.antares.model.truthtable.TruthTable
import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.ActionWrapperSwing
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.invocation.InvocationHandler
import ch.scorpion.jabbah.base.swing.DialogBuilder
import ch.scorpion.jabbah.base.swing.UiUtil
import ch.scorpion.jabbah.base.ui.UIBasics
import ch.scorpion.jabbah.graph.library.LibraryItem
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Frame
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

class CreateCircuitFromTruthTablePanel(
	private val truthTable: TruthTable,
	private val item: LibraryItem,
	private val service: CreateCircuitFromTruthTableService,
	private val closeHandler: () -> Unit
) : JPanel() {

	companion object {
		fun showAsDialog(
			parent: Frame,
			truthTable: TruthTable,
			item: LibraryItem,
			service: CreateCircuitFromTruthTableService = AntaresModuleJvm.createCircuitFromTruthTableService
		) {
			DialogBuilder<CreateCircuitFromTruthTablePanel>(parent)
				.content { dialog -> CreateCircuitFromTruthTablePanel(truthTable, item, service, closeHandler = { dialog.dispose() }) }
				.title(Translations.getString("antares.synthesis.createCircuitFromTruthTable.title"))
				.defaultButton { it.okButton }
				.nonResizable()
				.show()
		}
	}

	private val okAction = OkAction()
	private val okButton = JButton(ActionWrapperSwing(okAction))
	private val cancelAction = CancelAction()

	private val nameLabel = JLabel(Translations.getString("antares.synthesis.createCircuitFromTruthTable.circuitName"))
	private val nameField = JTextField(20)

	private val synthesisTypeLabel = JLabel(Translations.getString("antares.synthesis.type.name"))
	private val synthesisTypeField = JComboBox(CircuitSynthesisType
		.entries
		.filter { it.supportFSM }
		.toTypedArray()
	)

	init {
		buildUI()

		nameField.document.addDocumentListener(object : DocumentListener {
			override fun insertUpdate(e: DocumentEvent?) { update() }
			override fun removeUpdate(e: DocumentEvent?) { update() }
			override fun changedUpdate(e: DocumentEvent?) { update() }

			private fun update() {
				okAction.enabled = nameField.text.isNotBlank()
			}
		})
	}

	private fun buildUI() {
		layout = BorderLayout(10, 10)
		border = UIBasics.createDialogBorder()

		val contentPanel = JPanel()
		contentPanel.layout = BoxLayout(contentPanel, BoxLayout.PAGE_AXIS)

		nameField.text = truthTable.name.value

		nameLabel.horizontalAlignment = SwingConstants.RIGHT
		nameLabel.alignmentX = Component.LEFT_ALIGNMENT
		nameField.maximumSize = nameField.preferredSize
		nameField.alignmentX = Component.LEFT_ALIGNMENT
		contentPanel.add(nameLabel)
		contentPanel.add(Box.createVerticalStrut(2))
		contentPanel.add(nameField)
		contentPanel.add(Box.createVerticalStrut(6))

		synthesisTypeLabel.alignmentX = Component.LEFT_ALIGNMENT
		synthesisTypeField.alignmentX = Component.LEFT_ALIGNMENT
		synthesisTypeField.selectedIndex = 0
		contentPanel.add(synthesisTypeLabel)
		contentPanel.add(Box.createVerticalStrut(2))
		contentPanel.add(synthesisTypeField)
		contentPanel.add(Box.createVerticalStrut(18))

		val buttonPanel = JPanel()
		buttonPanel.layout = BoxLayout(buttonPanel, BoxLayout.LINE_AXIS)
		buttonPanel.add(Box.createHorizontalGlue())
		UIBasics.addButtons(buttonPanel, okButton, JButton(ActionWrapperSwing((cancelAction))))

		add(contentPanel, BorderLayout.CENTER)
		add(buttonPanel, BorderLayout.SOUTH)
	}

	private inner class OkAction : AbstractAction("base.action.ok") {
		override fun execute(event: ActionEvent) {
			InvocationHandler.invoke {
				try {
					service.create(truthTable, item, nameField.text, synthesisTypeField.selectedItem as CircuitSynthesisType)
					closeHandler()
				} catch (e: CircuitFromTruthTableBuilderError) {
					JOptionPane.showConfirmDialog(
						this@CreateCircuitFromTruthTablePanel,
						e.message,
						name,
						JOptionPane.OK_OPTION,
						JOptionPane.ERROR_MESSAGE
					)
				}
			}
		}
	}

	private inner class CancelAction : AbstractAction("base.action.cancel") {
		override fun execute(event: ActionEvent) {
			closeHandler()
		}
	}
}