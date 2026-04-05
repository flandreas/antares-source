package io.antarescircuit.jabbah.graph.library

import io.antarescircuit.jabbah.base.Settings
import io.antarescircuit.jabbah.base.AbstractAction
import io.antarescircuit.jabbah.base.ActionWrapperSwing
import io.antarescircuit.jabbah.base.StringUtils
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.event.ActionEvent
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.base.swing.*
import io.antarescircuit.jabbah.base.ui.UIBasics
import io.antarescircuit.jabbah.draw.graphics.ImageType
import org.apache.commons.io.FilenameUtils
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Frame
import java.nio.file.Paths
import javax.swing.*

/**
 * Provides a UI for [ImportImageAction] used for importing an image into a [Library].
 */
class ImportImagePanel(
    private val closeHandler: (ImportImagePanel) -> Unit
) : JPanel() {

    companion object {

        /** The name of the optional [String] in [Settings] containing the directory from which the last import took place.*/
        private const val SETTING_PATH = "graph.image.importDir"

        fun showAsDialog(
            title: String,
            parent: Frame = Frame.getFrames()[0],
        ): ImportImageParameters? {
            val builder = DialogBuilder<ImportImagePanel>(parent)
                .title(title)
                .content { dialog -> ImportImagePanel { dialog.dispose()} }
                .defaultButton { it.importButton }
                .show()
            return builder.content.parameters
        }
    }

    data class ImportImageParameters(
        val inputPath: String,
        val type: ImageType,
        val name: String
    )

    private val importAction = ImportAction()
    private val importButton = JButton(ActionWrapperSwing(importAction))
    private val cancelAction = CancelAction()

    private val nameTextField = JTextField()
    private val messageLabel = JLabel(" ")

    private val fileSelectionField = FileSelectionField(
        mode = FileSelectionField.Mode.File,
        text = if (BaseModule.settings.containsKey(SETTING_PATH)) {
            BaseModule.settings.get(SETTING_PATH)
        } else "",
        filter = FileExtensionFilter(
            ImageType.allFileExtensions,
            "${Translations.getString("draw.image.filterName")} (${ImageType.allFileExtensionsDesc})"
        )
    ) { path ->
        nameTextField.text = FilenameUtils.removeExtension(Paths.get(path).fileName.toString())
    }

    var parameters: ImportImageParameters? = null

    init {
        buildUI()
    }

    private fun determineImageType(): ImageType? =
        try {
            ImageType.withExtension(FilenameUtils.getExtension(fileSelectionField.path))
        } catch (e: Throwable) {
            null
        }

    private fun buildUI() {
        layout = BorderLayout(10, 20)
        border = UIBasics.createDialogBorder()
        add(buildContentPanel(), BorderLayout.CENTER)
        add(buildButtonPanel(), BorderLayout.SOUTH)
    }

    private fun buildContentPanel(): JPanel {
        val inset = 5
        var row = 0
        val rowDist = 8
        val panel = JPanel(EGBL.getLayout())

        nameTextField.preferredSize = Dimension(200, nameTextField.preferredSize.height)
        messageLabel.foreground = UiUtil.errorTextColor

        // File field
        EGBL.add(
            panel,
            JLabel("${Translations.getString("base.element.file.name")}:"),
            0, row,    // x, y
            1, 1,    // width, height
            0.0, 0.0,    // weightX, weightY
            EGBL.WEST,    // anchor
            EGBL.NONE,    // fill
            rowDist, inset, 0, 0)
        EGBL.add(
            panel,
            fileSelectionField,
            1, row++,
            EGBL.REMAINDER, 1,
            0.0, 0.0,
            EGBL.WEST,
            EGBL.HORIZONTAL,
            rowDist, 10, 0, inset)

        // Name TextField
        EGBL.add(
            panel,
            JLabel("${Translations.getString("base.element.name.name")}:"),
            0, row,    // x, y
            1, 1,    // width, height
            0.0, 0.0,    // weightX, weightY
            EGBL.WEST,    // anchor
            EGBL.NONE,    // fill
            rowDist, inset, 0, 0)
        EGBL.add(
            panel,
            nameTextField,
            1, row++,
            EGBL.REMAINDER, 1,
            0.0, 0.0,
            EGBL.WEST,
            EGBL.NONE,
            rowDist, 10, 0, inset)

        // Message label
        EGBL.add(
            panel,
            messageLabel,
            0, row,    // x, y
            EGBL.REMAINDER, 1,    // width, height
            0.0, 0.0,    // weightX, weightY
            EGBL.WEST,    // anchor
            EGBL.NONE,    // fill
            rowDist, inset, 0, 0)

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
        panel.add(Box.createHorizontalGlue())
        UIBasics.addButtons(panel, importButton, JButton(ActionWrapperSwing(cancelAction)))
        return panel
    }

    private inner class ImportAction : AbstractAction("base.action.import") {
        override fun execute(event: ActionEvent) {
            val imageType = determineImageType()
            if (imageType == null) {
                messageLabel.text = Translations.getString("draw.imageType.typeNotSupported.msg")
                return
            }
            if (StringUtils.isBlank(nameTextField.text)) {
                messageLabel.text = Translations.getString("draw.image.emptyName.msg")
                return
            }
            parameters = ImportImageParameters(fileSelectionField.path, imageType, nameTextField.text)
            BaseModule.settings.set(SETTING_PATH, FilenameUtils.getFullPath(fileSelectionField.path))
            closeHandler.invoke(this@ImportImagePanel)
        }
    }

    private inner class CancelAction : AbstractAction("base.action.cancel") {
        override fun execute(event: ActionEvent) {
            parameters = null
            closeHandler.invoke(this@ImportImagePanel)
        }
    }
}