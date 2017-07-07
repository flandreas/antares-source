package ch.scorpion.jabbah.graph.view.connect

import ch.scorpion.jabbah.draw.DrawProperties
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.drawable.AbstractRectangularUnzoomable
import ch.scorpion.jabbah.draw.graphics.Stroke
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.module.DrawModule

/**
 * Draws a [ConnectionPointHighlight] as a circle.
 */
class ConnectionPointHighlightCircle : AbstractRectangularUnzoomable(SIZE_HALF), ConnectionPointHighlight {

    companion object {
        /** The name of the [Color] property in [DrawProperties] */
        val PROP_COLOR = "graph.view.isPort.highlight.color"

        val SIZE_HALF = 6.0
        val STROKE = Stroke()
    }

    override fun draw(context: DrawContext) {
        context.g.color = DrawModule.properties.getColor(PROP_COLOR)
        val rect = getViewRectangle()
        context.g.drawOval(rect.x.toInt(), rect.y.toInt(), rect.width.toInt(), rect.height.toInt())
        context.g.fillOval(rect.x.toInt(), rect.y.toInt(), rect.width.toInt(), rect.height.toInt())
    }

    override val lineWidth: Double
        get() = STROKE.width.toDouble()
}