package ch.scorpion.jabbah.draw.view

import ch.scorpion.jabbah.base.checkState
import ch.scorpion.jabbah.base.event.*
import ch.scorpion.jabbah.draw.*
import ch.scorpion.jabbah.draw.drawable.Unzoomable
import ch.scorpion.jabbah.base.geom.AffineTransform
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.Cursor
import ch.scorpion.jabbah.draw.graphics.Graphics2D
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.base.geom.RectangularShape
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.container.DrawableContainerImpl
import ch.scorpion.jabbah.draw.module.DrawModule
import ch.scorpion.jabbah.draw.style.ThemeEvent


/**
 * A standard implementation of the [View] interface.
 *
 * @param canvas the target-specific [Canvas] in which this [View] draws its contents
 * @param transformFactory a factory for creating new [AffineTransform]s
 */
open class ViewImpl<C: InputEventContext>(
        override val canvas: Canvas,
        private val transformFactory: () -> AffineTransform,
        private val eventBus: EventBus = BaseModule.eventBus
) : View<C> {

    companion object {
        private val LOG by logger(ViewImpl::class)
    }

    private val controller: ZoomPanController = ZoomPanController(this)

    /** Contains the [Drawable]s drawn by this [View]. The topmost [Drawable] is stored at index 0.*/
    private val drawables: MutableList<Drawable> = mutableListOf()

    private val changeSupport = PropertyChangeSupport<Any>(this)

	private val themeListener: (ThemeEvent) -> Unit = {
		invalidate()
		repaint()
	}

	init {
		eventBus.register(ThemeEvent::class, themeListener)
	}

    /** ---- Life cycle */

    override var applicationContext: Any? = null
        set(value) {
            invalidate()
            field = value
            repaint()
        }

    override fun initialize() {
        applyDefaultZoomStrategy()
        controller.enabled = true
    }

	override fun dispose() {
		eventBus.unregister(ThemeEvent::class, themeListener)
	}

    /** ---- Content management */

    override val drawablesCount: Int get() = drawables.size

    override fun getDrawables(): Iterator<Drawable> = drawables.iterator()

    override fun addDrawable(drawable: Drawable) = addDrawable(drawable, drawablesCount)

    override fun addDrawable(drawable: Drawable, index: Int) {
        checkState(drawable !in drawables)
        if (drawable is Unzoomable) {
            drawable.zoomPan = this.zoomPan
        }
        drawables.add(index, drawable)
        drawable.addDrawableListener(childListener)
        drawable.invalidate()
        drawable.validate()
    }

    override fun removeDrawable(drawable: Drawable) {
        drawable.invalidate()
        drawables.remove(drawable)
        drawable.validate()
        // Remove DrawableListener not before drawable has been validated
        drawable.removeDrawableListener(childListener)
    }

    override fun containsDrawable(drawable: Drawable): Boolean = drawables.contains(drawable)

    override fun replaceDrawable(oldDrawable: Drawable, newDrawable: Drawable) {
        oldDrawable.invalidate()
        oldDrawable.removeDrawableListener(childListener)
        drawables[drawables.indexOf(oldDrawable)] = newDrawable
        newDrawable.addDrawableListener(childListener)
        //repaint()
    }

    /** ---- Geometry */

    override val width: Int get() = canvas.dimension.width.toInt()

    override val height: Int get() = canvas.dimension.height.toInt()

    override val contentBounds: RectangularShape
        get() = calculateCombinedBoundingBox()

    private fun calculateCombinedBoundingBox(): Rectangle2D {
        val bbox = Rectangle2D()
        drawables.forEach { bbox.add(it.boundingBox) }
        return bbox
    }

    /** ---- Event handling */

    private val handler: InputEventHandler<C> = EventHandler()

    override fun dispatchEvent(e: InputEvent) {
        canvas.dispatchEvent(e)
    }

    override fun requestFocus() {
        canvas.requestViewFocus()
    }

    override fun setToolTipText(text: String?) {
        canvas.setToolTipText(text)
    }

    override fun getInputEventHandler(e: InputEvent): InputEventHandler<C> = handler

    override fun addMouseListener(l: MouseListener) {
        canvas.addMouseListener(l)
    }

    override fun removeMouseListener(l: MouseListener) {
        canvas.removeMouseListener(l)
    }

    override fun addMouseMotionListener(l: MouseMotionListener) {
        canvas.addMouseMotionListener(l)
    }

    override fun removeMouseMotionListener(l: MouseMotionListener) {
        canvas.removeMouseMotionListener(l)
    }

    override fun addMouseWheelListener(l: MouseWheelListener) {
        canvas.addMouseWheelListener(l)
    }

    override fun removeMouseWheelListener(l: MouseWheelListener) {
        canvas.removeMouseWheelListener(l)
    }

    override fun addKeyListener(l: KeyListener) {
        canvas.addKeyListener(l)
    }

    override fun removeKeyListener(l: KeyListener) {
        canvas.removeKeyListener(l)
    }

    override fun addPropertyChangeListener(l: PropertyChangeListener<Any>) {
        changeSupport.add(l)
    }

    override fun removePropertyChangeListener(l: PropertyChangeListener<Any>) {
        changeSupport.remove(l)
    }

    protected fun firePropertyChange(name: String, oldValue: Any?, newValue: Any?) {
        changeSupport.fire(name, oldValue, newValue)
    }

    /** ---- Drawing and repainting */

    override var overlayColor: Color? = null
        set(value) {
            invalidate()
            field = value
            repaint()
        }

    final override val overlayContainer: DrawableContainer<Drawable> = DrawableContainerImpl()

    val painter: ViewPainter = InvalidatableViewPainter(this)

    /* Listens for [DrawableEvent]s from child [Drawable]s.*/
    private val childListener = ChildListener()

    inner class ChildListener : DrawableListener {
        override fun drawableInvalidated(event: DrawableEvent) {
            painter.invalidateRegion(event.area, false)
        }

        override fun drawableRequestRedraw(event: DrawableEvent) {
            painter.repaintView()
        }

        override fun drawableUpdated(event: DrawableEvent) {
            // empty
        }
    }

    /** Listens for [DrawableEvent]s from the [overlayContainer].*/
    private val overlayListener = OverlayListener()

    /**
     * We cannot use [ChildListener] for invalidating the view, because that one operates on model coordinates,
     * but the overlay container operates on view coordinates. Hence, we must invalidate the entire [View].
     */
    inner class OverlayListener : DrawableListener {
        override fun drawableInvalidated(event: DrawableEvent) {
            LOG.debug("OverlayListener: drawableInvalidated")
            painter.invalidateRegion(null, false)
        }

        override fun drawableRequestRedraw(event: DrawableEvent) {
            LOG.debug("OverlayListener: drawableRequestRedraw")
            painter.repaintView()
        }

        override fun drawableUpdated(event: DrawableEvent) {
            // empty
        }
    }

    override var antialiasing: Boolean = true

    override fun setCursor(cursor: Cursor) {
        canvas.setCursor(cursor)
    }

    override fun paint(g: Graphics2D) {
        painter.paintView(DrawContext(g, applicationContext))
    }

    override fun draw(context: DrawContext) {
        context.g.save()

        val oldTransform = context.g.transform
        val zoomedTransform = context.g.transform
        zoomedTransform.concatenate(transform)

        context.g.antialiasing = this.antialiasing

        drawables.asReversed().forEach {
            if (it is Unzoomable) {
                context.g.transform = oldTransform
            } else {
                context.g.transform = zoomedTransform
            }
            it.draw(context)
        }

        if (overlayColor != null) {
            context.g.color = overlayColor!!
            context.g.fillRect(0.0, 0.0, canvas.dimension.width, canvas.dimension.height)
        }

	    if (DrawModule.debugGfx) {
		    context.g.color = DrawModule.DEBUG_BBOX_COLOR_SECONDARY
		    val originView = modelToView(Point2D.ZERO)
		    context.g.drawLine(originView.x, 0.0, originView.x, height.toDouble())
			context.g.drawLine(0.0, originView.y, width.toDouble(), originView.y)
	    }

        context.g.transform = oldTransform

        overlayContainer.draw(context)

        context.g.restore()
    }

    override fun repaint() {
        canvas.repaint()
    }

    override fun repaint(x: Int, y: Int, w: Int, h: Int) {
        canvas.repaint(x, y, w, h)
    }

    private fun invalidate(region: RectangularShape? = null) {
        painter.invalidateRegion(region, false)
    }

    /** ---- Zooming, panning and navigating */

    /** The [AffineTransform] that represents the current zoom factor and pan offset.*/
    private var transform : AffineTransform = transformFactory.invoke()

    /** The location in view coordinates relative to which scale transformations are performed.*/
    private var zoomCenter: Point2D = Point2D()

    /** Offers functions for navigating withing this [View].*/
    override val navigator: ViewNavigator = ViewNavigatorImpl(this)

    override var defaultZoomStrategy: ZoomStrategy = ZoomStrategy(ZoomStrategyType.FIT_MAX_NORMAL)

    private var _zoomPan: ZoomPan = ZoomPan(this)
    override var zoomPan: ZoomPan
        get() = _zoomPan
        set(newValue) {
            LOG.debug("Setting ZoomPan $newValue")
            val panOffset = Point2D(
                    newValue.panOrigin.x - _zoomPan.panOrigin.x,
                    newValue.panOrigin.y - _zoomPan.panOrigin.y)
            val oldValue = _zoomPan
            _zoomPan = newValue

            updateTransform(panOffset)
            drawables.forEach {
                if (it is Unzoomable) {
                    it.zoomPan = _zoomPan
                }
            }

            invalidate()
            repaint()
            firePropertyChange(View.PROP_ZOOM_PAN, oldValue, newValue)
        }

    override var userZoomEnabled: Boolean = true
        set(value) {
            if (field != value) {
                field = value
                firePropertyChange(View.PROP_USER_ZOOM_ENABLED, !field, field)
            }
        }

    override var autoPanningEnabled: Boolean
        get() = controller.autoPanningEnabled
        set(value) { controller.autoPanningEnabled = value }

    protected fun applyDefaultZoomStrategy() {
        defaultZoomStrategy.apply(navigator)
    }

    /** Creates a new [ViewGeometry] for the specified zoom factor, while keeping the pan origin and zoom center.*/
    private fun createViewGeometry(zoomFactor: Double): ViewGeometry {
        return createViewGeometry(ZoomPan(this, zoomFactor, zoomPan.panOrigin), zoomCenter, Point2D())
    }

    /** Creates a new [ViewGeometry] that represents the specified geometrical properties.*/
    private fun createViewGeometry(zoomPan: ZoomPan, zoomCenter: Point2D, panOffset: Point2D): ViewGeometry {
        val newZoomCenter = calculateZoomCenter()

        // Compensate the offset that occurs if the View has been resized in the meantime
        val zoomCenterOffset = Point2D(
                (newZoomCenter.x - zoomCenter.x) / zoomPan.zoomFactor,
                (newZoomCenter.y - zoomCenter.y) / zoomPan.zoomFactor)

        if (zoomCenterOffset != Point2D()) {
            _zoomPan = ZoomPan(
                    this,
                    zoomPan.zoomFactor,
                    zoomPan.panOrigin.x + zoomCenterOffset.x + panOffset.x,
                    zoomPan.panOrigin.y + zoomCenterOffset.y + panOffset.y)
        }

        this.zoomCenter = newZoomCenter

        val transform = transformFactory.invoke()
        transform.translate(zoomCenter.x, zoomCenter.y)
        transform.scale(zoomPan.zoomFactor, zoomPan.zoomFactor)
        transform.translate(-zoomPan.panOrigin.x, -zoomPan.panOrigin.y)

        return ViewGeometry(_zoomPan, newZoomCenter, transform)
    }

    /** Calculates the default zoom center as the center ot the [View].*/
    private fun calculateZoomCenter() = Point2D(width / 2.0, height / 2.0)

    /** Updates the current [AffineTransform] for the specified pan offset.*/
    private fun updateTransform(panOffset: Point2D) {
        val viewGeom = createViewGeometry(zoomPan, zoomCenter, panOffset)
        this.transform = viewGeom.transform
        this._zoomPan = zoomPan
        this.zoomCenter = zoomCenter
    }

    /** ---- [ViewToModelTransform] */

    override fun viewToModelX(x: Double): Double = transform.inverseTransform(Point2D(x, 0.0)).x

    override fun viewToModelY(y: Double): Double = transform.inverseTransform(Point2D(0.0, y)).y

    override fun viewToModel(p: Point2D): Point2D = transform.inverseTransform(p)

    override fun modelToView(p: Point2D): Point2D = transform.transform(p)

    override fun modelToView(p: Point2D, zoomFactor: Double): Point2D =
        createViewGeometry(zoomFactor).transform.transform(p)

    /** ---- [ViewImpl] */

    init {
        overlayContainer.addDrawableListener(overlayListener)
    }

    private inner class EventHandler<in T : InputEventContext> : InputEventHandlerAdapter<T>() {
        private var target: InputEventHandler<T>? = null

        override fun mouseMoved(context: T): InputEventHandler<T>? {
            var destination: InputEventHandler<T>? = null
            drawables.firstOrNull {
	            destination = it.getInputEventHandler(context).mouseMoved(context)
	            destination != null
            }
	        return destination
        }

        override fun mouseClicked(context: T): InputEventHandler<T>? {
            var destination: InputEventHandler<T>? = null
            drawables.firstOrNull {
	            destination = it.getInputEventHandler(context).mouseClicked(context)
	            destination != null
            }
	        target = destination
            return destination
        }

        override fun mousePressed(context: T): InputEventHandler<T>? {
            var destination: InputEventHandler<T>? = null
            drawables.firstOrNull {
	            destination = it.getInputEventHandler(context).mousePressed(context)
	            destination != null
            }
	        target = destination
            return destination
        }

        override fun mouseDragged(context: T): InputEventHandler<T>? {
            return target?.mouseDragged(context)
        }

        override fun mouseReleased(context: T): InputEventHandler<T>? {
            return target?.mouseDragged(context)
        }

        override fun keyPressed(context: T): InputEventHandler<T>? {
            return target?.keyPressed(context)
        }

        override fun keyReleased(context: T): InputEventHandler<T>? {
            return target?.keyReleased(context)
        }
    }
}

data class ViewGeometry(val zoomPan: ZoomPan, val zoomPoint: Point2D, val transform: AffineTransform)