package ch.scorpion.jabbah.edit.view

import ch.scorpion.jabbah.base.PreferencesChangedEvent
import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.geom.AffineTransform
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.*
import ch.scorpion.jabbah.draw.drawable.AbstractDrawableDrawer
import ch.scorpion.jabbah.draw.drawable.DrawableDrawer
import ch.scorpion.jabbah.draw.style.DrawTheme
import ch.scorpion.jabbah.draw.style.Stylable
import ch.scorpion.jabbah.draw.style.Themes
import ch.scorpion.jabbah.draw.view.ContentView
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
			val firstTime = super._canvas == null
			super.canvas = value
			if (firstTime) {
				setupContent()
				grid.view = this
				showGrid = true
			}
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

	        content.selectionManager.activate()
        }

    override var editable: Boolean = editable
        set(value) {
	        LOG.debug("Setting DrawingView with '$drawing' to editable=$value")
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

    override val drawing: T get() = content.drawing

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

	override fun setDrawing(drawing: T, applyDefaultZoomStrategy: Boolean) {
		if (drawing !== content.drawing) {
			content = createContent(drawing)
			if (applyDefaultZoomStrategy) {
				applyDefaultZoomStrategy()
			}
		}
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

	override val mainContent: MainContent get() = MainContent(
		drawing.toString(),
		drawing,
		Themes.get<DrawTheme>().background.color.backgroundColor)

	override fun createViewContentBounds(): ViewContentBounds = ViewContentBounds { drawing.boundingBox }

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
        super.addDrawable(ghostContainer)
        super.addDrawable(animationContainer)
        super.addDrawable(content.zoomableSelectionContainerFor(SelectionDrawingStrategy.ABOVE)!!)
        super.addDrawable(content.unzoomableSelectionContainerFor(SelectionDrawingStrategy.ABOVE)!!)
        super.addDrawable(content.zoomableSelectionContainerFor(SelectionDrawingStrategy.REPLACE)!!)
        super.addDrawable(drawing)
        super.addDrawable(content.zoomableSelectionContainerFor(SelectionDrawingStrategy.BELOW)!!)
        super.addDrawable(highlightContainer)
	    super.addDrawable(content.backdropDrawer)
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
	    replaceDrawable(content.backdropDrawer, newContent.backdropDrawer)
        transformation = newContent.transformation
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

	        if (selectionManager.isSelected(drawable)) {
	        	val replacingSelectionModel = content.getReplacingSelectionModel(drawable)
		        if (replacingSelectionModel == null) {
					draw(drawable, context)
		        }
	        } else {
				draw(drawable, context)
	        }

	        nextProcessor(context, drawable)
        }

	    private fun draw(drawable: Drawable, context: DrawContext) {
			if (drawable !is Stylable || !drawable.styleType.isBackdrop) {
				drawable.draw(context)
			}
		}
    }
}
