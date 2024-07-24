package ch.scorpion.jabbah.draw.view

import ch.scorpion.jabbah.animation.AnimationModule
import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.View

/**
 * Plays a [ZoomedPointVoyageAnimation] to bring a particular [Drawable] into the
 * focus of the user.
 */
object FocusDrawablePlayer {

    private const val DURATION = 600.0
    private const val END_ZOOM_FACTOR = 1.0

    fun playFocus(drawable: Drawable, view: View<*>) {
        val animation = ZoomedPointVoyageAnimation(
            view,
            DURATION,
            ZoomedPointTranslation(drawable.boundingBox.center, view.center, END_ZOOM_FACTOR)
        )
        AnimationModule.constantSpeedAnimator.schedule(animation)
        animation.start()
    }
}