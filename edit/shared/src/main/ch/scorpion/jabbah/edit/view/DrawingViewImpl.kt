package ch.scorpion.jabbah.edit.view

import ch.scorpion.jabbah.animation.AnimationModule
import ch.scorpion.jabbah.animation.Animator
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.*
import ch.scorpion.jabbah.draw.container.DrawableContainerImpl
import ch.scorpion.jabbah.draw.drawable.AbstractDrawableDrawer
import ch.scorpion.jabbah.draw.drawable.DrawableDrawer
import ch.scorpion.jabbah.draw.drawable.Unzoomable
import ch.scorpion.jabbah.draw.container.UnzoomableContainer
import ch.scorpion.jabbah.draw.view.ViewImpl
import ch.scorpion.jabbah.edit.*
import ch.scorpion.jabbah.edit.DrawingView.Companion.PROP_EDITABLE
import ch.scorpion.jabbah.edit.DrawingView.Companion.PROP_SHOW_GRID
import ch.scorpion.jabbah.edit.model.ComponentMessage
import ch.scorpion.jabbah.edit.highlight.EditHighlightModule
import ch.scorpion.jabbah.edit.select.EditSelectModule
import ch.scorpion.jabbah.edit.select.UnzoomableSelectionModel
import ch.scorpion.jabbah.edit.snap.GridImpl
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.RectangularShape
import ch.scorpion.jabbah.base.geom.AffineTransform
import ch.scorpion.jabbah.draw.Canvas
import ch.scorpion.jabbah.base.System

/**
 * An implementation of a [View] for displaying and editing [Drawing]s.
 *
 * [DrawingViewImpl] has a fixed set of [DrawableContainer]s that are arranged on top of each other to form
 * a stack of view slides. Therefore, client classes cannot add or remove [Drawable]s by themselves.
 */
