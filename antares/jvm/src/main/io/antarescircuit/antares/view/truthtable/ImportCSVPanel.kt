package io.antarescircuit.antares.view.truthtable

import io.antarescircuit.antares.AntaresModuleJvm
import io.antarescircuit.antares.model.truthtable.TruthTableImportException
import io.antarescircuit.antares.model.truthtable.TruthTableImportParams
import io.antarescircuit.antares.model.truthtable.TruthTableReference
import io.antarescircuit.jabbah.base.*
import io.antarescircuit.jabbah.base.AbstractAction
import io.antarescircuit.jabbah.base.event.ActionEvent
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.base.swing.DataFormPanel
import io.antarescircuit.jabbah.base.swing.DialogBuilder
import io.antarescircuit.jabbah.base.swing.FileExtensionFilter
import io.antarescircuit.jabbah.base.swing.FileSelectionField
import io.antarescircuit.jabbah.base.ui.UIBasics
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Frame
import java.io.FileInputStream
import java.nio.file.Paths
import javax.swing.*
import kotlin.io.path.isDirectory

class ImportCSVPanel(
    private val truthTableRef: TruthTableReference,
    private val closeHandler: () -> Unit
) : JPanel() {

    companion object {

        private val LOG by logger(ImportCSVPanel::class)

        private const val PROP_HEADERS = "antares.truthTable.import.headers"
        private const val PROP_INPUTS = "antares.truthTable.import.inputs"
        private const val PROP_DIRECTORY = "antares.truthTable.import.directory"

        fun showAsDialog(
            parent: Frame,
            truthTableRef: TruthTableReference,
        ) {
            DialogBuilder<ImportCSVPanel>(parent)
                .content { dialog -> ImportCSVPanel(truthTableRef) { dialog.dispose() } }
                .title(Translations.getString("antares.truthTable.csv.import.name"))
                .preferredSize(Dimension(600, 200))
                .defaultButton { it.importButton }
                .nonResizable()
                .onWindowClosed { it.dispose() }
                .show()
        }
    }

    private val importAction = ImportAction()
    private val importButton = JButton(ActionWrapperSwing(importAction))
    private val cancelAction = CancelAction()

    private val headersCheckBox = JCheckBox()
    private val inputColumnsCheckBox = JCheckBox()

    private val fileSelectionField = FileSelectionField(
        FileSelectionField.Mode.File,
        BaseModule.settings.getString(PROP_DIRECTORY, ""),
        filter = FileExtensionFilter(setOf("csv"), "CSV")
    ) {
        importAction.enabled = StringUtils.isNotBlank(it)
    }

    init {
        buildUI()

        headersCheckBox.isSelected = BaseModule.settings.getBoolean(PROP_HEADERS, true)
        inputColumnsCheckBox.isSelected = BaseModule.settings.getBoolean(PROP_INPUTS, false)

        importAction.enabled = false
    }

    fun dispose() {
        BaseModule.settings.set(PROP_HEADERS, headersCheckBox.isSelected)
        BaseModule.settings.set(PROP_INPUTS, inputColumnsCheckBox.isSelected)

        val directory = if (Paths.get(fileSelectionField.path).isDirectory()) {
            fileSelectionField.path
        } else {
            Paths.get(fileSelectionField.path).parent
        }
        BaseModule.settings.set(PROP_DIRECTORY, directory ?: "")
    }

    private fun buildUI() {
        layout = BorderLayout(10, 10)
        border = UIBasics.createDialogBorder()

        val form = DataFormPanel()
        form.addLabeledRow(Translations.getString("antares.truthTable.csv.dialog.headers"), headersCheckBox)
        form.addLabeledRow(Translations.getString("antares.truthTable.csv.dialog.inputColumns"), inputColumnsCheckBox)
        form.addLabeledRow(Translations.getString("antares.truthTable.csv.dialog.file"), fileSelectionField, true)
        form.addFiller()

        val buttonPanel = JPanel()
        buttonPanel.layout = BoxLayout(buttonPanel, BoxLayout.LINE_AXIS)
        buttonPanel.add(Box.createHorizontalGlue())
        UIBasics.addButtons(buttonPanel, importButton, JButton(ActionWrapperSwing((cancelAction))))

        add(form, BorderLayout.CENTER)
        add(buttonPanel, BorderLayout.SOUTH)
    }

    private fun import() {
        val params = TruthTableImportParams(
            headers = headersCheckBox.isSelected,
            inputColumns = inputColumnsCheckBox.isSelected)

        try {
            LOG.userTrail("Importing TruthTable from CSV")

            AntaresModuleJvm.truthTableServiceJvm.importCSV(
                truthTableRef,
                params,
                FileInputStream(fileSelectionField.path))

            closeHandler()

            JOptionPane.showMessageDialog(
                Frame.getFrames()[0],
                "CSV successfully imported.",
                importAction.name,
                JOptionPane.INFORMATION_MESSAGE)
        } catch (e: TruthTableImportException) {
            JOptionPane.showMessageDialog(
                Frame.getFrames()[0],
                e.description,
                importAction.name,
                JOptionPane.ERROR_MESSAGE)
        } catch (e: Throwable) {
            LOG.error("General error", e)

            JOptionPane.showMessageDialog(
                Frame.getFrames()[0],
                Translations.getString("antares.truthTable.csv.generalError.msg"),
                importAction.name,
                JOptionPane.ERROR_MESSAGE)
        }
    }

    private inner class ImportAction : AbstractAction("antares.truthTable.csv.dialog.import") {
        override fun execute(event: ActionEvent) {
            import()
        }
    }

    private inner class CancelAction : AbstractAction("base.action.cancel") {
        override fun execute(event: ActionEvent) {
            closeHandler()
        }
    }
}