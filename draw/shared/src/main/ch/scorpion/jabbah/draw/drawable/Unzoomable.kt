package ch.scorpion.jabbah.draw.drawable

import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.ZoomPan
import ch.scorpion.jabbah.draw.graphics.Graphics2D
import ch.scorpion.jabbah.draw.View
import ch.scorpion.jabbah.draw.ViewPainter
import ch.scorpion.jabbah.draw.DrawableContainer

/**
 * [Unzoomable]s can be implemented by those [Drawable]s that are added as view slides to an [View],
 * but don't want to be drawn to a [Graphics2D] that already contains a scaling transformation.
 *
 * Instead, classes that implement [Unzoomable] will perform the necessary scaling of their geometry by
 * themselves, thus avoiding strokes to be zoomed as well.
 *
 * [ViewPainter]s don't distinguish between normal [Drawable]s and [Unzoomable]s when calculating
 * dirty regions, which are caused by [Drawable] updates and are represented in model coordinates.
 * [Drawable]s have to convert those dirty regions to regions that need repainting. These regions are represented in
 * view coordinates. As a consequence, [Unzoomable]s should express their geometry in model space and apply zoom
 * factor and pan origin when painting.
 *
 * As an example, consider a circle with radius 10 and the center location (0,0). A regular, non-unzoomable
 * circle will report the bounding box (-10, -10, 20, 20), and its painting logic will draw a circle with radius 10.
 * If the current zoom factor is 2, a [View] will automatically render the circle twice as large, resulting in
 * a circle with radius 20 in the view. When the circle is invalidated, the [ViewPainter] takes the reported
 * bounding box (-10, -10, 20, 20), transforms it to the view coordinates (-20, -20, 40, 40), and adds this view
 * rectangle to the accumulated invalidated region.
 *
 * However, if the circle is an [Unzoomable] and contained in an unzoomable [DrawableContainer], things are different.
 * When the circle is painted, it will not be zoomed by the [View], although the rest of the drawing is zoomed
 * as usual. Therefore, the unzoomable circle has to draw itself twice as large, which it can achieve by
 * using (-20, -20, 40, 40) when calling the drawing operations. When this circle is invalidated, the [ViewPainter]
 * doesn't know that the circle is part of an unzoomable [DrawableContainer], and transforms its invalidation bounding box
 * to view coordinates as usual. The unzoomable circle must therefore report the bounding box of (-10, -10, 20, 20),
 * just like the zoomable version of a circle.
 *
 * In order to draw itself in the correct size, every [Unzoomable] needs to know the current zoom and pan factors.
 * An unzoomable [DrawableContainer] that contains [Unzoomable]s gets informed by a [View] upon changes of zoom and pan factors,
 * and forwards this information to every [Unzoomable] it contains. These [Unzoomable] store this information and
 * use it the time they draw themselves.
 *
 * Examples of [Unzoomable]s are guidelines, ghost layers that are displayed above a drawing, or connection point highlighters.
 */
interface Unzoomable : Drawable {
    var zoomPan: ZoomPan?
}