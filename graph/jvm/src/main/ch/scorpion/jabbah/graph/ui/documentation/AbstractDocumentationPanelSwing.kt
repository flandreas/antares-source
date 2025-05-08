package ch.scorpion.jabbah.graph.ui.documentation

import ch.scorpion.jabbah.base.ui.UI
import org.jmarkdownviewer.jmdviewer.HtmlPane
import java.io.File
import java.io.FileOutputStream
import javax.swing.JPanel
import javax.swing.JScrollPane

abstract class AbstractDocumentationPanelSwing : JPanel() {

    protected val previewPane = HtmlPane(UI.isDark)

    protected val previewScrollPane = JScrollPane(previewPane)

    protected fun storeInTempFile(text: String): File {
        val file = File.createTempFile("antares-doc", ".md")
        FileOutputStream(file).use {
            it.write(text.toByteArray())
            it.flush()
        }
        return file
    }

    protected fun refreshPreviewPane(text: String) {
        val file = storeInTempFile(text)
        previewPane.load(file)
        file.delete()
    }
}