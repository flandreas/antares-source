package io.antarescircuit.jabbah.graph.ui.documentation

import java.awt.BorderLayout

class DocumentationViewerViewSwing(
    private val controller: DocumentationViewerController
) : AbstractDocumentationPanelSwing(), DocumentationViewerView {

    init {
        controller.view = this
        buildUI()
        refreshPreviewPane(controller.documentation.text)
    }

    override fun dispose() {}

    private fun buildUI() {
        layout = BorderLayout()
        add(previewScrollPane, BorderLayout.CENTER)
    }
}