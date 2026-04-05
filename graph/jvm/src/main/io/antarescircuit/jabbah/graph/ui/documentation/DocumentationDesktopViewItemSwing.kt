package io.antarescircuit.jabbah.graph.ui.documentation

import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.draw.CloseViewRequest
import io.antarescircuit.jabbah.draw.graphics.Color
import io.antarescircuit.jabbah.edit.DrawingView
import io.antarescircuit.jabbah.edit.DrawingViewContent
import io.antarescircuit.jabbah.graph.AbstractTitledGraphDesktopViewItemSwing
import io.antarescircuit.jabbah.graph.model.Document
import io.antarescircuit.jabbah.graph.ui.desktop.AbstractGraphDesktopViewItemSwing
import io.antarescircuit.jabbah.graph.ui.desktop.GraphDesktopViewItem
import io.antarescircuit.jabbah.graph.view.GraphView
import java.awt.BorderLayout
import javax.swing.JPanel

class DocumentationDesktopViewItemSwing(
    documentation: Document,
    isRoot: Boolean = false,
    private val metaGraphName: String
) : AbstractTitledGraphDesktopViewItemSwing(
    createTitleText(metaGraphName),
    JPanel(),
    null,
    isRoot = isRoot
), DocumentationDesktopViewItem {

    companion object {
        fun createTitleText(metaGraphName: String): String =
            Translations.getString("graph.documentation.desktopItem.title", metaGraphName)
    }

    private val controller = DocumentationDesktopViewItemController(documentation)

    private val documentationPanel = DocumentationViewerViewSwing(controller.viewerController)

    init {
        controller.view = this
        buildUI()
    }

    private fun buildUI() {
        with(contentPanel) {
            layout = BorderLayout()
            add(documentationPanel, BorderLayout.CENTER)
        }
    }

    override fun dispose() {}

    /** ---- [AbstractTitledGraphDesktopViewItemSwing] */

    override fun createHeaderText(): String = createTitleText(metaGraphName)

    /** ---- [GraphDesktopViewItem] */

    override val drawingView: DrawingView<GraphView>? get() = null

    override fun displays(content: Any?): Boolean =
        content === controller.viewerController.documentation

    override fun disposeItem() {
        controller.dispose()
    }

    override fun findContent(condition: (DrawingViewContent<GraphView>) -> Boolean): DrawingViewContent<*>? = null

    override fun createCloseRequest(): Any = CloseViewRequest(this)

    /** ---- [AbstractGraphDesktopViewItemSwing] */

    override fun addContextColorBorder(color: Color) { }

    override fun removeContextColorBorder() { }
}