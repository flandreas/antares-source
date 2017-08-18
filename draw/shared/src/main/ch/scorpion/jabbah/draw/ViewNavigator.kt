package ch.scorpion.jabbah.draw

import ch.scorpion.jabbah.base.event.PropertyChangeListener
import ch.scorpion.jabbah.base.geom.Point2D

/**
 * Provides navigation methods such as zooming and panning in a [View].
 */
interface ViewNavigator {

    /**
     * Sets the factor by which the [View] zooms the displayed [Drawable]s.
     *
     * A zoom factor of 1.0 represents no zooming (100%). Zoom factors smaller than 1.0 result in drawings that are
     * displayed smaller than normal. For example, a zoom factor of 0.5 displays the drawing in half the original size.
     * @param zoomFactor the new zoom factor, where 1.0 represents unzoomed views
     * @throws IllegalArgumentException if zoomFactor is smaller than or equal to 0
     */
    fun setZoomFactor(zoomFactor: Double)

    /** Increments the zoom factor by the given delta.*/
    fun addZoomFactor(delta: Double)

    /** Multiplies the zoom factor by the given factor.*/
    fun multiplyZoomFactor(factor: Double)

    /** Pans the [View] by the given delta offset.*/
    fun pan(dx: Int, dy: Int)

    /**
     * Sets the coordinates of the [View] to be displayed at the upper left-hand corner of the view.
     * @param p the new location of the pan origin.
     */
    fun setPanOrigin(p: Point2D)

    /** Sets the new zoom and pan settings of the [View].*/
    fun setZoomPan(zoomPan: ZoomPan)

    /** Pans to the center of the [View]'s content by maintaining the current zoom factor. */
    fun panCenter()

    /**
     * Pans to the center of the [View]'s content while applying the specified zoom factor.
     * @param zoomFactor the new zoom factor.
     */
    fun panCenter(zoomFactor: Double)

    /** Zooms and centers the [View] so that the [Drawable]s entirely fill the available space. */
    fun fit()

    /**
     * Zooms and centers the [View] so that the current [Drawable]s entirely fill the available space,
     * but avoids to set the zoom factor to more than 100% if not necessary in order to see everything.
     */
    fun fitMaxNormal()
}