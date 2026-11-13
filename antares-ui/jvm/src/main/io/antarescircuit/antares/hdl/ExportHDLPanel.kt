package io.antarescircuit.antares.hdl

import io.antarescircuit.antares.hdl.vhdl.HDLException
import io.antarescircuit.antares.model.DigitalGraph
import io.antarescircuit.antares.model.testcase.Testcase
import io.antarescircuit.jabbah.base.Settings
import io.antarescircuit.jabbah.base.StringUtils
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.dsl.DslError
import io.antarescircuit.jabbah.base.help.HelpId
import io.antarescircuit.jabbah.base.invocation.InvocationHandler
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.base.richtext.RichText
import io.antarescircuit.jabbah.base.swing.DialogBuilder
import io.antarescircuit.jabbah.base.swing.EGBL
import io.antarescircuit.jabbah.base.swing.FileSelectionField
import io.antarescircuit.jabbah.base.swing.UiUtil
import io.antarescircuit.jabbah.base.ui.HelpAction
import io.antarescircuit.jabbah.base.ui.UIBasics
import org.apache.commons.lang3.SystemUtils
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Dimension
import java.awt.Frame
import java.awt.event.ActionEvent
import java.nio.file.Paths
import java.text.DecimalFormat
import javax.swing.*
import javax.swing.text.NumberFormatter
import kotlin.io.path.exists

/**
 * A panel for specifying parameters when exporting a [DigitalGraph] to a [HDLLanguage],
 * as well as perform exporting when pressing "Export".
 */
