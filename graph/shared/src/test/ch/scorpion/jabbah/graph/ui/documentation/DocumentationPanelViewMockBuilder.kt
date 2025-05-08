package ch.scorpion.jabbah.graph.ui.documentation

import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock

class DocumentationPanelViewMockBuilder(private val controller: DocumentationPanelController) {

    private val documentationPanelView = mock<DocumentationPanelView>(MockMode.autofill)

    init {
        controller.view = documentationPanelView
    }

    fun withText(text: String): DocumentationPanelViewMockBuilder {
        every { documentationPanelView.viewText } returns text
        return this
    }

    fun build(): DocumentationPanelView = documentationPanelView
}