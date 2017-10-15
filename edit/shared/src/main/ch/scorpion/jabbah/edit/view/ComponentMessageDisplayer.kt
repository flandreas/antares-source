package ch.scorpion.jabbah.edit.view

import ch.scorpion.jabbah.animation.AnimationTask
import ch.scorpion.jabbah.animation.AnimationTaskAdapter
import ch.scorpion.jabbah.animation.Animator
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.draw.drawable.Transparent
import ch.scorpion.jabbah.draw.drawable.TransparentAnimation
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.model.ComponentMessage
import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.DrawableContainer
import ch.scorpion.jabbah.edit.model.text.FlexibleTextView
import ch.scorpion.jabbah.edit.style.EditStyleType

/**
 * Displays [ComponentMessage]s in a [DrawingView].
 */
class ComponentMessageDisplayer<T: Drawing<Component>>(
    private val drawingView: DrawingView<T>,
    private val eventBus: EventBus,
    private val animator: Animator
) {

    companion object {
        private val LOG by logger(ComponentMessageDisplayer::class)
        private val INSET = 10.0
    }

    init {
        eventBus.register(ComponentMessage::class, this::handle)
    }

    fun dispose() {
        eventBus.unregister(ComponentMessage::class, this::handle)
    }

    private fun handle(msg: ComponentMessage) {
        if (msg.source != null && !drawingView.drawing.contains(msg.source)) {
            return
        }
        val text = Translations.getString(msg.messageKey)
        val messageView = FlexibleTextView(
                text = text,
                anchor = calculateAnchorPoint(msg),
                direction = Direction.SOUTH,
                styleType = EditStyleType.MESSAGE)

        val container = if (msg.source == null) drawingView.overlayContainer else drawingView.ghostContainer as DrawableContainer<Drawable>

        container.add(messageView)
        container.validate()

        FadeInOut(messageView, container, animator)
    }

    private fun calculateAnchorPoint(msg: ComponentMessage): Point2D {
        if (msg.source == null) {
            return Point2D(drawingView.width / 2, drawingView.height / 2)
        } else {
            val bbox = msg.source.boundingBox
            return Point2D(bbox.centerX, bbox.maxY + INSET)
        }
    }

    private inner class FadeInOut(
            messageView: Transparent,
            container: DrawableContainer<Drawable>,
            animator: Animator
    ) {

        init {
            messageView.transparency = Transparent.FULLY_TRANSPARENT

            val fadeOutAnimation = TransparentAnimation.fadeOut(messageView, 600.0)
            fadeOutAnimation.addListener(object : AnimationTaskAdapter() {
                override fun ended(task: AnimationTask) {
                    LOG.debug("remove MessageView")
                    container.remove(messageView)
                }
            })
            animator.schedule(fadeOutAnimation)

            val timer = System.get().createTimer()
            timer.initialize(2000, {
                LOG.debug("start fade out animation")
                fadeOutAnimation.start()
                timer.stop()
            })

            val fadeInAnimation = TransparentAnimation.fadeIn(messageView, 300.0)
            fadeInAnimation.addListener(object : AnimationTaskAdapter() {
                override fun ended(task: AnimationTask) {
                    LOG.debug("start timer")
                    timer.start()
                }
            })
            animator.schedule(fadeInAnimation)
            LOG.debug("start fade in animation")
            fadeInAnimation.start()
        }
    }
}