class ExportHDLPanel(
	private val circuit: DigitalGraph,
	private val language: HDLLanguage,
	private val closeHandler: (ExportHDLPanel) -> Unit
) : JPanel() {

	companion object {

		private const val DEF_WAIT_TIME = 30

		/** Suffix used for naming the generated test bench file.*/
		private const val TEST_SUFFIX = "_tb"

		val HELP_ID = HelpId("exportVHDL")

		fun showAsDialog(
			title: String,
			language: HDLLanguage,
			digitalGraph: DigitalGraph,
			parent: Frame = Frame.getFrames()[0]
		) {
			DialogBuilder<ExportHDLPanel>(parent)
				.title(title)
				.content { dialog -> ExportHDLPanel(digitalGraph, language) {
					it.dispose()
					dialog.dispose()}
				}
				.nonResizable()
				.defaultButton { it.okButton }
				.show()
		}
	}

	/** Names of persistent [Settings], separate per [language]. */
	private val settingUseDelayModel = "${language.settingsPrefix}.useDelayModel"
	private val settingExportDirectory = "${language.settingsPrefix}.directory"

	private val exportAction = ExportAction()
	private val okButton = JButton(exportAction)
	private val cancelAction = CancelAction()
	private val helpAction = HelpAction(HELP_ID)

	/** Used to select the [Testcase] to created the test bench with. */
	private val testcaseComboBox = createTestcaseComboBox(circuit.testcases.testcases)

	/** Used to choose if delay models in the HDL file are to be created (e.g. "after 20 ns")*/
	private val delayModelCheckBox = JCheckBox().also {
		it.isSelected = BaseModule.settings.getBoolean(settingUseDelayModel, true)
	}

	private val waitTimeFormatter = NumberFormatter(DecimalFormat.getIntegerInstance()).apply {
		minimum = 10
	}

	/** Used to enter the time between test vector execution in the test bench. */
	private val waitTimeTextField = JFormattedTextField(waitTimeFormatter)

	/** Used to specify the base file name of the exported files. Will be expanded by the [HDLLanguage.fileExtension]. */
	private val fileNameTextField = JTextField()

	/** Used to select the directory where the export files are written.*/
	private val directorySelectionField = FileSelectionField(
		mode = FileSelectionField.Mode.Directory,
		text = BaseModule.settings.getString(settingExportDirectory, SystemUtils.getUserHome().absolutePath),
		usage = "export"
	)

	private val fileNameTextExplanation = JLabel()

	init {
		buildUI()
		updateFields()
	}

	fun dispose() {
		directorySelectionField.rememberPath()
		BaseModule.settings.apply {
			set(settingUseDelayModel, delayModelCheckBox.isSelected)
			set(settingExportDirectory, directorySelectionField.path)
		}
	}

	private fun buildUI() {
		layout = BorderLayout(10, 20)
		border = UIBasics.createDialogBorder()
		add(buildParametersPanel(), BorderLayout.CENTER)
		add(buildButtonPanel(), BorderLayout.SOUTH)
	}

	private fun updateFields() {
		testcaseComboBox.isEnabled = testcaseComboBox.itemCount > 1
		waitTimeTextField.isEnabled = testcaseComboBox.selectedItem != null

		var text = language.fileExtension
		if (testcaseComboBox.selectedItem != null) {
			text = "$text / $TEST_SUFFIX${language.fileExtension}"
		}
		fileNameTextExplanation.text = text
	}

	private fun buildParametersPanel(): JPanel {
		val inset = 5
		var row = 0
		val rowDist = UIBasics.ROW_GAP
		val panel = JPanel(EGBL.getLayout())

		waitTimeTextField.preferredSize = Dimension(100, waitTimeTextField.preferredSize.height)
		val propDelay = circuit.effectivePropagationDelay
		waitTimeTextField.text = if (propDelay > 0) {
			propDelay.toString()
		} else {
			DEF_WAIT_TIME.toString()
		}

		fileNameTextField.preferredSize = Dimension(200, fileNameTextField.preferredSize.height)
		fileNameTextField.text = language.createRenaming().checkName(StringUtils.simplify(RichText.stripToPlainText(circuit.name.value)))

		if (testcaseComboBox.model.size > 1) {
			testcaseComboBox.selectedIndex = 1
		}

		testcaseComboBox.addActionListener {
			updateFields()
		}

		// Testcase selection
		EGBL.add(
			panel,
			JLabel("${Translations.getString("antares.vhdl.testcase.name")}:"),
			0, row,
			1, 1,
			0.0, 0.0,
			EGBL.WEST,
			EGBL.NONE,
			0, inset, 0, 0)
		EGBL.add(
			panel,
			testcaseComboBox,
			1, row++,
			EGBL.REMAINDER, 1,
			0.0, 0.0,
			EGBL.WEST,
			EGBL.NONE,
			0, 10, 0, inset
		)

		// Wait TextField
		EGBL.add(
			panel,
			JLabel("${Translations.getString("antares.vhdl.waitTime.name")}:"),
			0, row,    // x, y
			1, 1,    // width, height
			0.0, 0.0,    // weightX, weightY
			EGBL.WEST,    // anchor
			EGBL.NONE,    // fill
			rowDist, inset, 0, 0)
		EGBL.add(
			panel,
			waitTimeTextField,
			1, row++,
			EGBL.REMAINDER, 1,
			0.0, 0.0,
			EGBL.WEST,
			EGBL.NONE,
			rowDist, 10, 0, inset)

		// Delay model TextField
		EGBL.add(
			panel,
			JLabel("${Translations.getString("${language.keyPrefix}.delayModel.name")}:"),
			0, row,    // x, y
			1, 1,    // width, height
			0.0, 0.0,    // weightX, weightY
			EGBL.WEST,    // anchor
			EGBL.NONE,    // fill
			rowDist, inset, 0, 0)
		EGBL.add(
			panel,
			delayModelCheckBox,
			1, row++,
			EGBL.REMAINDER, 1,
			0.0, 0.0,
			EGBL.WEST,
			EGBL.NONE,
			rowDist, 10, 0, inset)

		val fileNameTextPanel = JPanel()
		fileNameTextPanel.layout = BoxLayout(fileNameTextPanel, BoxLayout.LINE_AXIS)
		fileNameTextPanel.add(fileNameTextField)
		fileNameTextPanel.add(Box.createHorizontalStrut(5))
		fileNameTextPanel.add(fileNameTextExplanation)

		// File name TextField
		EGBL.add(
			panel,
			JLabel("${Translations.getString("antares.vhdl.baseFileName.name")}:"),
			0, row,    // x, y
			1, 1,    // width, height
			0.0, 0.0,    // weightX, weightY
			EGBL.WEST,    // anchor
			EGBL.NONE,    // fill
			rowDist, inset, 0, 0)
		EGBL.add(
			panel,
			fileNameTextPanel,
			1, row++,
			EGBL.REMAINDER, 1,
			0.0, 0.0,
			EGBL.WEST,
			EGBL.NONE,
			rowDist, 10, 0, inset)

		// Directory field
		EGBL.add(
			panel,
			JLabel("${Translations.getString("antares.vhdl.exportDirectory.name")}:"),
			0, row,    // x, y
			1, 1,    // width, height
			0.0, 0.0,    // weightX, weightY
			EGBL.WEST,    // anchor
			EGBL.NONE,    // fill
			rowDist, inset, 0, 0)
		EGBL.add(
			panel,
			directorySelectionField,
			1, row++,
			EGBL.REMAINDER, 1,
			0.0, 0.0,
			EGBL.WEST,
			EGBL.HORIZONTAL,
			rowDist, 10, 0, inset)

		// Filler
		EGBL.add(
			panel,
			JPanel(),
			2, row,
			EGBL.REMAINDER, EGBL.REMAINDER,
			1.0, 1.0,
			EGBL.NORTHWEST,
			EGBL.BOTH)

		return panel
	}

	private fun buildButtonPanel(): JPanel {
		val panel = JPanel()
		panel.layout = BoxLayout(panel, BoxLayout.LINE_AXIS)

		panel.add(UiUtil.createToolBarButton(helpAction))
		panel.add(Box.createHorizontalGlue())
		UIBasics.addButtons(panel, okButton, JButton(cancelAction))
		return panel
	}

	private fun createTestcaseComboBox(testcases: List<Testcase>): JComboBox<Testcase> {
		val comboBox = JComboBox<Testcase>()
		comboBox.renderer = TestcaseRenderer()
		comboBox.addItem(null)
		testcases.forEach { comboBox.addItem(it) }
		return comboBox
	}

	private class TestcaseRenderer : DefaultListCellRenderer() {
		override fun getListCellRendererComponent(list: JList<*>?, value: Any?, index: Int, isSelected: Boolean, cellHasFocus: Boolean): Component {
			val label = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus) as JLabel
			label.text = if (value == null) {
				Translations.getString("antares.vhdl.testcase.none")
			} else {
				(value as Testcase).name.getTranslation()
			}
			return label
		}
	}

	private inner class ExportAction : AbstractAction(Translations.getString("base.action.export.name")) {
		override fun actionPerformed(e: ActionEvent?) {
			try {
				val params = validatedParameters()

				if (params.hdlFile.exists() || params.testBenchParams?.tbFile?.exists() == true) {
					when (JOptionPane.showConfirmDialog(
						this@ExportHDLPanel,
						Translations.getString("antares.vhdl.fileExists.msg"),
						getValue(Action.NAME) as String,
						JOptionPane.YES_NO_CANCEL_OPTION,
						JOptionPane.QUESTION_MESSAGE
					)) {
						JOptionPane.CANCEL_OPTION -> {
							closeHandler(this@ExportHDLPanel)
							return
						}
						JOptionPane.NO_OPTION -> return
						JOptionPane.YES_OPTION -> {}
					}
				}

				export(params)

				closeHandler(this@ExportHDLPanel)
			} catch (e: IllegalArgumentException) {
				// Validation error
				JOptionPane.showMessageDialog(
					this@ExportHDLPanel,
					e.message,
					getValue(Action.NAME) as String,
					JOptionPane.ERROR_MESSAGE
				)
			}
		}
	}

	/**
	 * Validates user input parameters and returns them as [HDLExportParams].
	 * @throws IllegalArgumentException with a translated message in case of an invalid parameter
	 */
	private fun validatedParameters(): HDLExportParams {
		if (StringUtils.isBlank(fileNameTextField.text)) {
			throw IllegalArgumentException(Translations.getString("antares.vhdl.portNameNotFound.error.text"))
		}

		val renaming = language.createRenaming()
		val baseName = fileNameTextField.text
		val normalizedBaseName = renaming.checkName(StringUtils.simplify(baseName))
		if (baseName != normalizedBaseName) {
			throw IllegalArgumentException(Translations.getString("antares.vhdl.fileNameInvalid.error.txt", normalizedBaseName))
		}

		val hdlFile = Paths.get(directorySelectionField.path, "$baseName${language.fileExtension}")

		var testBenchParams: HDLExportTestBenchParams? = null
		if (testcaseComboBox.selectedItem != null) {
			val testBenchName = createTestName(baseName, renaming)
			val tbFile = Paths.get(directorySelectionField.path, "$testBenchName${language.fileExtension}")
			testBenchParams = HDLExportTestBenchParams(
				renaming,
				testBenchName,
				testcaseComboBox.selectedItem as Testcase,
				tbFile,
				waitTimeTextField.text.toInt())
		}

		return HDLExportParams(
			renaming,
			baseName,
			delayModelCheckBox.isSelected,
			hdlFile,
			testBenchParams
		)
	}

	private fun createTestName(baseName: String, renaming: HDLRenaming): String {
		val testcase = testcaseComboBox.selectedItem as Testcase
		val testName = StringUtils.simplify(RichText.stripToPlainText(testcase.name.value))
		return if (StringUtils.isNotBlank(testName)) {
			"${baseName}_${renaming.checkName(testName)}$TEST_SUFFIX"
		} else {
			"$baseName$TEST_SUFFIX"
		}
	}

	private fun export(params: HDLExportParams) {
		InvocationHandler.invoke {
			try {
				language.generate(params, circuit)

				JOptionPane.showMessageDialog(
					Frame.getFrames()[0],
					Translations.getString("antares.vhdl.success.msg", params.hdlFile.toAbsolutePath()),
					Translations.getString("base.action.export.name"),
					JOptionPane.INFORMATION_MESSAGE
				)
			} catch (e: HDLException) {
				JOptionPane.showMessageDialog(
					Frame.getFrames()[0],
					e.message,
					Translations.getString("base.action.export.name"),
					JOptionPane.ERROR_MESSAGE
				)
			} catch (e: DslError) {
				JOptionPane.showMessageDialog(
					Frame.getFrames()[0],
					Translations.getString("antares.vhdl.testcase.error.text", e.message ?: ""),
					Translations.getString("base.action.export.name"),
					JOptionPane.ERROR_MESSAGE
				)
			}
		}
	}

	private inner class CancelAction : AbstractAction(Translations.getString("base.action.cancel.name")) {
		override fun actionPerformed(e: ActionEvent?) {
			closeHandler(this@ExportHDLPanel)
		}
	}
}