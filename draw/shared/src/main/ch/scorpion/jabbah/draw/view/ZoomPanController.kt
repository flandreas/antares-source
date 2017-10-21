package ch.scorpion.jabbah.draw.view

import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.event.Button
import ch.scorpion.jabbah.base.event.EmptyMouseEvent
import ch.scorpion.jabbah.base.event.MouseAdapter
import ch.scorpion.jabbah.base.event.MouseEvent
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Vector2D
import ch.scorpion.jabbah.draw.View
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.time.Timer

/**
 * Allows the user to zoom in a [View] by using the mouse wheel and to pan in the [View] by dragging with the
 * middle mouse button.
 */
class ZoomPanController(val view: View<*>) {

    companion object {
        private val LOG by logger(ZoomPanController::class)
        private val AUTOPAN_TIMER_DELAY = 50
        private val AUTOPAN_SIZE = 10
        private val AUTOPAN_REGION = 50
    }

    var enabled: Boolean = false
        set(value) {
            if (value) {
                view.addMouseListener(controller)
                view.addMouseMotionListener(controller)
                view.addMouseWheelListener(controller)
            } else {
                view.removeMouseListener(controller)
                view.removeMouseMotionListener(controller)
                view.removeMouseWheelListener(controller)
            }
            field = value
        }

    private val controller = Controller()

    private val autoPanning = AutoPanning()

    var startPos: Point2D? = null

    inner class Controller : MouseAdapter() {


        override fun mousePressed(e: MouseEvent) {
            if (e.button != Button.BUTTON2) {
                return
            }
            startPos = Point2D(e.x.toDouble(), e.y.toDouble())
        }

        override fun mouseReleased(e: MouseEvent) {
            if (e.button != Button.BUTTON3) {
                autoPanning.deactivate()
            }
            startPos = null
        }

        override fun mouseDragged(e: MouseEvent) {
            if (e.button != Button.BUTTON3) {
                autoPanning.activate()
            }
            if (e.button != Button.BUTTON2) {
                return
            }
            val delta = Point2D(e.x - startPos!!.x, e.y - startPos!!.y)
            view.navigator.panBy(delta.x.toInt(), delta.y.toInt())
            startPos = Point2D(e.x.toDouble(), e.y.toDouble())
        }

        override fun mouseWheelRotated(e: MouseEvent) {
            view.navigator.multiplyZoomFactor(if (e.wheelRotation > 0) 0.9 else 1 / 0.9)
            e.consume()
        }
    }

    inner class AutoPanning : MouseAdapter() {

        private val timer: Timer = System.get().createTimer()
        private var event: MouseEvent = EmptyMouseEvent()

        init {
            timer.initialize(AUTOPAN_TIMER_DELAY, { pan() })
        }

        override fun mouseDragged(e: MouseEvent) {
            event = e
            if (isInsideSensitiveRegion(e.location)) {
                if (!timer.isRunning()) {
                    start()
                }
            } else {
                if (timer.isRunning()) {
                    stop()
                }
            }
        }

        fun activate() {
            view.addMouseMotionListener(this)
        }

        fun deactivate() {
            view.removeMouseMotionListener(this)
            stop()
        }

        private fun start() {
            timer.start()
        }

        private fun stop() {
            timer.stop()
        }

        private fun pan() {
            val dir = if (event.button == Button.BUTTON1) 1 else -1
            val panDirection = panDirection(event.location).multiply(dir * AUTOPAN_SIZE.toDouble())
            view.navigator.panBy(panDirection.x.toInt(), panDirection.y.toInt())
            view.dispatchEvent(event)
        }

        private fun isInsideSensitiveRegion(p: Point2D): Boolean {
            return p.x <= AUTOPAN_REGION || p.x > view.width - AUTOPAN_REGION
                || p.y <= AUTOPAN_REGION || p.y > view.height - AUTOPAN_REGION
        }

        private fun panDirection(p: Point2D): Vector2D {
            return Vector2D(view.width / 2 - p.x, view.height / 2 - p.y).normalize
        }
    }
}
