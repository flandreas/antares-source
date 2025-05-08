package ch.scorpion.jabbah.graph.ui.documentation

import ch.scorpion.jabbah.base.ui.AbstractUIController
import ch.scorpion.jabbah.base.ui.UIView
import ch.scorpion.jabbah.graph.model.Document
import ch.scorpion.jabbah.graph.MetaGraph

interface DocumentationViewerView : UIView

/**
 * Read-only viewer of the documentation [Document] of a [MetaGraph].
 */
class DocumentationViewerController(
    val documentation: Document
) : AbstractUIController<DocumentationViewerView>() {
}