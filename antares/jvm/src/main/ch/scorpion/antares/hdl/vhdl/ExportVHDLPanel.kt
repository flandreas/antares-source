package ch.scorpion.antares.hdl.vhdl

import ch.scorpion.antares.hdl.HDLExportParams
import ch.scorpion.antares.hdl.HDLExportTestBenchParams
import ch.scorpion.antares.model.DigitalGraph
import ch.scorpion.antares.model.testcase.Testcase
import ch.scorpion.jabbah.base.Settings
import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.dsl.DslError
import ch.scorpion.jabbah.base.help.HelpId
import ch.scorpion.jabbah.base.invocation.InvocationHandler
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.richtext.RichText
import ch.scorpion.jabbah.base.swing.DialogBuilder
import ch.scorpion.jabbah.base.swing.FileSelectionField
import ch.scorpion.jabbah.base.swing.EGBL
import ch.scorpion.jabbah.base.swing.UiUtil
import ch.scorpion.jabbah.base.ui.HelpAction
import ch.scorpion.jabbah.base.ui.UIBasics
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
 * A panel for specifying parameters when exporting a [DigitalGraph] to VHDL,
 * as well as perform exporting when pressing "Export".
 */
class ExportVHDLPanel(
	private val circuit: DigitalGraph,
	private val closeHandler: (ExportVHDLPanel) -> Unit
) : JPanel() {

	companion object {

		private const val DEF_WAIT_TIME = 30

		/** File name extensions used for naming the generated files.*/
		private const val VHDL_FILE_EXT = ".vhdl"
		private const val VHDL_TEST_SUFFIX = "_tb"

		/** Names of persistent [Settings]. */
		private const val SETTING_USE_DELAY_MODEL = "exportVHDL.useDelayModel"
		private const val SETTING_EXPORT_DIRECTORY = "exportVHDL.directory"

		val HELP_ID = HelpId("exportVHDL")

		fun showAsDialog(
			digitalGraph: DigitalGraph,
			parent: Frame = Frame.getFrames()[0]
		) {
			DialogBuilder<ExportVHDLPanel>(parent)
				.title(Translations.getString("antares.vhdl.action.name"))
				.content { dialog -> ExportVHDLPanel(digitalGraph) {
					it.dispose()
					dialog.dispose()}
				}
				.nonResizable()
				.defaultButton { it.okButton }
				.show()
		}
	}

	private val exportAction = ExportAction()
	private val okButton = JButton(exportAction)
	private val cancelAction = CancelAction()
	private val helpAction = HelpAction(HELP_ID)

	/** Used to select the [Testcase] to created the test bench with. */
	private val testcaseComboBox = createTestcaseComboBox(circuit.testcases.testcases)

	/** Used to choose if delay models in the VHDL file are to be created (e.g. "after 20 ns")*/
	private val delayModelCheckBox = JCheckBox().also {
		it.isSelected = BaseModule.settings.getBoolean(SETTING_USE_DELAY_MODEL, true)
	}

	private val waitTimeFormatter = NumberFormatter(DecimalFormat.getIntegerInstance()).apply {
		minimum = 10
	}

	/** Used to enter the time between test vector execution in the test bench. */
	private val waitTimeTextField = JFormattedTextField(waitTimeFormatter)

	/** Used to specify the base file name of the exported files. Will be expanded by .vhdl. */
	private val fileNameTextField = JTextField()

	/** Used to select the directory where the export files are written.*/
	private val directorySelectionField = FileSelectionField(
		mode = FileSelectionField.Mode.Directory,
		text = BaseModule.settings.getString(SETTING_EXPORT_DIRECTORY, SystemUtils.getUserHome().absolutePath)
	)

	private val fileNameTextExplanation = JLabel()

	init {
		buildUI()
		updateFields()
	}

	fun dispose() {
		BaseModule.settings.apply {
			set(SETTING_USE_DELAY_MODEL, delayModelCheckBox.isSelected)
			set(SETTING_EXPORT_DIRECTORY, directorySelectionField.path)
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

		var text = VHDL_FILE_EXT
		if (testcaseComboBox.selectedItem != null) {
			text = "$text / $VHDL_TEST_SUFFIX$VHDL_FILE_EXT"
		}
		fileNameTextExplanation.text = text
	}

	private fun buildParametersPanel(): JPanel {
		val inset = 5
		var row = 0
		val rowDist = 8
		val panel = JPanel(EGBL.getLayout())

		waitTimeTextField.preferredSize = Dimension(100, waitTimeTextField.preferredSize.height)
		waitTimeTextField.text = circuit.overallPropagationDelay?.toString() ?: DEF_WAIT_TIME.toString()

		fileNameTextField.preferredSize = Dimension(200, fileNameTextField.preferredSize.height)
		fileNameTextField.text = VHDLRenaming().checkName(StringUtils.simplify(RichText.stripToPlainText(circuit.name.value)))

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
			JLabel("${Translations.getString("antares.vhdl.delayModel.name")}:"),
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
						this@ExportVHDLPanel,
						Translations.getString("antares.vhdl.fileExists.msg"),
						getValue(Action.NAME) as String,
						JOptionPane.YES_NO_CANCEL_OPTION,
						JOptionPane.QUESTION_MESSAGE
					)) {
						JOptionPane.CANCEL_OPTION -> {
							closeHandler(this@ExportVHDLPanel)
							return
						}
						JOptionPane.NO_OPTION -> return
						JOptionPane.YES_OPTION -> {}
					}
				}

				export(params)

				closeHandler(this@ExportVHDLPanel)
			} catch (e: IllegalArgumentException) {
				// Validation error
				JOptionPane.showMessageDialog(
					this@ExportVHDLPanel,
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

		val renaming = VHDLRenaming()
		val baseName = fileNameTextField.text
		val normalizedBaseName = renaming.checkName(StringUtils.simplify(baseName))
		if (baseName != normalizedBaseName) {
			throw IllegalArgumentException(Translations.getString("antares.vhdl.fileNameInvalid.error.txt", normalizedBaseName))
		}

		val vhdlFile = Paths.get(directorySelectionField.path, "$baseName$VHDL_FILE_EXT")

		var testBenchParams: HDLExportTestBenchParams? = null
		if (testcaseComboBox.selectedItem != null) {
			val testBenchName = createTestName(baseName, renaming)
			val tbFile = Paths.get(directorySelectionField.path, "$testBenchName$VHDL_FILE_EXT")
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
			vhdlFile,
			testBenchParams
		)
	}

	private fun createTestName(vhdlFileName: String, renaming: VHDLRenaming): String {
		val testcase = testcaseComboBox.selectedItem as Testcase
		val testName = StringUtils.simplify(RichText.stripToPlainText(testcase.name.value))
		return if (StringUtils.isNotBlank(testName)) {
			"${vhdlFileName}_${renaming.checkName(testName)}_tb"
		} else {
			"${vhdlFileName}_tb"
		}
	}

	private fun export(params: HDLExportParams) {
		InvocationHandler.invoke {
			try {
				VHDLGenerator(
					params,
					generateComment = true
				).generate(circuit)

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
			closeHandler(this@ExportVHDLPanel)
		}
	}
}