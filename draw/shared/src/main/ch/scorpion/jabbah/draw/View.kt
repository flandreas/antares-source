package ch.scorpion.jabbah.draw

import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.event.*
import ch.scorpion.jabbah.base.geom.AffineTransform
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.draw.drawable.RectangularDrawable
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.Cursor
import ch.scorpion.jabbah.draw.graphics.Graphics2D
import ch.scorpion.jabbah.draw.view.ContentView
import ch.scorpion.jabbah.draw.view.ViewSpace

/**
 * Represents a user's request to close the specified view.
 * Typically processed by UI classes that contain the [View]. The system can refuse to accept the request,
 * for example if changes by the user have not yet been saved.
 */
data class CloseViewRequest(val view: Any)

/**
 * The main content of a [View] to be used when exporting or printing the contents of that [View],
 * excluding any auxiliary [Drawables][Drawable] used by this [View] for editing etc.
 */
data class MainContent(
	val name: String,
	val drawable: Drawable,
	val background: Color)

/**
 * Contains the current [ZoomPan] and the corresponding [AffineTransform] of a [View].
 */
data class ViewTransformation(
	val zoomPan: ZoomPan,
	val affineTransform: AffineTransform
) {
	companion object {
		fun identity(): ViewTransformation =
			ViewTransformation(ZoomPan(), System.createAffineTransform().apply { setToIdentity() })
	}
}

/**
 * A [View] is a zoomable view that can display a stack of [Drawable]s, which are typically
 * [DrawableContainer]s that represent entire diagrams, of that contain selection or highlight graphics.

 * [View]s are platform independent. Adoption to platform-specifics is provided by [Canvas].
 */
interface View<C : InputEventContext> : ContentView<C>, ViewToModelTransform {

	companion object {

		/** The name of the [Float] property in [Properties] representing the minimum zoom factor.*/
		const val PROP_MIN_ZOOM_FACTOR = "draw.view.minZoomFactor"

		/** The name of the [Float] property in [Properties] representing the maximum zoom factor.*/
		const val PROP_MAX_ZOOM_FACTOR = "draw.view.maxZoomFactor"

		/** The name of the [Float] property in [Properties] representing the default zoom factor (typically 1.0).*/
		const val PROP_DEFAULT_ZOOM_FACTOR = "draw.view.defaultZoomFactor"

		/** The name of the [ViewTransformation] property in [PropertyChangeEvent].*/
		const val PROP_TRANSFORMATION = "PROP_TRANSFORMATION"

		/** The name of the [userZoomEnabled] property in [PropertyChangeEvent]s.*/
		const val PROP_USER_ZOOM_ENABLED = "PROP_USER_ZOOM_ENABLED"

		/** The name of the [zoomStrategy] property in [PropertyChangeEvent]s. */
		const val PROP_ZOOM_STRATEGY = "draw.view.zoomStrategy"

		/** The name of the [canvas] property in [PropertyChangeEvent]s.*/
		const val PROP_CANVAS = "PROP_CANVAS"
	}

	/**
	 * The name of a [View] is primarily used to define scopes of [View] usages.
	 * For example, if an application consists of many different [Views][View],
	 * common logic classes can distinguish between these [Views][View].
	 * [View] names are optional. If not needed, an empty string can be used.
	 */
	val name: String

	/**
	 * The [Canvas] that renders this [View]. Late binding with this [View].
	 * Clients that need to perform logic NOT BEFORE the [Canvas] is bound can listen to the [PropertyChangeEvent].
	 */
	var canvas: Canvas

	val devicePixelRatio: Double get() = canvas.devicePixelRatio

	/** Controls the rectangular area that is free to display content to the user. Can be reduced from outside.*/
	val space: ViewSpace

	val mainContent: MainContent

	/** ---- Life cycle */

	val applicationContextHolder: ApplicationContextHolder?

	val applicationContext: Any?

	@Suppress("UNCHECKED_CAST")
	fun <T> castedAppContext(): T? = applicationContext as T

	/**
	 * Asks this [View] to initialize itself after it has been laid out and become visible for the first time.
	 * Implementing classes should at least apply their default [ZoomStrategy].
	 */
	fun initialize()

	fun dispose()

	/** ----  Geometry */

	/** The width of this [View] in view coordinates.*/
	val width: Int

	/** The height of this [View] in view coordinates.*/
	val height: Int

	/** The center of this [View] in view coordinates.*/
	val center: Point2D get() = Point2D(width / 2.0, height / 2.0)

	/** The bounds of the content (in model space) this [View] displays.*/
	val contentBounds: ViewContentBounds

	/** ---- Drawing and repainting */

	/** The [Color] to be used for filling the entire [View] over its content, if any.*/
	var overlayColor: Color?

	/** Controls whether this [View] uses anti-aliasing for painting.*/
	var antialiasing: Boolean

	/**
	 * Contains [Drawable]s that are painted above the main content on the [View] level.
	 * This is like a slide that lies above all other [DrawableContainer]s. Its [Drawable]s
	 * use the coordinate system of the [View] and not the one of the main content, and they
	 * are never zoomed. It can be used for displaying UI controls, or for displaying system-wide messages
	 * that are not related with a particular [Drawable] in the main content. Unlike all other [DrawableContainer]s,
	 * this one is not part of the [View]'s main content, i.e. it is not replaced when replacing the main content.
	 */
	val overlayContainer: DrawableContainer<Drawable>

	/** Decorates this [View] by displaying [RectangularDrawable]s at fixed positions.*/
	val decorator: ViewDecorator

	/** Sets the current cursor of this [View].*/
	fun setCursor(cursor: Cursor)

