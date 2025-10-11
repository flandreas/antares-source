package ch.scorpion.jabbah.draw.view

import ch.scorpion.jabbah.base.event.*
import ch.scorpion.jabbah.base.geom.*
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.*
import ch.scorpion.jabbah.draw.container.DrawableContainerImpl
import ch.scorpion.jabbah.draw.drawable.Unzoomable
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.Cursor
import ch.scorpion.jabbah.draw.graphics.Graphics2D
import ch.scorpion.jabbah.draw.module.DrawModule
import ch.scorpion.jabbah.draw.style.DrawTheme
import ch.scorpion.jabbah.draw.style.ThemeEvent
import ch.scorpion.jabbah.draw.style.Themes


/**
 * A standard implementation of the [View] interface.
 * @param affineTransformFactory a factory for creating new [AffineTransform]s
 */
open class ViewImpl<C : InputEventContext>(
	private val affineTransformFactory: () -> AffineTransform,
	applicationContextHolder: ApplicationContextHolder?,
	protected val eventBus: EventBus = BaseModule.eventBus,
	override val name: String = "",
	viewPainterFactory: ViewPainterFactory<C> = { InvalidatableViewPainter(it) }
) : View<C> {

	private val controller: ZoomPanController = ZoomPanController(this)

	// Contains the [Drawable]s drawn by this [View]. The topmost [Drawable] is stored at index 0.
	private val drawables: MutableList<Drawable> = mutableListOf()

	private val changeSupport = PropertyChangeSupport<Any>(this)

	private val themeListener: (ThemeEvent) -> Unit = {
		invalidate()
		repaint()
	}

	private val propertyChangeHandler = PropertyChangeHandler()

	override val space: ViewSpace = ViewSpace(Dimension2D(0, 0))

	override val contentBounds: ViewContentBounds = createViewContentBounds()

	// Backing property as alternative to 'lateinit' with custom setter.
	protected var _canvas: Canvas? = null

	/**
	 * Used to wait drawing this [View] until [Canvas] has been laid out and therefore has a proper [Dimension2D].
	 * Without this, this [View] would draw initial content before the [defaultZoomStrategy] has been applied,
	 * followed by drawing it after [defaultZoomStrategy] has been applied, which results in flickering.
	 * See also the [Canvas.initialLayout] property.
	 * */
	private var canvasLaidOut: Boolean = false

	override var canvas: Canvas
		get() = _canvas!!
		set(value) {
			if (_canvas != null) {
				_canvas!!.removePropertyChangeListener(propertyChangeHandler)
			}
			_canvas = value
			canvas.addPropertyChangeListener(propertyChangeHandler)

			space.viewDimension = canvas.dimension
			space.addPropertyChangeListener(propertyChangeHandler)

			canvasLaidOut = canvas.initialLayout

			firePropertyChange(View.PROP_CANVAS, null, _canvas)
		}

	override val mainContent: MainContent get() = MainContent(
		"Drawing",
		drawables.first(),
		Themes.get<DrawTheme>().background.color.backgroundColor)

	init {
		eventBus.register(ThemeEvent::class, themeListener)
		applicationContextHolder?.addPropertyChangeListener(propertyChangeHandler)
		contentBounds.addPropertyChangeListener(propertyChangeHandler)
		space.removePropertyChangeListener(propertyChangeHandler)
	}

	/** ---- [ContentView] */

	override val mainUI: Any? get() = canvas

	override val view: View<out C>? get() = this

	/** ---- Life cycle */

	override fun initialize() {
		applyDefaultZoomStrategy()
		controller.enabled = true
	}

	override fun dispose() {
		eventBus.unregister(ThemeEvent::class, themeListener)
		_canvas?.let {
			it.removePropertyChangeListener(propertyChangeHandler)
			painter.dispose()
		}
		space.removePropertyChangeListener(propertyChangeHandler)
		contentBounds.removePropertyChangeListener(propertyChangeHandler)
		controller.dispose()
	}

	override val applicationContextHolder: ApplicationContextHolder? = applicationContextHolder

	override val applicationContext: Any? get() = applicationContextHolder?.applicationContext

	/** ---- Content management */

	override val drawablesCount: Int get() = drawables.size

	override fun getDrawables(): Iterator<Drawable> = drawables.iterator()

	override fun addDrawable(drawable: Drawable) = addDrawable(drawable, drawablesCount)

	override fun addDrawable(drawable: Drawable, index: Int) {
		check(drawable !in drawables) { "drawable already contained" }
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

	override fun getInnerDrawableAt(x: Double, y: Double, condition: (Drawable) -> Boolean): Drawable? {
		for (drawable in drawables) {
			if (drawable is DrawableContainer<*>) {
				val innerDrawable = drawable.getDrawableAt(x, y)
				if (innerDrawable != null && condition.invoke(innerDrawable)) {
					return innerDrawable
				}
			}
		}
		return null
	}

	/** ---- Geometry */

	override val width: Int get() = _canvas?.dimension?.width?.toInt() ?: 0

	override val height: Int get() = _canvas?.dimension?.height?.toInt() ?: 0

	protected open fun createViewContentBounds(): ViewContentBounds = ViewContentBounds(mainBoundsAccessor = ::calculateCombinedBoundingBox)

	private fun calculateCombinedBoundingBox(): Rectangle2D {
		val bbox = Rectangle2D()
		drawables.filter { it.visible }.forEach { bbox.add(it.boundingBox) }
		return bbox
	}

	/** ---- Event handling */

	private val handler: InputEventHandler<C> = EventHandler()

	override fun dispatchEvent(e: InputEvent) {
		_canvas?.dispatchEvent(e)
	}

	override fun requestFocus() {
		_canvas?.requestViewFocus()
	}

	override fun getInputEventHandler(e: InputEvent): InputEventHandler<C> = handler

	override fun addMouseListener(l: MouseListener) {
		_canvas?.addMouseListener(l)
	}

	override fun removeMouseListener(l: MouseListener) {
		_canvas?.removeMouseListener(l)
	}

	override fun addMouseMotionListener(l: MouseMotionListener) {
		_canvas?.addMouseMotionListener(l)
	}

	override fun removeMouseMotionListener(l: MouseMotionListener) {
		_canvas?.removeMouseMotionListener(l)
	}

	override fun addMouseWheelListener(l: MouseWheelListener) {
		_canvas?.addMouseWheelListener(l)
	}

	override fun removeMouseWheelListener(l: MouseWheelListener) {
		_canvas?.removeMouseWheelListener(l)
	}

	override fun addKeyListener(l: KeyListener) {
		_canvas?.addKeyListener(l)
	}

	override fun removeKeyListener(l: KeyListener) {
		_canvas?.removeKeyListener(l)
	}

	override fun addPropertyChangeListener(l: PropertyChangeListener<Any>) {
		changeSupport.add(l)
	}

	override fun addPropertyChangeListener(l: (PropertyChangeEvent<Any>) -> Unit): PropertyChangeListener<Any> {
		return changeSupport.add(l)
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
			if (field != value) {
				invalidate()
				field = value
				repaint()
			}
		}

	final override val overlayContainer: DrawableContainer<Drawable> = DrawableContainerImpl(useViewCoordinates = true)

	override val decorator: ViewDecorator = ViewDecorator(this)

	val painter: ViewPainter = viewPainterFactory.invoke(this)

	/** Reusable buffer instance for querying the clipping rectangle from [Graphics2D]. */
	private val clipBuffer = Rectangle2D()

	/* Listens for [DrawableEvent]s from child [Drawable]s.*/
	private val childListener = ChildListener()

	inner class ChildListener : DrawableListener {
		override fun drawableInvalidated(event: DrawableEvent) {
			painter.invalidateRegion(event.area)
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
			painter.invalidateRegion(null)
		}

		override fun drawableRequestRedraw(event: DrawableEvent) {
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
		var modelClip = if (g.supportClipping) {
			g.getClipBounds(clipBuffer)
			clipBuffer.x = viewToModelX(clipBuffer.x)
			clipBuffer.y = viewToModelY(clipBuffer.y)
			clipBuffer.width = viewToModelLength(clipBuffer.width)
			clipBuffer.height = viewToModelLength(clipBuffer.height)
			clipBuffer
		} else {
			null
		}
		painter.paintView(DrawModule.drawContextFactory(g, modelClip, applicationContext))
	}

	override fun draw(context: DrawContext) {
		if (!canvasLaidOut) {
			return
		}

		context.g.save()

		// This creates a copy of the Transform with scaling set to 1.0
		val oldTransform = context.g.transform

		val zoomedTransform = context.g.transform
		zoomedTransform.concatenate(transformation.affineTransform)

		context.g.antialiasing = this.antialiasing

		drawables.asReversed().forEach {
			if (it is Unzoomable) {
				context.g.transform = oldTransform
				canvas.devicePixelRatio.also { dpr ->
					context.g.scale(dpr, dpr)
					it.draw(context)
					context.g.scale(1 / dpr, 1 / dpr)
				}
			} else {
				context.g.transform = zoomedTransform
				it.draw(context)
			}
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

		// Draw the overlay container
		context.g.transform = oldTransform
		canvas.devicePixelRatio.also { dpr ->
			context.g.scale(dpr, dpr)
			overlayContainer.draw(context)
			context.g.scale(1 / dpr, 1 / dpr)
		}

		context.g.restore()
	}

	override fun repaint() {
		_canvas?.repaint()
	}

	override fun repaint(x: Int, y: Int, w: Int, h: Int) {
		_canvas?.repaint(x, y, w, h)
	}

	protected fun invalidate(region: RectangularShape? = null) {
		_canvas?.let {
			painter.invalidateRegion(region)
		}
	}

	/** ---- Zooming, panning and navigating */

	/** Offers functions for navigating withing this [View].*/
	override val navigator: ViewNavigator = ViewNavigatorImpl(this, affineTransformFactory)

	override var defaultZoomStrategy: ZoomStrategy = ZoomStrategy(ZoomStrategyType.FIT_MAX_NORMAL)

	override var zoomStrategy: ZoomStrategy = defaultZoomStrategy
		set(value) {
			if (field != value) {
				val oldValue = field
				field = value
				firePropertyChange(View.PROP_ZOOM_STRATEGY, oldValue, field)
			}
			// Execute even if equal
			field.apply(navigator)
		}

	override var transformation: ViewTransformation = ViewTransformation.identity()
		set(value) {
			val oldValue = field
			field = value

			drawables.forEach {
				if (it is Unzoomable) {
					it.zoomPan = zoomPan
				}
			}

			invalidate()
			repaint()

			firePropertyChange(View.PROP_TRANSFORMATION, oldValue, field)
		}

	override val zoomPan: ZoomPan get() = transformation.zoomPan

	override var userZoomEnabled: Boolean = true
		set(value) {
			if (field != value) {
				field = value
				firePropertyChange(View.PROP_USER_ZOOM_ENABLED, !field, field)
			}
		}

	override var autoPanningEnabled: Boolean
		get() = controller.autoPanningEnabled
		set(value) {
			controller.autoPanningEnabled = value
		}

	override fun applyDefaultZoomStrategy() {
		zoomStrategy = defaultZoomStrategy
		applyZoomStrategy()
	}

	protected fun applyZoomStrategy() {
		zoomStrategy.apply(navigator)
	}

	/** ---- [ViewToModelTransform] */

	override fun viewToModelX(x: Double): Double = transformation.affineTransform.inverseTransform(Point2D(x, 0.0)).x

	override fun viewToModelY(y: Double): Double = transformation.affineTransform.inverseTransform(Point2D(0.0, y)).y

	override fun viewToModel(p: Point2D): Point2D = transformation.affineTransform.inverseTransform(p)

	override fun viewToModel(p: Point2D, zoomFactor: Double): Point2D =
		if (zoomFactor == this.zoomFactor) {
			viewToModel(p)
		} else {
			navigator.createTransformation(zoomFactor).affineTransform.inverseTransform(p)
		}

	override fun viewToModelLength(length: Double): Double = length / zoomFactor

	override fun modelToView(p: Point2D): Point2D = transformation.affineTransform.transform(p)

	override fun modelToViewX(x: Double): Double = modelToView(Point2D(x, 0.0)).x

	override fun modelToViewY(y: Double): Double = modelToView(Point2D(0.0, y)).y

	override fun modelToView(p: Point2D, zoomFactor: Double): Point2D =
		if (zoomFactor == this.zoomFactor) {
			modelToView(p)
		} else {
			navigator.createTransformation(zoomFactor).affineTransform.transform(p)
		}

	override fun modelToViewLength(length: Double): Double = length * zoomFactor

	override fun modelToViewLength(length: Double, zoomFactor: Double): Double = length * zoomFactor

	override fun modelToDeviceX(x: Double): Double = modelToViewX(x) / canvas.devicePixelRatio

	override fun modelToDeviceY(x: Double): Double = modelToViewY(x) / canvas.devicePixelRatio

	override fun modelToDevice(p: Point2D): Point2D = modelToView(p).multiply(1 / canvas.devicePixelRatio)

	override fun modelToDeviceLength(length: Double): Double = modelToViewLength(length) / canvas.devicePixelRatio

	/** ---- [ViewImpl] */

	init {
		overlayContainer.addDrawableListener(overlayListener)
	}

	private inner class PropertyChangeHandler : PropertyChangeListener<Any> {
		override fun propertyChanged(e: PropertyChangeEvent<Any>) {
			when (e.name) {
				Canvas.PROP_DIMENSION -> {
					space.viewDimension = canvas.dimension
					canvasLaidOut = space.viewDimension.widthInt > 0 && space.viewDimension.heightInt > 0
					applyZoomStrategy()
				}
				Canvas.PROP_DEVICE_PIXEL_RATIO -> {
					applyZoomStrategy()
				}
				ViewSpace.PROP_AREA -> applyZoomStrategy()
				ApplicationContextHolder.PROP_APPLICATION_CONTEXT -> {
					invalidate()
					repaint()
				}
				ViewContentBounds.PROP_TOTAL -> applyZoomStrategy()
			}
		}
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

		override fun mouseDragged(context: T): InputEventHandler<T>? =
			target?.mouseDragged(context)

		override fun mouseReleased(context: T): InputEventHandler<T>? =
			target?.mouseReleased(context)

		override fun keyPressed(context: T): InputEventHandler<T>? {
			target = target?.keyPressed(context)
			return target
		}

		override fun keyReleased(context: T): InputEventHandler<T>? {
			target = target?.keyReleased(context)
			return target
		}
	}
}