class DrawingViewImpl<T: Drawing<Component>>(
        drawing: T,
        canvas: Canvas,
        transformFactory: () -> AffineTransform,
        private val selectionManagerFactory: SelectionManagerFactory,
        private val highlighterFactory: HighlighterFactory,
        eventBus: EventBus,
        animator: Animator
) : ViewImpl<EditInputEventContext>(canvas, transformFactory), DrawingView<T> {

    constructor(drawing: T, canvas: Canvas): this(
        drawing,
        canvas,
        { System.get().createAffineTransform() },
        EditSelectModule.selectionManagerFactory,
        EditHighlightModule.highlighterFactory,
        BaseModule.eventBus,
        AnimationModule.animator)

    /** The [DrawableDrawer] used for drawing the [Drawing].*/
    private var drawableDrawer: DrawableDrawer<Component> = DrawingDrawer()

    /** Displays [ComponentMessage]s from [Component]s of the current [Drawing]. */
    private val componentMessageDisplayer = ComponentMessageDisplayer(this, eventBus, animator)

    /** ---- [DrawingView] interface */

    override var content: DrawingViewContent<T> = createContent(drawing)
        set(value) {
            if (field === value) {
                return
            }
            val oldDrawing = field.drawing
            replaceContent(value)
            field = value
            firePropertyChange(DrawingView.PROP_DRAWING, oldDrawing, field.drawing)
        }

    override var editable: Boolean = true
        set(value) {
            if (value != field) {
                field = value
                showGridIfNeeded()
                firePropertyChange(PROP_EDITABLE, !field, field)
            }
        }

    override val selectionManager get() = content.selectionManager

    override val highlighter get() = content.highlighter

    override val ghostContainer get() = content.ghostContainer

    override val animationContainer get() = content.animationContainer

    override val highlightContainer get() = content.highlightContainer

    override var drawing: T
        get() = content.drawing
        set(value) {
            content = createContent(value)
            value.setDrawableDrawer(drawableDrawer)
            applyDefaultZoomStrategy()
        }

    override var showGrid: Boolean = false
        set(value) {
            if (value != field) {
                field = value
                showGridIfNeeded()
                repaint()
                firePropertyChange(PROP_SHOW_GRID, !field, field)
            }
        }

    override val grid: Grid = GridImpl()

    override var defaultSelectionDrawingStrategy = SelectionDrawingStrategy.REPLACE

    init {
        setupContent()
        showGrid = true
    }

    override var dropComponent: Component? = null
        private set

    override fun createContent(drawing: T): DrawingViewContent<T> {
        return DrawingViewContentImpl(drawing, selectionManagerFactory.create(this), highlighterFactory.create(this))
    }

    override fun setDropComponent(component: Component?, location: Point2D?) {
        if (component != null) {
            if (dropComponent != null) {
                dropComponent!!.location = location!!.copy()
            } else {
                dropComponent = component
                animationContainer.add(component)
            }
            dropComponent!!.validate()
        } else {
            if (dropComponent != null) {
                animationContainer.remove(dropComponent!!)
                drawing.validate()
                dropComponent = null
            }
        }
    }

    override fun addDrawableDrawer(drawableDrawer: DrawableDrawer<Component>) {
        drawableDrawer.successor = this.drawableDrawer
        this.drawableDrawer = drawableDrawer
        drawing.setDrawableDrawer(this.drawableDrawer)
    }

    override fun getComponentSelectionDrawingStrategy(component: Component): SelectionDrawingStrategy {
        return component.preferredSelectionDrawingStrategy ?: defaultSelectionDrawingStrategy
    }

    /** ---- [View] interface */

    override val contentBounds: RectangularShape get() = drawing.boundingBox

    override fun removeDrawable(drawable: Drawable) {
        // DrawingViewImpl has a fixed set of DrawableContainers
        throw ch.scorpion.jabbah.base.exception.UnsupportedOperationException("Clients cannot remove Drawable from DrawingViewImpl")
    }

    override fun addDrawable(drawable: Drawable) {
        // DrawingViewImpl has a fixed set of DrawableContainers
        throw ch.scorpion.jabbah.base.exception.UnsupportedOperationException("Clients cannot add Drawable to DrawingViewImpl")
    }

    /** ---- [DrawingViewImpl] */

    fun dispose() {
        componentMessageDisplayer.dispose()
    }

    private fun setupContent() {
        super.addDrawable(ghostContainer)
        super.addDrawable(animationContainer)
        super.addDrawable(content.zoomableSelectionContainerFor(SelectionDrawingStrategy.ABOVE)!!)
        super.addDrawable(content.unzoomableSelectionContainerFor(SelectionDrawingStrategy.ABOVE)!!)
        super.addDrawable(content.zoomableSelectionContainerFor(SelectionDrawingStrategy.REPLACE)!!)
        super.addDrawable(drawing)
        super.addDrawable(content.zoomableSelectionContainerFor(SelectionDrawingStrategy.BELOW)!!)
        super.addDrawable(highlightContainer)
    }

    private fun replaceContent(newContent: DrawingViewContent<T>) {
        replaceDrawable(content.ghostContainer, newContent.ghostContainer)
        replaceDrawable(content.animationContainer, newContent.animationContainer)
        replaceDrawable(content.zoomableSelectionContainerFor(SelectionDrawingStrategy.ABOVE)!!, newContent.zoomableSelectionContainerFor(SelectionDrawingStrategy.ABOVE)!!)
        replaceDrawable(content.unzoomableSelectionContainerFor(SelectionDrawingStrategy.ABOVE)!!, newContent.unzoomableSelectionContainerFor(SelectionDrawingStrategy.ABOVE)!!)
        replaceDrawable(content.zoomableSelectionContainerFor(SelectionDrawingStrategy.REPLACE)!!, newContent.zoomableSelectionContainerFor(SelectionDrawingStrategy.REPLACE)!!)
        replaceDrawable(content.drawing, newContent.drawing)
        replaceDrawable(content.zoomableSelectionContainerFor(SelectionDrawingStrategy.BELOW)!!, newContent.zoomableSelectionContainerFor(SelectionDrawingStrategy.BELOW)!!)
        replaceDrawable(content.highlightContainer, newContent.highlightContainer)
        repaint()
    }

    /** Adds or removed the [Grid] depending on the [showGrid] and [editable] properties.*/
    private fun showGridIfNeeded() {
        val gridNeeded = showGrid && editable
        if (gridNeeded) {
            if (!containsDrawable(grid)) {
                super.addDrawable(grid)
            }
        } else {
            super.removeDrawable(grid)
        }
        repaint()
    }

    /**
     * The [DrawableDrawer] used for drawing the [Drawing]. Implements the drawing behaviour used for the
     * different [SelectionDrawingStrategies][SelectionDrawingStrategy].
     */
    private inner class DrawingDrawer : AbstractDrawableDrawer<Component>() {
        override fun process(context: DrawContext, drawable: Component) {
            if (!selectionManager.isSelected(drawable) || getComponentSelectionDrawingStrategy(drawable) != SelectionDrawingStrategy.REPLACE) {
                drawable.draw(context)
            }
            processDone(context, drawable)
        }
    }
}
