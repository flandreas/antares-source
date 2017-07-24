package ch.scorpion.jabbah.draw.view

import ch.scorpion.jabbah.base.event.Button
import ch.scorpion.jabbah.base.event.MouseAdapter
import ch.scorpion.jabbah.base.event.MouseEvent
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.draw.View
import ch.scorpion.jabbah.base.logger

/**
 * Allows the user to zoom in a [View] by using the mouse wheel and to pan in the [View] by dragging with the
 * middle mouse button.
 */
class ZoomPanController(val view: View<*>) {

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

    inner class Controller : MouseAdapter() {
        val LOG by logger(ZoomPanController::class)
        var startPos: Point2D? = null

        override fun mousePressed(e: MouseEvent) {
            if (e.button != Button.BUTTON2) {
                return
            }
            startPos = Point2D(e.x.toDouble(), e.y.toDouble())
        }

        override fun mouseReleased(e: MouseEvent) {
            startPos = null
        }

        override fun mouseDragged(e: MouseEvent) {
            if (e.button != Button.BUTTON2) {
                return
            }
            val delta = Point2D(e.x - startPos!!.x, e.y - startPos!!.y)
            view.navigator.pan(delta.x.toInt(), delta.y.toInt())
            startPos = Point2D(e.x.toDouble(), e.y.toDouble())
        }

        override fun mouseWheelRotated(e: MouseEvent) {
            view.navigator.multiplyZoomFactor(if (e.wheelRotation > 0) 0.9 else 1 / 0.9)
            e.consume()
        }
    }
}
