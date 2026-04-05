package io.antarescircuit.jabbah.graph.poster

import io.antarescircuit.jabbah.base.geom.Margin
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.base.ui.AbstractUIController
import io.antarescircuit.jabbah.base.ui.UIView
import io.antarescircuit.jabbah.draw.ZoomStrategy
import io.antarescircuit.jabbah.draw.drawable.*
import io.antarescircuit.jabbah.draw.style.DrawStyleModule
import io.antarescircuit.jabbah.edit.model.DrawingImpl
import io.antarescircuit.jabbah.edit.model.text.description.Name
import io.antarescircuit.jabbah.edit.module.EditModule
import io.antarescircuit.jabbah.graph.library.ContainerLibraryElement
import io.antarescircuit.jabbah.graph.library.Library
import javax.swing.SwingUtilities

interface PosterViewerView : UIView {

    /**
     * Notifies this [PosterViewerView] that it's [PosterViewerController] properties like [Page] and [Resolution]
     * have changed.
     */
    fun notifyPropertiesChanged()
}

class PosterViewerController(
    private val library: Library
) : AbstractUIController<PosterViewerView>() {

    companion object {

        private const val PROP_PAGE_SIZE = "graph.poster.pageSize"
        private const val PROP_PAGE_ORIENTATION = "graph.poster.pageOrientation"
        private const val PROP_RESOLUTION = "graph.poster.resolution"
        private const val PROP_BORDERS = "graph.poster.borders"

        private const val GAP = 2
        private val PAGE_MARGIN = Margin.allOf(5)
        private val ELEM_MARGIN = Margin(5, 2, 2, 2)

        private fun loadPageFromSettingsOrDefault(): Page {
            val size = PageSize.predefinedWithName(BaseModule.settings.getString(PROP_PAGE_SIZE, PageSize.A4.customName))
            val orientation = PageOrientation.withName(BaseModule.settings.getString(PROP_PAGE_ORIENTATION, PageOrientation.LANDSCAPE.customName))
            return Page(size, orientation, PAGE_MARGIN)
        }

        private fun loadResolutionFromSettingsOrDefault(): Resolution =
            Resolution.predefinedWithName(BaseModule.settings.getString(PROP_RESOLUTION, Resolution.DPI_300.name))

        private fun loadBorderFromSettingsOrDefaults(): Boolean =
            BaseModule.settings.getBoolean(PROP_BORDERS, false)
    }

    val drawingView = EditModule.drawingViewFactory.create(
        DrawingImpl(Name("${library.name.value} poster")),
        null,
        false,
        "")

    var page: Page = loadPageFromSettingsOrDefault()
        set(value) {
            field = value
            updateLayout(false)
        }

    var resolution = loadResolutionFromSettingsOrDefault()
        set(value) {
            field = value
            updateLayout(false)
        }

    var drawElementBorder: Boolean = loadBorderFromSettingsOrDefaults()
        set(value) {
            field = value
            updateLayout(false)
        }

    private lateinit var pageView: PageView

    init {
        drawingView.editable = false
        drawingView.defaultZoomStrategy = ZoomStrategy.FIT
        drawingView.backgroundContainer.visible = true

        updateLayout()
    }

    override fun dispose() {
        super.dispose()
        BaseModule.settings.set(PROP_PAGE_SIZE, page.size.customName)
        BaseModule.settings.set(PROP_PAGE_ORIENTATION, page.orientation.customName)
        BaseModule.settings.set(PROP_RESOLUTION, resolution.name)
        BaseModule.settings.set(PROP_BORDERS, drawElementBorder)
    }

    private fun updateLayout(initialize: Boolean = true) {
        drawingView.drawing.clear()
        drawingView.backgroundContainer.clear()

        pageView = PageView(page, DrawStyleModule.styleProvider)
        drawingView.backgroundContainer.add(pageView)

        val elements = createPosterElements()
        elements.forEach { element -> drawingView.drawing.add(element) }
        PosterPacker(page, elements, GAP).pack()

        if (!initialize) {
            view.notifyPropertiesChanged()
        }

        SwingUtilities.invokeLater {
            drawingView.applyDefaultZoomStrategy()
        }
    }

    private fun createPosterElements(): Set<PosterElement> {
        val elements = mutableSetOf<PosterElement>()
        library
            .allLocalItems { it is ContainerLibraryElement }
            .map { it as ContainerLibraryElement }
            .forEach { element ->
                PosterElement(
                    library.libraryService.getMetaGraph(library, element),
                    drawBorder = drawElementBorder,
                    margin = ELEM_MARGIN
                ).also { elements.add(it) }
            }
        return elements
    }
}