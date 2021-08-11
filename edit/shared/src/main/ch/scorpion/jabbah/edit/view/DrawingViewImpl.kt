package ch.scorpion.jabbah.edit.view

import ch.scorpion.jabbah.base.PreferencesChangedEvent
import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.geom.AffineTransform
import ch.scorpion.jabbah.base.geom.RectangularShape
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.*
import ch.scorpion.jabbah.draw.drawable.AbstractDrawableDrawer
import ch.scorpion.jabbah.draw.drawable.DrawableDrawer
import ch.scorpion.jabbah.draw.view.InvalidatableViewPainter
import ch.scorpion.jabbah.draw.view.ViewImpl
import ch.scorpion.jabbah.edit.*
import ch.scorpion.jabbah.edit.DrawingView.Companion.PROP_EDITABLE
import ch.scorpion.jabbah.edit.DrawingView.Companion.PROP_SHOW_GRID
import ch.scorpion.jabbah.edit.highlight.EditHighlightModule
import ch.scorpion.jabbah.edit.model.ComponentMessage
import ch.scorpion.jabbah.edit.select.EditSelectModule
import ch.scorpion.jabbah.edit.snap.GridImpl

/**
 * An implementation of a [View] for displaying and editing [Drawing]s.
 *
 * [DrawingViewImpl] has a fixed set of [DrawableContainer]s that are arranged on top of each other to form
 * a stack of view slides. Therefore, client classes cannot add or remove [Drawable]s by themselves.
 */
class DrawingViewImpl<T: Drawing<Component>>(
    drawing: T,
    transformFactory: () -> AffineTransform = { System.createAffineTransform() },
    applicationContextHolder: ApplicationContextHolder? = null,
    displayGlobalMessages: Boolean = false,
    private val selectionManagerFactory: SelectionManagerFactory = EditSelectModule.selectionManagerFactory,
    private val highlighterFactory: HighlighterFactory = EditHighlightModule.highlighterFactory,
    eventBus: EventBus = BaseModule.eventBus,
    viewPainterFactory: ViewPainterFactory<out EditInputEventContext> = { InvalidatableViewPainter(it) },
    editable: Boolean = true
) : ViewImpl<EditInputEventContext>(transformFactory, applicationContextHolder, eventBus, viewPainterFactory), DrawingView<T> {

	companion object {
		private val LOG by logger(DrawingViewImpl::class)
	}

	override var canvas: Canvas
		get() = super.canvas
		set(value) {
			super.canvas = value
			setupContent()
			grid.view = this
			showGrid = true
		}

    /** The [DrawableDrawer] used for drawing the [Drawing].*/
    private var drawableDrawer: DrawableDrawer<Component> = DrawingDrawer()

    /** Displays [ComponentMessage]s from [Component]s of the current [Drawing]. */
    private val componentMessageDisplayer = ComponentMessageDisplayer(
	    drawingView = this, displayGlobalMessages = displayGlobalMessages, eventBus = eventBus)

	private val preferenceChangeHandler: (PreferencesChangedEvent) -> Unit = {
		invalidate()
		repaint()
	}

	private val commandEventHandler: (CommandEvent) -> Unit = {
		invalidate()
		repaint()
	}

    /** ---- [DrawingView] interface */

    override var content: DrawingViewContent<T> = createContent(drawing)
        set(value) {
            if (field === value) {
                return
            }
	        // don't dispose old content, as it could be reused later
            val oldDrawing = field.drawing
            replaceContent(value)
            field = value
	        field.drawing.setDrawableDrawer(drawableDrawer)
            firePropertyChange(DrawingView.PROP_DRAWING, oldDrawing, field.drawing)
        }

    override var editable: Boolean = editable
        set(value) {
	        LOG.trace("Setting DrawingView with '$drawing' to editable=$value")
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
	        if (value !== content.drawing) {
		        content = createContent(value)
		        applyDefaultZoomStrategy()
	        }
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
	    eventBus.register(PreferencesChangedEvent::class, preferenceChangeHandler)
	    eventBus.register(CommandEvent::class, commandEventHandler)
    }

	override fun dispose() {
		super.dispose()
		eventBus.unregister(PreferencesChangedEvent::class, preferenceChangeHandler)
		eventBus.unregister(CommandEvent::class, commandEventHandler)
		componentMessageDisplayer.dispose()
		grid.dispose()
	}

    override fun createContent(drawing: T): DrawingViewContent<T> {
        return DrawingViewContentImpl(this, drawing, selectionManagerFactory, highlighterFactory)
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
        throw UnsupportedOperationException("Clients cannot remove Drawable from DrawingViewImpl")
    }

    override fun addDrawable(drawable: Drawable) {
        // DrawingViewImpl has a fixed set of DrawableContainers
        throw UnsupportedOperationException("Clients cannot add Drawable to DrawingViewImpl")
    }

    /** ---- [DrawingViewImpl] */

    private fun setupContent() {
	    // The DrawableContainer for REPLACE is expected to be invisible, because its SelectionModels are
	    // drawn by DrawingDrawer instead of the DrawableContainer. It must still be added to the View
	    // to avoid repainting issues (invalid regions would be wrong)
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
        zoomPan = newContent.zoomPan
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

	        content.highlighter.getHighlightFor(drawable)?.draw(context)

	        if (selectionManager.isSelected(drawable)) {
	        	val replacingSelectionModel = content.getReplacingSelectionModel(drawable)
		        if (replacingSelectionModel != null) {
		        	replacingSelectionModel.draw(context)
		        } else {
		        	drawable.draw(context)
		        }

	        } else {
	        	drawable.draw(context)
	        }

	        nextProcessor(context, drawable)
        }
    }
}
