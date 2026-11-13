package io.antarescircuit.antares.view.synthesis

import io.antarescircuit.antares.AntaresModuleJvm
import io.antarescircuit.antares.model.truthtable.TruthTable
import io.antarescircuit.jabbah.base.AbstractAction
import io.antarescircuit.jabbah.base.ActionWrapperSwing
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.event.ActionEvent
import io.antarescircuit.jabbah.base.invocation.InvocationHandler
import io.antarescircuit.jabbah.base.swing.DialogBuilder
import io.antarescircuit.jabbah.base.swing.UiUtil
import io.antarescircuit.jabbah.base.ui.UIBasics
import io.antarescircuit.jabbah.graph.library.LibraryItem
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

	private val twoInputGatesOnlyField = JCheckBox(Translations.getString("antares.synthesis.twoInputGatesOnly"))
	/** Only combinational truth tables can be turned into test cases.*/
	private val createTestcaseField = JCheckBox(Translations.getString("antares.synthesis.createCircuitFromTruthTable.createTestcase"), true)
		.also { it.isVisible = truthTable.stateColumnCount == 0 }

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

		synthesisTypeField.addActionListener { updateTwoInputGatesOnlyField() }
		updateTwoInputGatesOnlyField()
	}

	private val selectedSynthesisType: CircuitSynthesisType get() = synthesisTypeField.selectedItem as CircuitSynthesisType

	private fun updateTwoInputGatesOnlyField() {
		twoInputGatesOnlyField.isEnabled = selectedSynthesisType.supportTwoInputGatesOnly
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
		contentPanel.add(Box.createVerticalStrut(12))

		synthesisTypeLabel.alignmentX = Component.LEFT_ALIGNMENT
		synthesisTypeField.alignmentX = Component.LEFT_ALIGNMENT
		synthesisTypeField.selectedIndex = 0
		contentPanel.add(synthesisTypeLabel)
		contentPanel.add(Box.createVerticalStrut(2))
		contentPanel.add(synthesisTypeField)
		contentPanel.add(Box.createVerticalStrut(12))

		twoInputGatesOnlyField.alignmentX = Component.LEFT_ALIGNMENT
		contentPanel.add(twoInputGatesOnlyField)
		createTestcaseField.alignmentX = Component.LEFT_ALIGNMENT
		contentPanel.add(createTestcaseField)
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
					service.create(
						truthTable,
						item,
						nameField.text,
						selectedSynthesisType,
						twoInputGatesOnlyField.isEnabled && twoInputGatesOnlyField.isSelected,
						createTestcaseField.isSelected,
					)
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