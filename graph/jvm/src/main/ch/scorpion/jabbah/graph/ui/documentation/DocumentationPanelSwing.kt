package ch.scorpion.jabbah.graph.ui.documentation

import ch.scorpion.jabbah.app.Application
import ch.scorpion.jabbah.app.ToolBar
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.ui.TitleBar
import ch.scorpion.jabbah.graph.documentation.DocumentationPanelController
import ch.scorpion.jabbah.graph.documentation.DocumentationPanelView
import java.awt.BorderLayout
import java.awt.event.FocusAdapter
import java.awt.event.FocusEvent
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

class DocumentationPanelSwing(
    private val controller: DocumentationPanelController,
    application: Application
) : JPanel(), DocumentationPanelView {

    companion object {
        private val LOG by logger(DocumentationPanelSwing::class)
    }

    private val textArea = JTextArea()
    private val previewPanel = JPanel()
    private val splitPane = JSplitPane(JSplitPane.HORIZONTAL_SPLIT, JScrollPane(textArea), previewPanel)

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
    }

    /** ---- [DocumentationPanelSwing] */

    private fun buildToolBar(application: Application): ToolBar {
        val toolbar = ToolBar()
        toolbar.addAction(application.controller.saveAction)
        return toolbar
    }

    private fun buildUI() {
        add(TitleBar(Translations.getString("graph.documentation.title")), BorderLayout.NORTH)
        add(splitPane, BorderLayout.CENTER)
        textArea.lineWrap = true
        textArea.wrapStyleWord = true

        previewPanel.layout = BorderLayout()
        previewPanel.add(JLabel("TODO: Preview", null, SwingConstants.CENTER), BorderLayout.CENTER)
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