package ch.scorpion.jabbah.graph.view.container

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.SelectionDrawingStrategy
import ch.scorpion.jabbah.edit.model.AbstractComponent
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter
import ch.scorpion.jabbah.draw.graphics.Stroke
import ch.scorpion.jabbah.draw.module.DrawModule

/**
 * Indicates the location of the origin of the [Drawing] that represents the outer view of a container.
 */
class OriginIndicator(
        styleProvider: StyleProvider = DrawStyleModule.styleProvider,
        x: Double = 0.0,
        y: Double = 0.0
) : AbstractComponent(styleProvider) {

    companion object {
        val PROP_COLOR = "graph.containereditor.OriginIndicator.color"
        val PROP_SELECTION_COLOR = "graph.containereditor.OriginIndicator.selectionColor"
        val STROKE = Stroke(1.0f)
        val SIZE = 20.0
        val CIRCLE_SIZE = 14.0
    }

    override var location = Point2D(x, y)
        set(value) {
            invalidate()
            field = Point2D(value.x, value.y)
            updateBoundingBox()
            invalidate()
        }

    override val boundingBox = Rectangle2D()

    init {
        updateBoundingBox()
    }

    /** ---- [Storable] interface */

    override fun write(writer: StoreWriter) {
        writer.writePoint("location", location)
    }

    override fun read(reader: StoreReader) {
        location = reader.readPoint("location")
    }

    /** ---- [Drawable] interface */

    override fun draw(context: DrawContext) {
        val oldColor = context.g.color
        context.g.color = DrawModule.properties.getColor(PROP_COLOR)
        drawImpl(context)
        context.g.color = oldColor
    }

    override fun contains(x: Double, y: Double): Boolean {
        return boundingBox.contains(x, y)
    }

    /** ---- [Component] */

    override val type: String?
        get() = Translations.getString("graph.component.origin")

    override var preferredSelectionDrawingStrategy: SelectionDrawingStrategy?
        get() = SelectionDrawingStrategy.REPLACE
        set(value) { super.preferredSelectionDrawingStrategy = value }


    /** ---- [OriginIndicator] */

    fun drawSelected(context: DrawContext) {
        context.g.color = DrawModule.properties.getColor(PROP_SELECTION_COLOR)
        drawImpl(context)
    }

    private fun drawImpl(context: DrawContext) {
        context.g.stroke = STROKE
        context.g.drawLine(
            (location.x - SIZE / 2).toInt(), location.y.toInt(),
            (location.x + SIZE / 2).toInt(), location.y.toInt())
        context.g.drawLine(
            location.x.toInt(), (location.y - SIZE / 2).toInt(),
            location.x.toInt(), (location.y + SIZE / 2).toInt())

        context.g.drawOval(
            (location.x - CIRCLE_SIZE / 2).toInt(), (location.y - CIRCLE_SIZE / 2).toInt(),
            CIRCLE_SIZE.toInt(), CIRCLE_SIZE.toInt())
    }

    private fun updateBoundingBox() {
        boundingBox.setFrame(
            location.x - SIZE / 2 - 1, location.y - SIZE / 2 - 1, SIZE + 2, SIZE + 2)
    }
}