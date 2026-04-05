package io.antarescircuit.jabbah.draw.view

import io.antarescircuit.jabbah.animation.AnimationModule
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.base.geom.Rectangle2D
import io.antarescircuit.jabbah.draw.Drawable
import io.antarescircuit.jabbah.draw.View

/**
 * Plays a [ZoomedPointVoyageAnimation] to bring a particular [Drawable] into the
 * focus of the user.
 */
object FocusDrawablePlayer {

    private const val DURATION = 600.0
    private const val END_ZOOM_FACTOR = 1.0

    fun playFocus(drawable: Drawable, view: View<*>) {
        playFocus(drawable.boundingBox.center, view)
    }

    private fun playFocus(p: Point2D, view: View<*>, endZoomFactor: Double = END_ZOOM_FACTOR) {
        val animation = ZoomedPointVoyageAnimation(
            view,
            DURATION,
            ZoomedPointTranslation(p, view.center, endZoomFactor)
        )
        AnimationModule.constantSpeedAnimator.schedule(animation)
        animation.start()
    }

    /**
     * Plays a pan animation if [drawables] are not completely visible in
     */
    fun ensureVisible(drawables: Collection<Drawable>, view: View<*>) {
        val bbox = Drawable.combinedBoundingBox(drawables)
        if (!Rectangle2D(0, 0, view.width, view.height).contains(view.modelToView(bbox))) {
            playFocus(bbox.center, view, view.zoomFactor)
        }
    }
}