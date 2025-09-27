package ch.scorpion.jabbah.graph.ui.container

import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.event.PropertyChangeListener
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Direction.*
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.base.geom.RectangularShape
import ch.scorpion.jabbah.base.invocation.InvocationHandler
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.ui.AbstractUIController
import ch.scorpion.jabbah.base.ui.UIView
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.View
import ch.scorpion.jabbah.draw.drawable.AbstractDrawable
import ch.scorpion.jabbah.draw.drawable.AbstractDrawableDrawer
import ch.scorpion.jabbah.draw.drawable.Locatable
import ch.scorpion.jabbah.draw.graphics.CompositeColor
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleType
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.SnapResult
import ch.scorpion.jabbah.graph.container.OriginIndicator
import ch.scorpion.jabbah.graph.library.CurrentLibraryEvent
import ch.scorpion.jabbah.graph.library.LibraryElement
import ch.scorpion.jabbah.graph.library.LibraryModule
import ch.scorpion.jabbah.graph.model.GraphElement
import ch.scorpion.jabbah.graph.ui.library.BasicLibraryTreeView
import ch.scorpion.jabbah.graph.ui.library.BasicLibraryTreeViewController
import ch.scorpion.jabbah.graph.ui.library.LibrarySelectionChangedEvent
import ch.scorpion.jabbah.graph.ui.library.LibraryTreeViewType

interface SymbolComparatorView : UIView {
    fun reset()
    fun refresh()
}

