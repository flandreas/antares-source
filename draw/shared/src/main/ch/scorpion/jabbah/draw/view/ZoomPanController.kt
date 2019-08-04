package ch.scorpion.jabbah.draw.view

import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.event.*
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Vector2D
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.View
import ch.scorpion.jabbah.base.time.Timer

/**
 * Allows the user to zoom in a [View] by using the mouse wheel and to pan in the [View] by dragging with the
 * middle mouse button (or by making scroll gestures on the track pad while pressing ALT).
 */
class ZoomPanController(val view: View<*>) {

    companion object {

	    /** The name of the wheel zoom step [Float] property in [Properties].*/
	    const val PROP_WHEEL_ZOOM_STEP = "view.ZoomPanController.wheelZoomStep"

	    /** The name of the wheel pan step [Float] property in [Properties].*/
	    const val PROP_WHEEL_PAN_STEP = "view.ZoomPanController.wheelPanStep"

	    private val LOG by logger(ZoomPanController::class)
        private const val AUTOPAN_TIMER_DELAY = 50
        private const val AUTOPAN_SIZE = 10
        private const val AUTOPAN_REGION = 50
	    private const val MOUSE_WHEEL_PAN_FACTOR = 5
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

	var autoPanningEnabled: Boolean
		get() = autoPanning.enabled
		set(value) { autoPanning.enabled = value }

    private val controller = Controller()

    private val autoPanning = AutoPanning()

	private var isMousePressed: Boolean = false

	private val wheelZoomStep: Double get() = BaseModule.properties.getFloat(PROP_WHEEL_ZOOM_STEP).toDouble()

	private val wheelPanStep: Int get() = BaseModule.properties.getInt(PROP_WHEEL_PAN_STEP)

    var startPos: Point2D = Point2D.ZERO

    inner class Controller : MouseAdapter() {

	    private fun isZoomOutWheelRotation(e: MouseEvent) = e.wheelRotation > 0

        private fun zoomChangeFactorFromWheelRotation(e: MouseEvent): Double {
	        if (e.wheelRotation != 0 && e.modifiers == 0) {
		        return if (isZoomOutWheelRotation(e)) 1 / wheelZoomStep else wheelZoomStep
	        }
            return 1.0
        }

	    private fun panVectorFromWheelRotation(e: MouseEvent): Point2D {
		    if (e.wheelRotation != 0) {
			    return if (e.isShiftDown) {
				    Point2D(e.wheelRotation * wheelPanStep, 0)
			    } else {
				    Point2D(0, e.wheelRotation * wheelPanStep)
			    }
		    }
		    return Point2D.ZERO
	    }

	    /** ---- [MouseAdapter] */

        override fun mousePressed(e: MouseEvent) {
		    isMousePressed = true
            if (e.button != Button.BUTTON2) {
                return
            }
            startPan(e.location)
        }

        override fun mouseReleased(e: MouseEvent) {
	        isMousePressed = false
            if (e.button != Button.BUTTON3) {
                autoPanning.deactivate()
            }
        }

        override fun mouseDragged(e: MouseEvent) {
            if (e.button != Button.BUTTON3) {
                autoPanning.activate()
            }
            if (e.button != Button.BUTTON2) {
                return
            }
	        pan(e.location)
        }

        override fun mouseWheelRotated(e: MouseEvent) {
	        LOG.trace("ZoomPanController: mouseWheelRotated by ${e.wheelRotation}, modifiers=${e.modifiers}")
	        if (isMousePressed) {
		        // Don't zoom if a mouse button (especially the middle mouse button used for panning) is down
		        return
	        }
	        if (e.modifiers == 0) {
		        view.navigator.multiplyZoomFactor(zoomChangeFactorFromWheelRotation(e))
	        } else if (e.isAltDown) {
		        startPos = Point2D.ZERO
		        pan(panVectorFromWheelRotation(e))
	        }
            e.consume()
        }

	    /** ---- [ZoomPanController] */

	    private fun startPan(pos: Point2D) {
		    startPos = pos
	    }

	    private fun pan(pos: Point2D) {
		   val  delta = pos.subtract(startPos)
		    view.navigator.panBy(delta.x.toInt(), delta.y.toInt())
		    startPos = pos
	    }
    }

    inner class AutoPanning : MouseAdapter() {

        private val timer: Timer = System.get().createTimer()
        private var event: MouseEvent = MouseEventImpl()

        var enabled: Boolean = true

        init {
            timer.initialize(AUTOPAN_TIMER_DELAY) { pan() }
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
	        if (enabled) {
		        view.addMouseMotionListener(this)
	        }
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
