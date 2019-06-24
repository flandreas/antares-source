package ch.scorpion.jabbah.draw

import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.base.event.*
import ch.scorpion.jabbah.base.exception.IllegalStateException
import ch.scorpion.jabbah.base.geom.RectangularShape
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.Cursor
import ch.scorpion.jabbah.draw.graphics.Graphics2D

/**
 * Represents a user's request to close the specified view.
 * Typically processed by UI classes that contain the [View]. The system can refuse to accept the request,
 * for example if changes by the user have not yet been saved.
 */
data class CloseViewRequest(val view: View<*>)

/**
 * A [View] is a zoomable view that can display a stack of [Drawable]s, which are typically
 * [DrawableContainer]s that represent entire diagrams, of that contain selection or highlight graphics.
 */
interface View<C : InputEventContext> : ViewToModelTransform {

	companion object {

		/** The name of the [Float] property in [Properties] representing the minimum zoom factor.*/
		const val PROP_MIN_ZOOM_FACTOR = "draw.view.minZoomFactor"

		/** The name of the [Float] property in [Properties] representing the maximum zoom factor.*/
		const val PROP_MAX_ZOOM_FACTOR = "draw.view.maxZoomFactor"

		/** The name of the [Float] property in [Properties] representing the default zoom factor (typically 1.0).*/
		const val PROP_DEFAULT_ZOOM_FACTOR = "draw.view.defaultZoomFactor"

		/** The name of the [ZoomPan] property in [PropertyChangeEvent]s.*/
		const val PROP_ZOOM_PAN = "PROP_ZOOM_PAN"

		/** The name of the [userZoomEnabled] property in [PropertyChangeEvent]s.*/
		const val PROP_USER_ZOOM_ENABLED = "PROP_USER_ZOOM_ENABLED"
	}

	/** The [Canvas] that renders this [View].*/
	val canvas: Canvas

	/** ---- Life cycle */

	/** Sets the application context to be added to the [DrawContext] being used by this [View].*/
	var applicationContext: Any?

	@Suppress("UNCHECKED_CAST")
	fun <T> castedAppContext(): T? = applicationContext as T

	/**
	 * Asks this [View] to initialize itself after it has been laid out and become visible for the first time.
	 * Implementing classes should at least apply their default {@link ZoomStrategy}.
	 */
	fun initialize()

	fun dispose()

	/** ----  Geometry */

	/** The width of this [View] in view space.*/
	val width: Int

	/** The height of this [View] in view space.*/
	val height: Int

	/** The bounds in model space of the primary content that this [View] displays.*/
	val contentBounds: RectangularShape

	/** ---- Drawing and repainting */

	/** The [Color] to be used for filling the entire [View] over its content, if any.*/
	var overlayColor: Color?

	/** Controls whether this [View] uses anti-aliasing for painting.*/
	var antialiasing: Boolean

	/**
	 * Contains [Drawable]s that are painted above the main content on the [View] level.
	 * This is like a slide that lies above all other [DrawableContainer]s. Its [Drawable]s
	 * use the coordinate system of the [View] and not the one of the main content, and they
	 * are never zoomed. It can be used for displaying UI controls, or for displaying system wide messages
	 * that are not related with a particular [Drawable] in the main content. Unlike all other [DrawableContainer]s,
	 * this one is not part of the [View]'s main content, i.e. it is not replaced when replacing the main content.
	 */
	val overlayContainer: DrawableContainer<Drawable>

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
	 * Since classes that implement this {@link View} interface will setup themselves to get notified when it is
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
	 * repainting thread, depending of the current target platform.
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
	 * Sets the tool tip text to be displayed at the current location of the mouse, or `null` if no tooltip is
	 * to be displayed.
	 */
	@Deprecated("Replaced with custom tooltip Drawables. See ch.scorpion.jabbah.draw.view.TooltipHandler")
	fun setToolTipText(text: String?)

	/**
	 * Returns the [InputEventHandler] that forwards mouse and key input events to the interested
	 * [Drawable]s of this [View].
	 */
	fun getInputEventHandler(e: InputEvent): InputEventHandler<C>

	fun addPropertyChangeListener(l: PropertyChangeListener<Any>)

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
	 * Holds the current zoom and pan of this [View].
	 * When changed, notifies all registered [PropertyChangeListener]s that property [PROP_ZOOM_PAN] has changed.
	 */
	var zoomPan: ZoomPan

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