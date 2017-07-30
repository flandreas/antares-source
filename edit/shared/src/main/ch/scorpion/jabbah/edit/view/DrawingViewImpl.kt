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
        selectionManagerFactory: SelectionManagerFactory,
        highlighterFactory: HighlighterFactory,
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

    /** Holds a [DrawableContainer] for every supported [SelectionDrawingStrategy].*/
    private val selectionContainers = mutableMapOf<SelectionDrawingStrategy, DrawableContainer<SelectionModel<Component>>>()

    /** Used for managing [SelectionModel]s that are [Unzoomable]. */
    private val unzoomableSelectionContainers = mutableMapOf<SelectionDrawingStrategy, UnzoomableContainer<UnzoomableSelectionModel<Component>>>()

    /** The [DrawableDrawer] used for drawing the [Drawing].*/
    private var drawableDrawer: DrawableDrawer<Component> = DrawingDrawer()

    /** Displays [ComponentMessage]s from [Component]s of the current [Drawing]. */
    private val componentMessageDisplayer = ComponentMessageDisplayer(this, eventBus, animator)

    fun dispose() {
        componentMessageDisplayer.dispose()
    }

    /** ---- [DrawingView] interface */

    override var editable: Boolean = true
        set(value) {
            if (value != field) {
                field = value
                showGridIfNeeded()
                firePropertyChange(PROP_EDITABLE, !field, field)
            }
        }

    override val selectionManager: SelectionManager = selectionManagerFactory.create(this)

    override val highlighter: Highlighter = highlighterFactory.create(this)

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

    override val ghostContainer = UnzoomableContainer<Unzoomable>()

    override val animationContainer: DrawableContainer<Drawable>

    override val highlightContainer: DrawableContainer<Drawable>

    override var drawing: T = drawing
        set(value) {
            if (field === value) {
                return
            }
            val oldDrawing = this.drawing
            replaceDrawable(oldDrawing, value)
            field = value
            field.setDrawableDrawer(drawableDrawer)

            ghostContainer.clear()
            animationContainer.clear()
            highlightContainer.clear()

            applyDefaultZoomStrategy()

            firePropertyChange(DrawingView.PROP_DRAWING, oldDrawing, field)
        }

    override var dropComponent: Component? = null
        private set

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

    init {
        animationContainer = DrawableContainerImpl()
        highlightContainer = DrawableContainerImpl()

        selectionContainers.put(SelectionDrawingStrategy.ABOVE, DrawableContainerImpl<SelectionModel<Component>>())
        selectionContainers.put(SelectionDrawingStrategy.REPLACE, DrawableContainerImpl<SelectionModel<Component>>())
        selectionContainers.put(SelectionDrawingStrategy.BELOW, DrawableContainerImpl<SelectionModel<Component>>())
        unzoomableSelectionContainers.put(SelectionDrawingStrategy.ABOVE, UnzoomableContainer<UnzoomableSelectionModel<Component>>())

        super.addDrawable(ghostContainer)
        super.addDrawable(animationContainer)
        super.addDrawable(selectionContainers[SelectionDrawingStrategy.ABOVE]!!)
        super.addDrawable(unzoomableSelectionContainers[SelectionDrawingStrategy.ABOVE]!!)
        super.addDrawable(selectionContainers[SelectionDrawingStrategy.REPLACE]!!)
        super.addDrawable(drawing)
        super.addDrawable(selectionContainers[SelectionDrawingStrategy.BELOW]!!)
        super.addDrawable(highlightContainer)

        showGrid = true
    }

    override fun addSelectionModel(selectionModel: SelectionModel<Component>, strategy: SelectionDrawingStrategy) {
        if (selectionModel is Unzoomable) {
            unzoomableSelectionContainers[strategy]?.add(selectionModel as UnzoomableSelectionModel<Component>)
                ?: throw IllegalArgumentException("no suitable selection container found")
        } else {
            selectionContainers[strategy]?.add(selectionModel)
                ?: throw IllegalArgumentException("no suitable selection container found")
        }
    }

    override fun removeSelectionModel(selectionModel: SelectionModel<Component>) {
        selectionContainers.values.forEach { it.remove(selectionModel) }
        if (selectionModel is UnzoomableSelectionModel) {
            unzoomableSelectionContainers.values.forEach { it.remove(selectionModel) }
        }
    }

    override fun removeAllSelectionModels() {
        selectionContainers.values.forEach { it.clear() }
    }

    override fun hasSelectionModelFor(component: Component): Boolean {
        selectionContainers.values.forEach {
            if (!it.getDrawables().filter { it.component === component }.isEmpty()) {
                return true
            }
        }
        unzoomableSelectionContainers.values.forEach {
            if (!it.getDrawables().filter { it.component === component }.isEmpty()) {
                return true
            }
        }
        return false
    }

    override fun addDrawableDrawer(drawableDrawer: DrawableDrawer<Component>) {
        drawableDrawer.successor = this.drawableDrawer
        this.drawableDrawer = drawableDrawer
        drawing.setDrawableDrawer(this.drawableDrawer)
    }

    override fun getComponentSelectionDrawingStrategy(component: Component): SelectionDrawingStrategy {
        return component.preferredSelectionDrawingStrategy ?: defaultSelectionDrawingStrategy
    }

    /** ---- [View] interface*/

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