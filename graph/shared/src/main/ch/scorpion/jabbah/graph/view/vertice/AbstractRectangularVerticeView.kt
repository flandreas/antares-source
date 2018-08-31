package ch.scorpion.jabbah.graph.view.vertice

import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.drawable.RectangularDrawable
import ch.scorpion.jabbah.draw.drawable.Locatable
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.base.geom.RectangularShape
import ch.scorpion.jabbah.base.geom.Rotation
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.DrawableAdapter
import ch.scorpion.jabbah.draw.DrawableEvent
import ch.scorpion.jabbah.draw.style.Themes
import ch.scorpion.jabbah.edit.model.AbstractComponent
import ch.scorpion.jabbah.edit.select.AbstractSelectionModel
import ch.scorpion.jabbah.graph.view.VerticeView
import ch.scorpion.jabbah.graph.model.Port
import ch.scorpion.jabbah.graph.model.Vertice
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.port.PortView
import ch.scorpion.jabbah.graph.view.style.GraphTheme
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter
import ch.scorpion.jabbah.io.Storable

/**
 * A rectangular shaped [VerticeView] implementation that adds the bounding boxes of all [PortView]s to its
 * own bounding box.
 *
 * [AbstractRectangularVerticeView] distinguishes between the location in terms of [Locatable] and the
 * location of the rectangle that defined the basic geometry. The upper left corner of the rectangle is
 * defined relative to the [location] of the [Locatable], which allows to implements concrete [VerticeView]s
 * whose location is at the connection point of one of the [PortView]s and **not** at the upper-left
 * corner of a rectangular box.
 *
 * @param T the type of the model [Vertice] that this [AbstractRectangularVerticeView] displays
 * @param x the x-coordinate of the upper-left rectangle corner, relative to [location]
 * @param y the y-coordinate of the upper-left rectangle corner, relative to [location]
 * @param w the width of the rectangle
 * @param h the height of the rectangle
 */
