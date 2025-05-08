package ch.scorpion.jabbah.graph.ui.documentation

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.draw.CloseViewRequest
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.DrawingViewContent
import ch.scorpion.jabbah.graph.AbstractTitledGraphDesktopViewItemSwing
import ch.scorpion.jabbah.graph.model.Document
import ch.scorpion.jabbah.graph.ui.desktop.AbstractGraphDesktopViewItemSwing
import ch.scorpion.jabbah.graph.ui.desktop.GraphDesktopViewItem
import ch.scorpion.jabbah.graph.view.GraphView
import java.awt.BorderLayout
import javax.swing.JPanel

class DocumentationDesktopViewItemSwing(
    documentation: Document,
    isRoot: Boolean = false,
) : AbstractTitledGraphDesktopViewItemSwing(
    Translations.getString("graph.documentation.title"),
    JPanel(),
    null,
    isRoot = isRoot
), DocumentationDesktopViewItem {

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

    override fun createHeaderText(): String = Translations.getString("graph.documentation.title")

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