package ch.scorpion.jabbah.draw

import ch.scorpion.jabbah.base.event.*
import ch.scorpion.jabbah.base.exception.IllegalStateException
import ch.scorpion.jabbah.base.geom.RectangularShape
import ch.scorpion.jabbah.draw.Canvas
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.Cursor
import ch.scorpion.jabbah.draw.graphics.Graphics2D

/**
 * A [View] is a zoomable view that can display a stack of [Drawable]s, which are typically
 * [DrawableContainer]s that represent entire diagrams, of that contain selection or highlight graphics.
 */
interface View<C : InputEventContext> : ViewToModelTransform {

    companion object {
        /** The name of the [ZoomPan] property in [PropertyChangeEvent]s.*/
        val PROP_ZOOM_PAN = "PROP_ZOOM_PAN"
    }

    /** The [Canvas] that renders this [View].*/
    val canvas: Canvas

    /** ---- Life cycle */

    /** Sets the application context to be added to the [DrawContext] being used by this [View].*/
    var applicationContext: Any?

    fun <T> castedAppContext(): T? = applicationContext as T

    /**
     * Asks this [View] to initialize itself after it has been layouted and become visible for the first time.
     * Implementing classes should at least apply their default {@link ZoomStrategy}.
     */
    fun initialize()

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

    /** Request the event focus from the window system for this [View] in order to receive key events.*/
    fun requestFocus()

    /**
     *  Sets the tool tip text to be displayed at the current location of the mouse, or `null` if no tooltip is
     * to be displayed.
     */
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

    var zoomPan: ZoomPan

    val zoomFactor: Double get() {return zoomPan.zoomFactor}

    /** Provides methods to navigate in this [View] using zooming and panning.*/
    val navigator: ViewNavigator

    /**
     * The [ZoomStrategy] to be applied by default when this [View] is initialized, or when new main content is set
     * (which is the responsibility of subclasses, because [View] doesn't define a main content).
     */
    var defaultZoomStrategy: ZoomStrategy


    /** ---- Content management */

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

    /** Replaces `oldDrawable` by newDrawable]`.*/
    fun replaceDrawable(oldDrawable: Drawable, newDrawable: Drawable)
}