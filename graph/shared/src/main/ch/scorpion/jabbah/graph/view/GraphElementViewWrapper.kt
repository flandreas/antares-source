package ch.scorpion.jabbah.graph.view

import ch.scorpion.jabbah.draw.*
import ch.scorpion.jabbah.draw.drawable.Locatable
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.SelectionDrawingStrategy
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.RectangularShape
import ch.scorpion.jabbah.graph.model.GraphElement
import ch.scorpion.jabbah.graph.view.style.GraphStyleType
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

/**
 * Wraps a [Component] in a [GraphElementView].
 *
 * Note that we can't use Kotlin delegation for delegating the [Component] interface to the wrapped [Component],
 * because the wrapped [Component] is changed during the lifetime of this wrapper (due to deserialization),
 * which is not yet supported by Kotlin's built-in delegation.
 */
class GraphElementViewWrapper<T : GraphElement>(
    component: Component? = null,
    styleProvider: StyleProvider = DrawStyleModule.styleProvider
) : AbstractGraphElementView<T>(styleProvider, GraphStyleType.VERTICE, null) {

    private var drawableOwner: DrawableOwner? = null

    private var _component: Component? = null
        set(value) {
            if (drawableOwner != null) {
                drawableOwner?.dispose()
            }
            field = value
            if (value != null) {
                drawableOwner = DrawableOwner(this, value)
            }
        }

    init {
        _component = component
    }

    /** ---- [Storable] interface */

    override fun write(writer: StoreWriter) {
        writer.writeStorable("component", _component!!)
    }

    override fun read(reader: StoreReader) {
        // don't call super.read() in order not to interfere with the wrapped Component's style
        _component = reader.readStorable("component") as Component
    }

    /** ---- [Locatable] interface */

    override var location: Point2D
        get() = _component!!.location
        set(value) {
            _component!!.location = value
        }

    /** ---- [Drawable] */

    override val boundingBox: RectangularShape
        get() = _component!!.boundingBox


    override val type: String?
        get() = _component!!.type

    override var visible: Boolean
        get() = _component!!.visible
        set(value) { _component!!.visible = value }

    override fun draw(context: DrawContext) {
        _component!!.draw(context)
    }

    override fun contains(x: Double, y: Double): Boolean {
        return _component!!.contains(x, y)
    }

    override fun getToolTipText(x: Double, y: Double, width: Int?): String? {
        return _component!!.getToolTipText(x, y, width)
    }

    override fun <T : InputEventContext> getInputEventHandler(context: T): InputEventHandler<T> {
        return _component!!.getInputEventHandler(context)
    }

    override fun handleAdded(container: DrawableContainer<*>) {
        _component!!.handleAdded(container)
    }

    override fun <T : Drawable> handleRemoved(container: DrawableContainer<T>) {
        _component!!.handleRemoved(container)
    }

    /** ---- [Component] interface */

    override val selectableComponent: Component get() = _component!!

    override val propertyOwner: Any get() = _component!!

    override var preferredSelectionDrawingStrategy: SelectionDrawingStrategy?
        get() = _component!!.preferredSelectionDrawingStrategy
        set(value) {
            super.preferredSelectionDrawingStrategy = value
        }
}