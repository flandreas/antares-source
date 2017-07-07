package ch.scorpion.jabbah.draw.drawable

import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.ZoomPan

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
 * Examples of [Unzoomable]s are guidelines or ghost layers that are displayed above a drawing.
 */
interface Unzoomable : Drawable {
    var zoomPan: ZoomPan?
}