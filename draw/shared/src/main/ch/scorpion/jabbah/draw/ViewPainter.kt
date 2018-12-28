package ch.scorpion.jabbah.draw

import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.base.geom.RectangularShape

/**
 * [ViewPainter] is a strategy used by a [View] for painting and repainting this [View].
 *
 * A simple strategy for repainting [View]s is to redraw the entire drawing every time is has been requested. A
 * more efficient strategy is to redraw only the parts of the drawing that have been invalidated previously. This
 * strategy would require to keep track of every invalidated region. Even more efficient strategies could use buffers
 * for regions that didn't have changed, but need to be redrawn.
 */
interface ViewPainter {

    /**
     * Initiates a repaint of the [View] that this [ViewPainter] is painting.
     *
     * It calculates the accumulated dirty regions and then calls [View.repaint] for the
     * entire dirty region.
     */
    fun repaintView()

    /**
     * Asks this [ViewPainter] to paint the associated [View] using the specified graphics context.
     *
     * It uses the clipping area of the specified graphics context as well as accumulated dirty region to find out the
     * region that is to be painted. Complex painting strategies will use buffered images and reduce the clipping area
     * before calling [View.draw] that draws the missing parts.
     */
    fun paintView(context: DrawContext)

    /**
     * Notifies this [ViewPainter] that a certain region of the [View] has been invalidated and therefore
     * needs to be repainted when [paintView] is called for the next time.
     *
     * Invalidation and repainting can be implemented more efficiently if it is known whether the invalidation was
     * caused by a ghost. A ghost is a [Drawable] that is always painted in front of all other [Drawable]s,
     * and therefore doesn't affect the other [Drawable]s of the drawing. Ghosts are [Drawable]s that are
     * heavily moved and resized. If it comes to repainting a region that was invalidated by a ghost, an optimized
     * repainting strategy could simply copy the buffered drawing background into the [View]. To make this
     * strategy work, the ghosts must not be painted into the drawing buffer of optimized implementations of this
     * [ViewPainter]. As a consequence, ghosts must be drawn by the [View] itself.
     *
     * @param region the region of the view that has been invalidated and needs redrawing. If `code`, the entire
     * [View] is invalidated.
     * @param ghost `true` if the invalidation was caused by a ghost
     */
    fun invalidateRegion(region: RectangularShape?, ghost: Boolean)
}

typealias ViewPainterFactory<T> = (View<T>) -> ViewPainter