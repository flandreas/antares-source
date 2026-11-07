package io.antarescircuit.jabbah.graph.ui.documentation

import io.antarescircuit.jabbah.base.StringUtils
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.graph.model.Document
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.JFrame

class DocumentationViewerFrameSwing(
    applicationName: String,
    documentation: Document,
    metaGraphName: String
) : JFrame() {

    private val controller = DocumentationViewerController(documentation)
    private val viewer = DocumentationViewerViewSwing(controller)

    init {
        buildUI()
        addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent?) {
                controller.dispose()
            }
        })
        pack()
        setLocationRelativeTo(getFrames()[0])

        title = if (StringUtils.isNotBlank(applicationName)) {
            "$applicationName - ${Translations.getString("graph.documentation.desktopItem.title", metaGraphName)}"
        } else {
            Translations.getString("graph.documentation.desktopItem.title", metaGraphName)
        }

        isVisible = true
    }

    private fun buildUI() {
        viewer.preferredSize = Dimension(1000, 800)
        layout = BorderLayout()
        add(viewer, BorderLayout.CENTER)
    }
}