package ch.scorpion.jabbah.graph.ui.documentation

import ch.scorpion.jabbah.base.ui.AbstractUIController
import ch.scorpion.jabbah.base.ui.UIView
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.model.Document
import ch.scorpion.jabbah.graph.ui.desktop.GraphDesktopViewItem

interface DocumentationDesktopViewItem : UIView, GraphDesktopViewItem

/**
 * Displays the document [Document] of a [MetaGraph] in preview-only mode as a [GraphDesktopViewItem].
 */
class DocumentationDesktopViewItemController(
    documentation: Document,
) : AbstractUIController<DocumentationDesktopViewItem>() {

    val viewerController = DocumentationViewerController(documentation)
}