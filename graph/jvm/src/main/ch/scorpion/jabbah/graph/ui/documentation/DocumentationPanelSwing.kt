package ch.scorpion.jabbah.graph.ui.documentation

import ch.scorpion.jabbah.app.Application
import ch.scorpion.jabbah.app.ToolBar
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.ui.TitleBar
import ch.scorpion.jabbah.graph.documentation.DocumentationPanelController
import ch.scorpion.jabbah.graph.documentation.DocumentationPanelView
import org.jmarkdownviewer.jmdviewer.HtmlPane
import java.awt.BorderLayout
import java.awt.event.FocusAdapter
import java.awt.event.FocusEvent
import java.io.File
import java.io.FileOutputStream
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JSplitPane
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import org.fife.ui.rsyntaxtextarea.*
import org.fife.ui.rtextarea.RTextScrollPane

class DocumentationPanelSwing(
    private val controller: DocumentationPanelController,
    application: Application
) : JPanel(), DocumentationPanelView {

    companion object {
        private val LOG by logger(DocumentationPanelSwing::class)
    }

    private val textArea = RSyntaxTextArea(20, 60).apply {
        syntaxEditingStyle = SyntaxConstants.SYNTAX_STYLE_MARKDOWN
        isCodeFoldingEnabled = true
    }
    private val previewPane = HtmlPane()
    private val splitPane = JSplitPane(
        JSplitPane.HORIZONTAL_SPLIT,
        RTextScrollPane(textArea),
        JScrollPane(previewPane)
    )

    private val updateListener = UpdateListener()

    val toolbars: List<ToolBar> = listOf(buildToolBar(application))

    private var viewDataChanged: Boolean = false

    override val viewText: String get() = textArea.text

    init {
        controller.view = this
        layout = BorderLayout()
        buildUI()

        textArea.document.addDocumentListener(updateListener)

        textArea.addFocusListener(object : FocusAdapter() {
            override fun focusLost(e: FocusEvent?) {
                controller.documentChangeEnd()
            }
        })

        updateEditability()
    }

    private fun handleViewDataChanged() {
        if (!viewDataChanged) {
            viewDataChanged = true
            controller.documentChangeBegin()
        }
    }

    /** ---- [DocumentationPanelView] interface */

    override fun dispose() {}

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
        val file = storeInTempFile()
        previewPane.load(file)
        file.delete()
    }

    /** ---- [DocumentationPanelSwing] */

    private fun buildToolBar(application: Application): ToolBar {
        val toolbar = ToolBar()
        toolbar.addAction(application.controller.saveAction)
        toolbar.addAction(controller.refreshAction)
        return toolbar
    }

    private fun buildUI() {
        add(TitleBar(Translations.getString("graph.documentation.title")), BorderLayout.NORTH)
        add(splitPane, BorderLayout.CENTER)
        textArea.lineWrap = true
        textArea.wrapStyleWord = true
    }

    private fun storeInTempFile(): File {
        val file = File.createTempFile("antares-doc", ".md")
        FileOutputStream(file).use {
            it.write(textArea.text.toByteArray())
            it.flush()
        }
        return file
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