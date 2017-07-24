package ch.scorpion.jabbah.edit.view

import ch.scorpion.jabbah.animation.AnimationTask
import ch.scorpion.jabbah.animation.AnimationTaskAdapter
import ch.scorpion.jabbah.animation.Animator
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.exception.IllegalArgumentException
import ch.scorpion.jabbah.draw.drawable.Transparent
import ch.scorpion.jabbah.draw.drawable.TransparentAnimation
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.model.ComponentMessage
import ch.scorpion.jabbah.edit.model.text.TextComponent
import ch.scorpion.jabbah.edit.style.EditStyleType
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.edit.model.text.TextComponentFactory
import ch.scorpion.jabbah.edit.module.EditModule

/**
 * Displays [ComponentMessage]s in a [DrawingView].
 */
class ComponentMessageDisplayer<T: Drawing<Component>>(
    private val drawingView: DrawingView<T>,
    private val eventBus: EventBus,
    private val animator: Animator,
    private val textComponentFactory: TextComponentFactory = EditModule.textComponentFactory.invoke()
) {

    companion object {
        private val INSET = 10
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
        val component = textComponentFactory.create(text, Point2D(), EditStyleType.MESSAGE ,DrawStyleModule.styleProvider)
        component.setFrame(calculateBounds(msg.source, text))

        FadeInOut(component, drawingView, animator)
    }

    /**
     * TODO: The height of the [TextComponent] should adjust itself according to the width of the box
     * and the text size.
     */
    private fun calculateBounds(source: Component, @Suppress("UNUSED_PARAMETER") text: String): Rectangle2D {
        val bbox = source.boundingBox
        return Rectangle2D(bbox.x, bbox.maxY + INSET, WIDTH, HEIGHT)
    }

    private inner class FadeInOut<T: Drawing<Component>>(
        component: Component,
        drawingView: DrawingView<T>,
        animator: Animator
    ) {

        init {
            if (component !is Transparent) {
                throw IllegalArgumentException("component must be Transparent")
            }
            component.transparency = Transparent.FULLY_TRANSPARENT
            drawingView.animationContainer.add(component)

            val fadeOutAnimation = TransparentAnimation.fadeOut(component, 600.0)
            fadeOutAnimation.addListener(object : AnimationTaskAdapter() {
                override fun ended(task: AnimationTask) {
                    LOG.debug("remove component")
                    drawingView.animationContainer.remove(component)
                }
            })
            animator.schedule(fadeOutAnimation)

            val timer = System.get().createTimer()
            timer.initialize(2000, {
                LOG.debug("start fade out animation")
                fadeOutAnimation.start()
                timer.stop()
            })

            val fadeInAnimation = TransparentAnimation.fadeIn(component, 300.0)
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