	/**
	 * Paints this [View] by setting up a [DrawContext] and delegating to the [ViewPainter].
	 *
	 * This method is typically called by a [Canvas] that contains this [View].
	 */
	fun paint(g: Graphics2D)

	/**
	 * Draws this [View] onto the specified graphics context.
	 *
	 * Since classes that implement this [View] interface will setup themselves to get notified when it is
	 * necessary to redraw parts or all of a drawing, this method is typically called by [ViewPainter]s that
	 * repaint the view on behalf of [View]s.
	 *
	 * Note that this method is responsible for scaling the drawing primitives according to the current zoom factors.
	 * This is not the responsibility of the [ViewPainter].
	 */
	fun draw(context: DrawContext)

	/**
	 * Request for asynchronous repainting the entire [View].
	 *
	 * Concrete [View] implementations should not immediately paint the specified region, but should delegate it to the
	 * repainting thread, depending of the current target platform.
	 */
	fun repaint()

	/**
	 * Request for asynchronous repainting the entire [View].
	 *
	 * Concrete [View] implementations should not immediately paint the specified region, but should delegate it to the
	 * repainting thread, depending on the current target platform.
	 *
	 * This function is typically called by a [ViewPainter] that keeps track of the dirty region.
	 */
	fun repaint(x: Int, y: Int, w: Int, h: Int)

	/** ---- Event handling */

	/**
	 * Dispatches the platform-specific event in the specified cross-platform [InputEvent]
	 * to the platform's event handling. Typical implementations will delegate to the corresponding method in [Canvas]
	 */
	fun dispatchEvent(e: InputEvent)

	/** Request the event focus from the window system for this [View] in order to receive key events.*/
	fun requestFocus()

	/**
	 * Returns the [InputEventHandler] that forwards mouse and key input events to the interested
	 * [Drawable]s of this [View].
	 */
	fun getInputEventHandler(e: InputEvent): InputEventHandler<C>

	fun addPropertyChangeListener(l: PropertyChangeListener<Any>)

	fun addPropertyChangeListener(l: (PropertyChangeEvent<Any>) -> Unit): PropertyChangeListener<Any>

	fun removePropertyChangeListener(l: PropertyChangeListener<Any>)

	fun addMouseListener(l: MouseListener)

	fun removeMouseListener(l: MouseListener)

	fun addMouseMotionListener(l: MouseMotionListener)

	fun removeMouseMotionListener(l: MouseMotionListener)

	fun addMouseWheelListener(l: MouseWheelListener)

	fun removeMouseWheelListener(l: MouseWheelListener)

	fun addKeyListener(l: KeyListener)

	fun removeKeyListener(l: KeyListener)

	/** ---- Navigation */

	/**
	 * Defines how model coordinate space is transformed to displayed view coordinate space,
	 * which involves zooming and panning, and the resulting [AffineTransform].
	 * When changed, notifies all registered [PropertyChangeListener]s that property [PROP_TRANSFORMATION] has changed.
	 */
	var transformation: ViewTransformation

	/** A shortcut for getting the [ZoomPan] in [transformation].*/
	val zoomPan: ZoomPan

	/** A shortcut for getting the zoom factor in [zoomPan].*/
	val zoomFactor: Double get() = zoomPan.zoomFactor

	/** Provides methods to navigate in this [View] using zooming and panning.*/
	val navigator: ViewNavigator

	/**
	 * The [ZoomStrategy] to be applied by default when this [View] is initialized, or when new main content is set
	 * (which is the responsibility of subclasses, because [View] doesn't define a main content).
	 */
	var defaultZoomStrategy: ZoomStrategy

	/**
	 * The [ZoomStrategy] requested most recently by the user. Initialized by [defaultZoomStrategy].
	 * When setting [zoomStrategy], its [ZoomStrategy.apply] method gets invoked by this [View].
	 */
	var zoomStrategy: ZoomStrategy

	/**
	 * Determines whether the user can manually change the [ZoomPan] of this [View]. While this is typically allowed,
	 * there are situations when it isn't allowed, for example during zoom animations.
	 * When changed, notifies all registered [PropertyChangeListener]s that property [PROP_USER_ZOOM_ENABLED] has changed.
	 */
	var userZoomEnabled: Boolean

	/**
	 * Determines whether auto-panning is enabled in this [View].
	 * Auto-panning automatically pans this [View] when the user drags the mouse near the border of the [View].
	 */
	var autoPanningEnabled: Boolean

	fun applyDefaultZoomStrategy()

	/** ---- Content management */

	/** Returns the number of [Drawable]s being displayed by this [View].*/
	val drawablesCount: Int

	/** Returns the [Drawable]s of this [View] in front-to-back order*/
	fun getDrawables(): Iterator<Drawable>

	/**
	 * Adds the specified [Drawable] at the bottommost stacking order index of this [View].
	 * @throws IllegalStateException if [drawable] is already contained
	 */
	fun addDrawable(drawable: Drawable)

	/**
	 * Adds the specified [Drawable] at the specified stacking order index of this [View].
	 * @throws IllegalStateException if [drawable] is already contained
	 */
	fun addDrawable(drawable: Drawable, index: Int)

	/** Removes the specified [Drawable] from this [View].*/
	fun removeDrawable(drawable: Drawable)

	/** Determines whether this [View] contains `drawable'.*/
	fun containsDrawable(drawable: Drawable): Boolean

	/** Replaces `oldDrawable` by `newDrawable`.*/
	fun replaceDrawable(oldDrawable: Drawable, newDrawable: Drawable)

	/**
	 * Returns the first [Drawable] contained in the top-level [Drawable]s of this [View]
	 * that fulfills the specified condition.
	 */
	fun getInnerDrawableAt(x: Double, y: Double, condition: (Drawable) -> Boolean): Drawable?
}