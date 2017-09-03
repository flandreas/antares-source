package ch.scorpion.jabbah.graph.view.oscilloscope

import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.drawable.AbstractRectangle
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.CompositeColor
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.style.Themes
import ch.scorpion.jabbah.edit.model.text.Label
import ch.scorpion.jabbah.graph.view.style.GraphStyleType
import ch.scorpion.jabbah.graph.view.style.GraphTheme

/**
 * The graphical figure used in probe views.
 */
class OscilloscopeProbeViewDrawable(
        location: Point2D,
        rowNumber: Int,
        val color: CompositeColor,
        private val styleProvider: StyleProvider = DrawStyleModule.styleProvider
) : AbstractRectangle(location.x, location.y, SIZE, SIZE) {

    companion object {
        val F = 7.0
        val SIZE = 5 * F

        private val PATH = System.get().createPath()
                .moveTo(0.0, SIZE)
                .lineTo(1 * F, 2 * F)
                .curveTo(2.0 * F, -3 * F, 8 * F, 3.0 * F, 3 * F, 4 * F)
                .close()
    }

    private val label = Label(
            text = rowNumber.toString(),
            font = styleProvider.getStyle(GraphStyleType.VERTICE).font,
            location = Point2D(20, 17)
    )

    var highlighted = false

    var filled = true

    var rowNumber: Int = rowNumber
        set(value) {
            if (field != value) {
                field = value
                label.text = field.toString()
            }
        }

    /** ---- [Drawable] */

    override fun draw(context: DrawContext) {
        context.g.translate(x, y)

        if (highlighted) {
            // TODO Configurable
            context.g.color = context.choose(Themes.get<GraphTheme>().selection).foregroundColor
            context.g.draw(PATH)
            label.draw(context)
        } else {
            if (filled) {
                context.g.color = context.choose(color).backgroundColor
                context.g.fill(PATH)
            }
            context.g.color = context.choose(color).foregroundColor
            context.g.stroke = styleProvider.getStyle(GraphStyleType.ANNOTATION).stroke
            context.g.draw(PATH)
            label.draw(context)
        }

        context.g.translate(-x, -y)
    }

    override val lineWidth: Double get() = 0.0
}
