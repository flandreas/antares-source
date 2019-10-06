package ch.scorpion.jabbah.graph.container

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.RectangularShape
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.drawable.Locatable
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.SelectionDrawingStrategy
import ch.scorpion.jabbah.edit.model.AbstractComponent
import ch.scorpion.jabbah.graph.model.Port
import ch.scorpion.jabbah.graph.view.port.PortView
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

/**
 * A [Component] that wraps a [PortView] in order to allow the user to manipulate it.
 */
open class PortViewComponent<T: Any>(
    styleProvider: StyleProvider = DrawStyleModule.styleProvider,
    var portView: PortView<T>? = null
) : AbstractComponent(styleProvider) {

    val port: Port<T> get() = portView!!.port

    var drawableOwner: DrawableOwner? = null

    init {
        preferredSelectionDrawingStrategy = SelectionDrawingStrategy.REPLACE
        if (portView != null) {
            drawableOwner = DrawableOwner(this, portView!!)
        }
    }

    /** ---- Manually delegated properties */

    var direction: Direction
        get() = portView!!.direction
        set(value) { portView!!.direction = value }

    /** ---- [Component] */

    override val type: String? get() = Translations.getString("graph.component.port")

    /** ---- [Storable] interface */

    override fun write(writer: StoreWriter) {
        super.write(writer)
        writer.writeStorable("portView", portView!!)
    }

    override fun read(reader: StoreReader) {
        super.read(reader)
        if (drawableOwner != null) {
            drawableOwner!!.dispose()
        }
        portView = reader.readStorable("portView") as PortView<T>
        drawableOwner = DrawableOwner(this, portView!!)
    }

    override fun getStorableChildren(): Iterator<Storable> {
        return listOf(portView!!).iterator()
    }

    /** ---- [Drawable] */

    override val boundingBox: RectangularShape
        get() = portView!!.boundingBox

    override fun draw(context: DrawContext) {
        portView!!.draw(context)
    }

    override fun contains(x: Double, y: Double): Boolean {
        return portView!!.contains(x, y)
    }

    /** ---- [Locatable] */

    override var location: Point2D
        get() = portView!!.location
        set(value) {portView!!.location = value}
}