abstract class AbstractRectangularVerticeView<T : Vertice>(
    styleProvider: StyleProvider,
    baseResourceKey: String,
    model: T?,
    x: Double,
    y: Double,
    w: Double,
    h: Double
): AbstractVerticeView<T>(styleProvider, baseResourceKey, model), RectangularDrawable {

    constructor(
        styleProvider: StyleProvider,
        baseResourceKey: String,
        model: T?
    ): this(styleProvider, baseResourceKey, model, 0.0, 0.0, 0.0, 0.0)

    constructor(
        baseResourceKey: String,
        model: T
    ): this(DrawStyleModule.styleProvider, baseResourceKey, model)

    /** Contains the position relative to [location] and the size of the rectangle..*/
    private val rectangle = Rectangle2D(x, y, w, h)

    /** Contains the actual and absolute bounding box including all [PortView] bounding boxes. */
    private val _boundingBox = Rectangle2D()

    /** Contains the rectangular area that responds to [contains] method calls. Always kept in sync with the geometry.*/
    private val containsBox = Rectangle2D()

    /** Listens for geometry updates on [PortView]s and initiates bounding box recalculation.*/
    private val portViewUpdateListener = object : DrawableAdapter() {
        override fun drawableUpdated(event: DrawableEvent) {
            updateBoxes()
        }
    }

    /** ---- [Locatable] interface  */

    /** Holds the absolute location of this [AbstractRectangularVerticeView], which must not necessarily be the upper-left corner. */
    override var location: Point2D = Point2D.ZERO
        set(value) {
            invalidate()
            field = value
            updateBoxes()
            invalidate()
            if (!isResolving) {
                update()
            }
        }

    /** ---- [Storable] interface */

    override fun write(writer: StoreWriter) {
        super.write(writer)
        writer.writeDouble("x", location.x)
        writer.writeDouble("y", location.y)
        if (storeSize) {
            writer.writeDouble("w", width)
            writer.writeDouble("h", height)
        }
    }

    override fun read(reader: StoreReader) {
        super.read(reader)
        location = Point2D(reader.readDouble("x"), reader.readDouble("y"))
        if (storeSize) {
            setDimension(reader.readDouble("w"), reader.readDouble("h"))
        }
    }

    /** ---- [Drawable] interface */

    override fun contains(x: Double, y: Double): Boolean {
        return rotate(containsBox).contains(x, y)
    }

    /** ---- [AbstractComponent] */

    override fun rotationChanged(newRotation: Rotation) {
        super.rotationChanged(newRotation)
        updateBoxes()
    }

    /** ---- [AbstractVerticeView] */

    override fun getBoundingBoxImpl(): Rectangle2D {
        updateBoxes()
        return _boundingBox
    }

    override fun addPortView(portView: PortView<*>) {
        super.addPortView(portView)
        portView.addDrawableListener(portViewUpdateListener)
        updateBoxes()
    }

    override fun removePortView(portView: PortView<*>) {
        super.removePortView(portView)
        portView.removeDrawableListener(portViewUpdateListener)
        updateBoxes()
    }

    override fun <G : Any> handleConnect(edgeView: EdgeView<G>, port: Port<G>?) {
        super.handleConnect(edgeView, port)
        updateBoxes()
    }

    override fun <G : Any> handleUnconnect(edgeView: EdgeView<G>, port: Port<G>?) {
        super.handleUnconnect(edgeView, port)
        updateBoxes()
    }

    /** ---- [RectangularDrawable] */

    override val bounds: RectangularShape get() = rectangle

    override fun setBounds(x: Double, y: Double, w: Double, h: Double) {
        if (this.x == x && this.y == y && this.width == w && this.height == h) {
            return
        }
        invalidate()
        rectangle.setFrame(x, y, w, h)
        updateBoxes()
        invalidate()
        if (!isResolving) {
            update()
        }
    }

    override var width: Double
        get() = rectangle.width
        set(value) {setDimension(value, height)}

    override var height: Double
        get() = rectangle.height
        set(value) {setDimension(width, value)}

    override val lineWidth: Double
        get() = stroke.width.toDouble()

    override fun contains(x: Double, y: Double, w: Double, h: Double): Boolean {
        return rectangle.contains(x, y, w, h)
    }

    /** ---- [AbstractRectangularVerticeView] */

    open fun drawSelected(context: DrawContext) {
        draw(context) { c->
            super.drawImpl(c)
            context.g.color = Themes.get<GraphTheme>().selection.foregroundColor
            context.g.stroke = stroke
            context.g.draw(bounds)
        }
    }

    /**
     * Determines whether the width and height attributes should be stored by this [Storable], or whether
     * subclasses calculate the size by themselves. The default ist `false`
     * @return `true` if the width and height should be stored by this [Storable].
     */
    protected open val storeSize: Boolean = false

    /** Returns the number of model units to be added at the corresponding side of the rectangle when calculating the bounding box.*/
    protected open val outsetLeft: Int get() = 0
    protected open val outsetRight: Int get() = 0
    protected open val outsetTop: Int get() = 0
    protected open val outsetBottom: Int get() = 0

    protected fun setDimension(w: Double, h: Double) {
        invalidate()
        rectangle.setFrame(rectangle.x, rectangle.y, w, h)
        updateBoxes()
        invalidate()
        if (!isResolving) {
            update()
        }
    }

    protected fun updateBoxes() {
        _boundingBox.setFrame(
            location.x + x - lineWidth - outsetLeft,
            location.y + y - lineWidth - outsetTop,
            width + 2 * lineWidth + outsetLeft + outsetRight,
            height + 2 * lineWidth + outsetTop + outsetBottom
        )
        addPortViewsTo(_boundingBox, containsBox)
    }
}

class RectangularVerticeViewSelectionModel(c: AbstractRectangularVerticeView<*>) : AbstractSelectionModel<AbstractRectangularVerticeView<*>>(c) {

    override val boundingBox: RectangularShape get() = component.boundingBox

    override fun draw(context: DrawContext) {
        component.drawSelected(context)
    }

    override fun contains(x: Double, y: Double): Boolean = component.contains(x, y)

    override fun componentUpdated() {
        validate()
    }
}