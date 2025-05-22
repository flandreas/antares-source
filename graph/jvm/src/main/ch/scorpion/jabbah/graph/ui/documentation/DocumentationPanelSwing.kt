package ch.scorpion.jabbah.graph.ui.documentation

import ch.scorpion.jabbah.app.ToolBar
import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.ActionWrapperSwing
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.swing.UiUtil
import ch.scorpion.jabbah.base.ui.TitleBar
import ch.scorpion.jabbah.base.ui.UI
import ch.scorpion.jabbah.graph.ui.documentation.DocumentationPanelViewMode.EditAndPreview
import ch.scorpion.jabbah.graph.ui.documentation.DocumentationPanelViewMode.EditOnly
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea
import org.fife.ui.rsyntaxtextarea.SyntaxConstants
import org.fife.ui.rsyntaxtextarea.Theme
import org.fife.ui.rtextarea.RTextScrollPane
import java.awt.BorderLayout
import java.awt.event.FocusAdapter
import java.awt.event.FocusEvent
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

class DocumentationPanelSwing(
    val controller: DocumentationPanelController,
    saveAction: Action? = null
) : AbstractDocumentationPanelSwing(), DocumentationPanelView {

    companion object {
        private val LOG by logger(DocumentationPanelSwing::class)
        private const val PROP_SPLIT_POS = "documentationPanel.splitPos"
    }

    private val contentPanel = JPanel(BorderLayout())

    private val textArea = RSyntaxTextArea(20, 80).apply {
        syntaxEditingStyle = SyntaxConstants.SYNTAX_STYLE_MARKDOWN
        isCodeFoldingEnabled = true
    }

    private val textAreaScrollPane = RTextScrollPane(textArea)

    private val splitLeftPanel = JPanel(BorderLayout())

    private val splitRightPanel = JPanel(BorderLayout())

    private val splitPane = JSplitPane(JSplitPane.HORIZONTAL_SPLIT, splitLeftPanel, splitRightPanel)

    private val updateListener = UpdateListener()

    private val documentTypeChooser = DocumentTypeChooser()

    val toolbars: List<ToolBar> = listOf(buildToolBar(saveAction))

    private var viewDataChanged: Boolean = false

    override val viewText: String get() = textArea.text

    init {
        controller.view = this
        layout = BorderLayout()
        buildUI()

        if (UI.isDark) {
            loadDarkRSTATheme()
        }

        textArea.document.addDocumentListener(updateListener)

        textArea.addFocusListener(object : FocusAdapter() {
            override fun focusLost(e: FocusEvent?) {
                controller.documentChangeEnd()
            }
        })

        updateEditability()
    }

    fun notifyActivated() {
        if (controller.mode == EditOnly || controller.mode == EditAndPreview) {
            SwingUtilities.invokeLater {
                textArea.caretPosition = 0
                textArea.requestFocusInWindow()
            }
        }
    }

    private fun loadDarkRSTATheme() {
        try {
            Theme.load(javaClass.getResourceAsStream("/org/fife/ui/rsyntaxtextarea/themes/dark.xml")).apply {
                apply(textArea)
            }
        } catch (e: Exception) {
            LOG.error("Failed to load RSTA dark theme", e)
        }
    }

    private fun handleViewDataChanged() {
        if (!viewDataChanged) {
            viewDataChanged = true
            controller.documentChangeBegin()
        }
    }

    /** ---- [DocumentationPanelView] interface */

    override fun dispose() {
        BaseModule.settings.set(PROP_SPLIT_POS, splitPane.dividerLocation)
    }

    override fun notifyModelDataChanged() {
        textArea.document.removeDocumentListener(updateListener)
        textArea.text = controller.text
        viewDataChanged = false
        textArea.document.addDocumentListener(updateListener)
        refreshPreview()
    }

    override fun notifyEditabilityChanged() {
        updateEditability()
    }

    override fun refreshPreview() {
        refreshPreviewPane(textArea.text)
    }

    override fun notifyModeChanged() {
        updateUIForMode()
    }

    /** ---- [DocumentationPanelSwing] */

    private fun buildToolBar(saveAction: Action?): ToolBar {
        val toolbar = ToolBar()
        toolbar.addSeparator()
        documentTypeChooser.alignmentX = LEFT_ALIGNMENT
        toolbar.add(documentTypeChooser)

        if (saveAction != null) {
            toolbar.addAction(saveAction)
        }
        toolbar.addAction(controller.refreshAction)
        toolbar.addSeparator()

        val buttonGroup = ButtonGroup()
        toolbar.add(createToolBarButton(controller.editOnlyAction).also { buttonGroup.add(it) })
        toolbar.add(createToolBarButton(controller.editAndPreviewAction).also { buttonGroup.add(it) })
        toolbar.add(createToolBarButton(controller.previewOnlyAction).also { buttonGroup.add(it) })
        return toolbar
    }

    private fun createToolBarButton(action: Action): JToggleButton {
        val button = JToggleButton(ActionWrapperSwing(action))
        action.imagePath?.let { button.icon = UiUtil.themedIcon(it) }
        button.text = null
        button.toolTipText = action.name
        return button
    }

    private fun buildUI() {
        add(TitleBar(Translations.getString("graph.documentation.title")), BorderLayout.NORTH)
        add(contentPanel, BorderLayout.CENTER)
        updateUIForMode()
        textArea.lineWrap = true
        textArea.wrapStyleWord = true
        splitPane.dividerLocation = BaseModule.settings.getInt(PROP_SPLIT_POS, -1)
    }

    private fun updateUIForMode() {
        contentPanel.removeAll()
        when (controller.mode) {
            EditOnly -> {
                contentPanel.add(textAreaScrollPane, BorderLayout.CENTER)
            }
            EditAndPreview -> {
                splitLeftPanel.add(textAreaScrollPane, BorderLayout.CENTER)
                splitRightPanel.add(previewScrollPane, BorderLayout.CENTER)
                contentPanel.add(splitPane, BorderLayout.CENTER)
            }
            DocumentationPanelViewMode.PreviewOnly -> {
                contentPanel.add(previewScrollPane, BorderLayout.CENTER)
            }
        }

        contentPanel.invalidate()
        contentPanel.validate()
        repaint()
    }

    private fun updateEditability() {
        textArea.isEditable = controller.editable
    }

    private inner class UpdateListener : DocumentListener {
        override fun insertUpdate(e: DocumentEvent?) {
            handleViewDataChanged()
        }

        override fun removeUpdate(e: DocumentEvent?) {
            handleViewDataChanged()
        }

        override fun changedUpdate(e: DocumentEvent?) {
            handleViewDataChanged()
        }
    }
}