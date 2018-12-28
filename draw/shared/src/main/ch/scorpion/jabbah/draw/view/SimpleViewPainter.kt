package ch.scorpion.jabbah.draw.view

import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.InputEventContext
import ch.scorpion.jabbah.draw.View
import ch.scorpion.jabbah.draw.ViewPainter
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.base.geom.RectangularShape

/**
 * [SimpleViewPainter] is a simple implementation of the [ViewPainter] interface and paints an [View]
 * by painting the entire [View] each time a repaint is requested.
 *
 * This class only exists for testing and demonstration purposes. For real applications, use more efficient painting
 * strategies like [BufferedViewPainter].
 */
class SimpleViewPainter(val view: View<out InputEventContext>) : ViewPainter {

    override fun repaintView() {
        view.repaint()
    }

    override fun paintView(context: DrawContext) {
        view.draw(context)
    }

    override fun invalidateRegion(region: RectangularShape?, ghost: Boolean) {
        // empty, because this simple strategy repaints the entire drawing
    }
}