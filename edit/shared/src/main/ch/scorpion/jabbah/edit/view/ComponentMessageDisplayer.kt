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
import ch.scorpion.jabbah.edit.model.text.TextComponent
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.draw.container.UnzoomableContainer
import ch.scorpion.jabbah.draw.drawable.Unzoomable

/**
 * Displays [ComponentMessage]s in a [DrawingView].
 */
class ComponentMessageDisplayer<T: Drawing<Component>>(
    private val drawingView: DrawingView<T>,
    private val eventBus: EventBus,
    private val animator: Animator
) {

    companion object {
        private val INSET = 10.0
        private val WIDTH = 200.0
        private val HEIGHT = 50.0
    }

    private val LOG by logger(ComponentMessageDisplayer::class)

    init {
        eventBus.register(ComponentMessage::class, this::handle)
    }

    fun dispose() {
        eventBus.unregister(ComponentMessage::class, this::handle)
    }

    private fun handle(msg: ComponentMessage) {
        if (!drawingView.drawing.contains(msg.source)) {
            return
        }
        val text = Translations.getString(msg.messageKey)
        val messageView = ComponentMessageView(text = text, yDist = INSET, frame = calculateBounds(msg.source, text))

        FadeInOut(messageView, drawingView.ghostContainer, animator)
    }

    /**
     * TODO: The height of the [TextComponent] should adjust itself according to the width of the box
     * and the text size.
     */
    private fun calculateBounds(source: Component, @Suppress("UNUSED_PARAMETER") text: String): Rectangle2D {
        val bbox = source.boundingBox
        return Rectangle2D(bbox.x, bbox.maxY, WIDTH, HEIGHT)
    }

    private inner class FadeInOut(
            messageView: ComponentMessageView,
            container: UnzoomableContainer<Unzoomable>,
            animator: Animator
    ) {

        init {
            messageView.transparency = Transparent.FULLY_TRANSPARENT
            container.add(messageView)
            container.validate()

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
