package io.antarescircuit.jabbah.draw.view

import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.draw.InputEventContext
import io.antarescircuit.jabbah.draw.View
import io.antarescircuit.jabbah.draw.ViewPainter
import io.antarescircuit.jabbah.base.geom.RectangularShape

/**
 * [SimpleViewPainter] is a simple implementation of the [ViewPainter] interface and paints an [View]
 * by painting the entire [View] each time a repaint is requested.
 *
 * This class only exists for testing and demonstration purposes. For real applications, use more efficient painting
 * strategies like [InvalidatableViewPainter].
 */
class SimpleViewPainter(val view: View<out InputEventContext>) : ViewPainter {

    override fun dispose() {}

    override fun repaintView() {
        view.repaint()
    }

    override fun paintView(context: DrawContext) {
        view.draw(context)
    }

    override fun invalidateRegion(region: RectangularShape?) {
        // empty, because this simple strategy repaints the entire drawing
    }
}