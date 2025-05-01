package ch.scorpion.jabbah.graph.ui.documentation

import ch.scorpion.jabbah.graph.documentation.DocumentationPanelController
import ch.scorpion.jabbah.graph.documentation.DocumentationPanelView
import dev.mokkery.MockMode
import dev.mokkery.mock

class DocumentationPanelViewMockBuilder(private val controller: DocumentationPanelController) {

    private val documentationPanelView = mock<DocumentationPanelView>(MockMode.autofill)

    init {
        controller.view = documentationPanelView
    }

    fun build(): DocumentationPanelView = documentationPanelView
}