package ch.scorpion.jabbah.graph.documentation

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.ui.TitleBar
import java.awt.BorderLayout
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JSplitPane
import javax.swing.JTextArea
import javax.swing.SwingConstants

class DocumentationPanelSwing : JPanel() {

    private val textArea = JTextArea()
    private val previewPanel = JPanel()
    private val splitPane = JSplitPane(JSplitPane.HORIZONTAL_SPLIT, JScrollPane(textArea), previewPanel)

    init {
        layout = BorderLayout()
        buildUI()
    }

    private fun buildUI() {
        add(TitleBar(Translations.getString("graph.documentation.title")), BorderLayout.NORTH)
        add(splitPane, BorderLayout.CENTER)
        textArea.lineWrap = true
        textArea.wrapStyleWord = true

        previewPanel.layout = BorderLayout()
        previewPanel.add(JLabel("TODO: Preview", null, SwingConstants.CENTER), BorderLayout.CENTER)
    }
}