class SymbolComparatorController(
    private val drawingView: DrawingView<Drawing<Component>>,
    private val eventBus: EventBus = BaseModule.eventBus
) : AbstractUIController<SymbolComparatorView>() {

    companion object {

        private val LOG by logger(SymbolComparatorController::class)

        /** The inset (in view coordinates) between teh compare symbol and the view border.*/
        private const val INSET = 50

        private const val DIST = 70

        /** The alpha channel value for grayed-out rendering of [comparisonSymbol]. */
        private const val ALPHA_VALUE = 40
    }

    var active: Boolean = false
        set(value) {
            if (field != value) {
                LOG.debug("Setting active=$value")
                field = value
                updateState()
            }
        }

    val refreshAction: Action = RefreshAction()

    val libraryTreeViewController = BasicLibraryTreeViewController<BasicLibraryTreeView>(
        LibraryTreeViewType.Main,
        null
    )

    private val viewPropertyListener = PropertyChangeListener<Any> { e ->
        when (e.name) {
            DrawingView.PROP_DRAWING -> clear()
        }
    }

    private val currentLibraryHandler: EventHandler<CurrentLibraryEvent> = { updateState() }

    private val librarySelectionChangedHandler: EventHandler<LibrarySelectionChangedEvent> = {
        if (it.controller === libraryTreeViewController && active) {
            handle(it)
        }
    }

    /** The currently displayed 'ghost' symbol of the selected [libraryElement]. */
    private var comparisonSymbol: ComparisonSymbol? = null

    private val comparisonSymbolDrawer by lazy { GrayingDrawableDrawer() }

    /** Used for snapping to the grid while placing [comparisonSymbol]. */
    private val snapResult = SnapResult()

    //private data class CacheEntry(val bbox: RectangularShape, val symbol: Component)

    /** Maps a [LibraryElement] to the instantiated [comparisonSymbol] and its original bounding box. */
    private val cache: MutableMap<LibraryElement, ComparisonSymbol> = mutableMapOf()

    /** The direction relative to the [View] where the [comparisonSymbol] is displayed.*/
    var direction: Direction = EAST
        set(value) {
            if (field != value) {
                field = value
                if (libraryElement != null) {
                    cache[libraryElement!!]?.let {
                        placeNearby(it)
                    }
                }
            }
        }

    /** The [LibraryElement] whose symbol is set in [comparisonSymbol]. */
    var libraryElement: LibraryElement? = null
        set(value) {
            if (field != value) {
                field = value
                handleContainerLibraryElementChanged()
            }
        }

    init {
        drawingView.addPropertyChangeListener(viewPropertyListener)
        eventBus.register(CurrentLibraryEvent::class, currentLibraryHandler)
        eventBus.register(LibrarySelectionChangedEvent::class, librarySelectionChangedHandler)
    }

    override fun dispose() {
        super.dispose()
        eventBus.unregister(currentLibraryHandler)
        eventBus.unregister(librarySelectionChangedHandler)
    }

    private fun clear() {
        view.reset()
    }

    private fun updateState() {
        if (active) {
            libraryTreeViewController.library = LibraryModule.libraryHolder.l
        } else {
            hideComparisonSymbol()
        }
    }

    private fun handle(@Suppress("unused") event: LibrarySelectionChangedEvent) {
        libraryElement = if (libraryTreeViewController.selectedItem is LibraryElement) {
            libraryTreeViewController.selectedItem as LibraryElement
        } else {
            null
        }
    }

    private fun hideComparisonSymbol() {
        if (comparisonSymbol != null) {
            drawingView.animationContainer.remove(comparisonSymbol!!)
            comparisonSymbol = null
        }
    }

    private fun handleContainerLibraryElementChanged() {
        drawingView.animationContainer.setDrawableDrawer(comparisonSymbolDrawer)

        hideComparisonSymbol()

        libraryElement?.let {
            val entry = cache[it]
            if (entry == null) {
                InvocationHandler.invoke {
                    try {
                        val component = it.getNewInstance<GraphElement>()
                        //val entry = CacheEntry(Rectangle2D(component.boundingBox), component)
                        val entry = ComparisonSymbol(component)
                        cache[it] = entry
                        updateSelection(entry)
                    } catch (e: Throwable) {
                        // Ignore
                    }
                }
            } else {
                updateSelection(entry)
            }
        }

        drawingView.drawing.validate()
    }

    private fun updateSelection(entry: ComparisonSymbol) {
        comparisonSymbol = entry
        placeNearby(entry)
        drawingView.animationContainer.add(comparisonSymbol!!)
        drawingView.drawing.validate()
    }

    private fun placeNearby(entry: ComparisonSymbol) {
        if (comparisonSymbol == null) {
            return
        }

        val origBBox = entry.boundingBox
        val bbox = drawingView.drawing.boundingBox

        when (direction) {
            EAST -> placeAt(bbox.maxX + DIST - origBBox.minX, bbox.minY - origBBox.minY)
            NORTH -> placeAt(bbox.minX - origBBox.minX, bbox.minY - INSET - origBBox.height - origBBox.minY)
            WEST -> placeAt(bbox.minX - DIST - origBBox.width - origBBox.minX, bbox.minY - origBBox.minY)
            SOUTH -> placeAt(bbox.minX - origBBox.minX, bbox.maxY + INSET - origBBox.minY)
        }

        drawingView.drawing.validate()
    }

    /** Places [comparisonSymbol] to the specified model coordinates while snapping to the grid.*/
    private fun placeAt(x: Double, y: Double) {
        snapResult.reset()
        drawingView.grid.snap(x, y, snapResult)
        comparisonSymbol!!.location = Point2D(x + snapResult.dx, y + snapResult.dy)
    }

    private inner class GrayingDrawableDrawer : AbstractDrawableDrawer<Drawable>() {

        private val color = CompositeColor(
            DrawStyleModule.styleProvider.getStyle(StyleType.FIGURE).color.foregroundColor.withAlpha(ALPHA_VALUE),
            DrawStyleModule.styleProvider.getStyle(StyleType.FIGURE).color.backgroundColor.withAlpha(ALPHA_VALUE)
        )

        override fun process(context: DrawContext, drawable: Drawable) {
            val oldUseContextColor = context.useContextColors

            if (drawable === comparisonSymbol) {
                context.useContextColors = true
                context.color = color
            }
            drawable.draw(context)
            nextProcessor(context, drawable)

            context.useContextColors = oldUseContextColor
        }
    }

    private inner class RefreshAction : AbstractAction("graph.container.symbolComparison.refresh", "/img/refresh.png") {
        override fun execute(event: ActionEvent) {
            view.refresh()
            cache.clear()
        }
    }

    private class ComparisonSymbol(
        private val component: Component
    ) : AbstractDrawable(), Locatable {

        companion object {
            private val ORIGIN_INDICATOR = OriginIndicator()
        }

        override val boundingBox: RectangularShape = Rectangle2D(component.boundingBox).add(ORIGIN_INDICATOR.boundingBox)

        override var location: Point2D = Point2D.ZERO
            set(value) {
                invalidate()
                field = value
                invalidate()
                update()
            }

        override fun draw(context: DrawContext) {
            context.translated(location.x, location.y) {
                component.draw(context)
                ORIGIN_INDICATOR.draw(context)
            }
        }

        override fun contains(x: Double, y: Double): Boolean = boundingBox.contains(x, y)
    }
}