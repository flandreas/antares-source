package ch.scorpion.jabbah.draw.rasterimg

import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.ActionWrapperSwing
import ch.scorpion.jabbah.base.Settings
import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.swing.DialogBuilder
import ch.scorpion.jabbah.base.swing.EGBL
import ch.scorpion.jabbah.base.swing.FileSelectionField
import ch.scorpion.jabbah.base.swing.UiUtil
import ch.scorpion.jabbah.base.ui.UIBasics
import ch.scorpion.jabbah.draw.MainContent
import ch.scorpion.jabbah.draw.graphics.ImageType
import org.apache.commons.lang3.SystemUtils
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Frame
import java.io.IOException
import java.nio.file.Path
import java.text.DecimalFormat
import javax.swing.*
import javax.swing.text.NumberFormatter
import kotlin.io.path.absolutePathString
import kotlin.io.path.exists

class ExportRasterImagePanel(
    private val mainContent: MainContent,
    private val closeHandler: () -> Unit
) : JPanel() {

    companion object {

        private const val TEXT_FIELD_WIDTH = 300

        private const val DEF_IMAGE_WIDTH = 800
        private const val DEF_IMAGE_HEIGHT = 600

        /** The name in [Settings] for storing the directory path where images are exported.*/
        private const val SETTING_PATH = "draw.rasterimg.export.path"

        private val title: String get() = Translations.getString("draw.action.exportImage.title")

        fun showAsDialog(
            mainContent: MainContent,
            parent: Frame = Frame.getFrames()[0]
        ) {
            DialogBuilder<ExportRasterImagePanel>(parent)
                .content { dialog -> ExportRasterImagePanel(mainContent) { dialog.dispose() } }
                .title(title)
                .nonResizable()
                .defaultButton { it.okButton }
                .show()
        }
    }

    private val okAction = OkAction()
    private val okButton = JButton(ActionWrapperSwing(okAction))
    private val cancelAction = CancelAction()

    private val imageTypeComboBox = JComboBox<ImageType>().also {
        ImageType.entries.toTypedArray().filter { it.isRaster }.forEach { type -> it.addItem(type) }
    }

    private val directorySelectionField = FileSelectionField(
        text = BaseModule.settings.getString(SETTING_PATH, SystemUtils.getUserHome().absolutePath),
        labelText = null,
        preferredWidth = TEXT_FIELD_WIDTH)

    private val fileNameField = JTextField()

    private val widthField = JFormattedTextField(NumberFormatter(DecimalFormat.getIntegerInstance()).apply { minimum = 1 })
    private val heightField = JFormattedTextField(NumberFormatter(DecimalFormat.getIntegerInstance()).apply { minimum = 1 })

    private val errorLabel = JLabel(" ")

    init {
        buildUI()
        imageTypeComboBox.addActionListener {
            fileNameField.text = buildFileName()
        }
        widthField.text = DEF_IMAGE_WIDTH.toString()
        heightField.text = DEF_IMAGE_HEIGHT.toString()
        errorLabel.foreground = UiUtil.errorTextColor
    }

    private fun buildUI() {
        layout = BorderLayout(10, 20)
        border = UIBasics.createDialogBorder()
        add(createContentPanel(), BorderLayout.NORTH)
        add(errorLabel, BorderLayout.CENTER)
        add(createButtonPanel(), BorderLayout.SOUTH)
    }

    private fun createContentPanel(): JPanel {
        val inset = 5
        var row = 0
        val rowDist = UIBasics.ROW_GAP
        val panel = JPanel(EGBL.getLayout())

        // ImageType (Raster only)
        EGBL.add(
            panel,
            JLabel("${Translations.getString("draw.imageType.name")}:"),
            0, row,
            1, 1,
            0.0, 0.0,
            EGBL.WEST,
            EGBL.NONE,
            rowDist, inset, 0, UIBasics.LABEL_GAP)
        EGBL.add(
            panel,
            imageTypeComboBox,
            1, row++,
            EGBL.REMAINDER, 1,
            0.0, 0.0,
            EGBL.WEST,
            EGBL.NONE,
            rowDist, 0, 0, inset)

        // Directory
        EGBL.add(
            panel,
            JLabel("${Translations.getString("base.element.directory.name")}:"),
            0, row,
            1, 1,
            0.0, 0.0,
            EGBL.WEST,
            EGBL.NONE,
            rowDist, inset, 0, UIBasics.LABEL_GAP)
        EGBL.add(
            panel,
            directorySelectionField,
            1, row++,
            EGBL.REMAINDER, 1,
            0.0, 0.0,
            EGBL.WEST,
            EGBL.NONE,
            rowDist, 0, 0, inset)

        // File name: Text field
        fileNameField.text = buildFileName()
        fileNameField.preferredSize = Dimension(TEXT_FIELD_WIDTH, fileNameField.preferredSize.height)
        EGBL.add(
            panel,
            JLabel("${Translations.getString("base.element.file.name")}:"),
            0, row,
            1, 1,
            0.0, 0.0,
            EGBL.WEST,
            EGBL.NONE,
            rowDist, inset, 0, UIBasics.LABEL_GAP)
        EGBL.add(
            panel,
            fileNameField,
            1, row++,
            EGBL.REMAINDER, 1,
            0.0, 0.0,
            EGBL.WEST,
            EGBL.NONE,
            rowDist, 0, 0, inset)

        EGBL.add(
            panel,
            JLabel("${Translations.getString("draw.image.width.name")}:"),
            0, row,
            1, 1,
            0.0, 0.0,
            EGBL.WEST,
            EGBL.NONE,
            rowDist, inset, 0, UIBasics.LABEL_GAP)
        EGBL.add(
            panel,
            widthField,
            1, row++,
            EGBL.REMAINDER, 1,
            0.0, 0.0,
            EGBL.WEST,
            EGBL.NONE,
            rowDist, 0, 0, inset)

        EGBL.add(
            panel,
            JLabel("${Translations.getString("draw.image.height.name")}:"),
            0, row,
            1, 1,
            0.0, 0.0,
            EGBL.WEST,
            EGBL.NONE,
            rowDist, inset, 0, UIBasics.LABEL_GAP)
        EGBL.add(
            panel,
            heightField,
            1, row++,
            EGBL.REMAINDER, 1,
            0.0, 0.0,
            EGBL.WEST,
            EGBL.NONE,
            rowDist, 0, 0, inset)

        return panel
    }

    private fun buildFileName(): String =
        "${StringUtils.simplify(mainContent.name)}.${(imageTypeComboBox.selectedItem as ImageType).fileExtension}"

    private fun createButtonPanel(): JPanel {
        val buttonPanel = JPanel()
        buttonPanel.layout = BoxLayout(buttonPanel, BoxLayout.LINE_AXIS)
        buttonPanel.add(Box.createHorizontalGlue())
        UIBasics.addButtons(buttonPanel, okButton, JButton(ActionWrapperSwing(cancelAction)))
        return buttonPanel
    }

    private fun validateInput(): Boolean {
        if (StringUtils.isBlank(fileNameField.text)) {
            errorLabel.text = Translations.getString("draw.image.emptyName.msg")
            return false
        }
        if (StringUtils.isEmpty(widthField.text)) {
            errorLabel.text = Translations.getString("draw.image.emptyWidth.msg")
            return false
        }
        if (StringUtils.isEmpty(heightField.text)) {
            errorLabel.text = Translations.getString("draw.image.emptyHeight.msg")
            return false
        }

        widthField.commitEdit()
        heightField.commitEdit()

        return true
    }

    private fun export(path: Path): Boolean {
        try {
            BaseModule.settings.set(SETTING_PATH, directorySelectionField.path)

            if (path.exists()) {
                when (JOptionPane.showConfirmDialog(
                    this@ExportRasterImagePanel,
                    Translations.getString("antares.vhdl.fileExists.msg"),
                    title,
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
                )) {
                    JOptionPane.NO_OPTION -> return false
                    JOptionPane.YES_OPTION -> {}
                }
            }

            RasterImageExporter.exportToFile(
                mainContent,
                imageTypeComboBox.selectedItem as ImageType,
                path.absolutePathString(),
                width = widthField.value as Int,
                height = heightField.value as Int
            )
        } catch (e: IOException) {
            JOptionPane.showMessageDialog(
                this,
                "Error: ${e.message}",
                title,
                JOptionPane.ERROR_MESSAGE
            )
        }
        return true
    }

    private inner class OkAction : AbstractAction("base.action.ok") {
        override fun execute(event: ActionEvent) {
            if (validateInput()) {
                val path = Path.of(directorySelectionField.path, fileNameField.text)
                if (export(path)) {
                    closeHandler()
                    JOptionPane.showConfirmDialog(
                        Frame.getFrames()[0],
                        Translations.getString("draw.image.exported.msg", path.toAbsolutePath()),
                        title,
                        JOptionPane.DEFAULT_OPTION,
                        JOptionPane.INFORMATION_MESSAGE
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