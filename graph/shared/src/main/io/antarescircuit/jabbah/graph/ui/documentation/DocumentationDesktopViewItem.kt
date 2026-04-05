package io.antarescircuit.jabbah.graph.ui.documentation

import io.antarescircuit.jabbah.base.ui.AbstractUIController
import io.antarescircuit.jabbah.base.ui.UIView
import io.antarescircuit.jabbah.graph.MetaGraph
import io.antarescircuit.jabbah.graph.model.Document
import io.antarescircuit.jabbah.graph.ui.desktop.GraphDesktopViewItem

interface DocumentationDesktopViewItem : UIView, GraphDesktopViewItem

/**
 * Displays the document [Document] of a [MetaGraph] in preview-only mode as a [GraphDesktopViewItem].
 */
class DocumentationDesktopViewItemController(
    documentation: Document,
) : AbstractUIController<DocumentationDesktopViewItem>() {

    val viewerController = DocumentationViewerController(documentation)
}