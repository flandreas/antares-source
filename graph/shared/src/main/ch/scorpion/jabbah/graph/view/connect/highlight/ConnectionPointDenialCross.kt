package ch.scorpion.jabbah.graph.view.connect.highlight

import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.drawable.AbstractRectangularUnzoomable
import ch.scorpion.jabbah.draw.graphics.Stroke
import ch.scorpion.jabbah.draw.style.Themes
import ch.scorpion.jabbah.graph.view.style.GraphTheme

/**
 * Draws a [ConnectionPointHighlight] as a red cross to indicate that the current location is basically
 * a valid connection point, but the current state doesn't allow to make a connection.
 */
class ConnectionPointDenialCross: AbstractRectangularUnzoomable(ConnectionPointHighlight.Companion.SIZE_HALF), ConnectionPointHighlight {

    companion object {
        private val STROKE = Stroke(3.0f)
        private const val SIZE_HALF = 10
    }

    override val lineWidth: Double get() = STROKE.width.toDouble()

    override fun draw(context: DrawContext) {
        context.g.color = Themes.get<GraphTheme>().error.foregroundColor
        context.g.stroke = STROKE
        with (getViewRectangle()) {
            context.g.drawLine(centerX - SIZE_HALF, centerY - SIZE_HALF, centerX + SIZE_HALF, centerY + SIZE_HALF)
            context.g.drawLine(centerX + SIZE_HALF, centerY - SIZE_HALF, centerX - SIZE_HALF, centerY + SIZE_HALF)
        }
    }

    override var alternativeView: Boolean = false
}