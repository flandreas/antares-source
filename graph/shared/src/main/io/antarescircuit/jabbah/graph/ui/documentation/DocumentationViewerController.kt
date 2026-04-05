package io.antarescircuit.jabbah.graph.ui.documentation

import io.antarescircuit.jabbah.base.ui.AbstractUIController
import io.antarescircuit.jabbah.base.ui.UIView
import io.antarescircuit.jabbah.graph.model.Document
import io.antarescircuit.jabbah.graph.MetaGraph

interface DocumentationViewerView : UIView

/**
 * Read-only viewer of the documentation [Document] of a [MetaGraph].
 */
class DocumentationViewerController(
    val documentation: Document
) : AbstractUIController<DocumentationViewerView>() {
}