package io.antarescircuit.jabbah.edit

import io.antarescircuit.jabbah.draw.Drawable
import io.antarescircuit.jabbah.draw.drawable.Unzoomable

/**
 * A [Snapper] snaps single points or [Snappable]s to constrained locations, and optionally provides graphical
 * highlighter for these snapped locations.
 *
 * Typical examples of concrete [Snapper]s are guidelines, grids and snappers that automatically align [Component]s
 * to each other when they are moved or resized.
 *
 * [Snapper]s are not directly called by classes that handle user interactions like mouse dragging. Instead, they
 * are managed by an [SnapManager] belonging to an [Editor]'s ser interaction code that wants to use
 * snapping should retrieve an [Editor]'s [SnapManager] and use its snapping methods.
 *
 * Since snapping can often be done independently at both spacial dimensions, [Snapper] provides separate sets
 * of methods for x and y snapping, both for snapping points and for snapping [Snappable]s. For example, a single
 * horizontal guideline would only snap y coordinates of points and [Snappable]s, and would leave the x coordinate
 * unchanged.
 *
 * [Snapper]s are designed to be used hierarchically. For example, a particular [Editor] could use
 * guidelines at topmost level, followed by a component-to-component snapper, and a grid at the bottom. The grid would
 * only be called for snapping if the snappers above it would have denied to snap (because the point that was requested
 * to be snapped was outside their snapping region). Note that organizing [Snapper]s in hierarchies is not done by
 * the [Snapper]s themselves, but by [SnapManager]s.
 *
 * Due to the stackable nature of [Snapper]s, the snapping results are passed using an instance of
 * [SnapResult], which must be filled by the snapping methods of each [Snapper]. [SnapResult]
 * accumulates snap offsets while being passed through a hierarchy of [Snapper]s. For example, a vertical guideline
 * [Snapper] would implement [snapX(Double, SnapResult)] as follows:
 *
 * <pre>
 * private static final double GRAVITY = 10d;
 *
 * public void blaX(double x, SnapResult result) {
 * 	if (!isSnapEnabled()) {
 * 		return;
 * 	}
 * 	if (x &lt; guidelinePos - GRAVITY || x &gt; guidelinePos + GRAVITY) {
 * 		return;
 * 	}
 * 	result.addDX(guidelinePos - x, guidelinePos, this);
 * }
 * </pre>
 */

interface Snapper {

    var snapEnabled: Boolean

    /**
     * Snaps a point defined by its x and y coordinates and stores the snapping results in the specified
     * [SnapResult] object.
     *
     * @param x the x coordinate of the point that is to be snapped
     * @param y the y coordinate of the point that is to be snapped
     * @param result the object into which this [Snapper] should store its snapping results. */
    fun snap(x: Double, y: Double, result: SnapResult)

    /**
     * Snaps the x coordinate of a point and  stores the snapping results in the specified [SnapResult] object.
     *
     * @param x the x coordinate to be snapped
     * @param result the object into which this [Snapper] should store its snapping results.
     */
    fun snapX(x: Double, result: SnapResult)

    /**
     * Snaps the y coordinate of a point.
     * Stores the snapping results in the specified [SnapResult] object.
     *
     * @param y the y coordinate to be snapped
     * @param result the object into which this [Snapper] should store its snapping results.
     */
    fun snapY(y: Double, result: SnapResult)

    /**
     * Snaps an entire [Snappable] while taking an optional offset vector into account.
     *
     *
     * This method can be used when an [Snappable] is tried to move by a particular offset, and the move should be
     * snapped.
     *
     * @param snappable the [Snappable] to be snapped
     * @param dx the x coordinate of the offset vector
     * @param dy the y coordinate of the offset vector
     * @param result the object into which this [Snapper] should store its snapping results.
     */
    fun snap(snappable: Snappable, dx: Double, dy: Double, result: SnapResult)

    /**
     * Snaps the x coordinate of an [Snappable] while taking an optional offset vector into account.
     * This method can be used when an [Snappable] is tried to move by a particular offset, and the move should be
     * snapped.
     *
     * @param snappable the [Snappable] to be snapped
     * @param dx the x coordinate of the offset vector
     * @param result the object into which this [Snapper] should store its snapping results.
     */
    fun snapX(snappable: Snappable, dx: Double, result: SnapResult)

    /**
     * Snaps the y coordinate of an [Snappable] while taking an optional offset vector into account.
     * This method can be used when an Snappable is tried to move by a particular offset, and the move should be
     * snapped.

     * @param snappable the [Snappable] to be snapped
     * @param dy the y coordinate of the offset vector
     * @param result the object into which this [Snapper] should store its snapping results.
     */
    fun snapY(snappable: Snappable, dy: Double, result: SnapResult)

    /**
     * Returns an unzoomable [Drawable] that graphically highlights the last snapping of an x coordinate that was
     * done by this [Snapper].

     * @param x the x coordinate of the snapped location
     * @param y the y coordinate of the snapped location
     * @return the [Drawable] that highlight the snapping, or `null` if no x coordinate highlighting is
     *      provided by this [Snapper]
     */
    fun getSnapHighlightX(x: Double, y: Double, snappable: Snappable?): Unzoomable? = null

    /**
     * Returns an unzoomable [Drawable] that graphically highlights the last snapping of an y coordinate that was
     * done by this [Snapper].

     * @param x the x coordinate of the snapped location
     * @param y the y coordinate of the snapped location
     * @return the [Drawable] that highlight the snapping, or `null` if no y coordinate highlighting is
     *      provided by this [Snapper]
     */
    fun getSnapHighlightY(x: Double, y: Double, snappable: Snappable?): Unzoomable? = null
}