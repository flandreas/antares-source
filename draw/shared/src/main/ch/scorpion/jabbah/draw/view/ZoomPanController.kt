package ch.scorpion.jabbah.draw.view

import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.event.*
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.View
import ch.scorpion.jabbah.draw.ZoomStrategy

/**
 * Allows the user to zoom in a [View] by using the mouse wheel and to pan in the [View] by dragging with the
 * middle mouse button (or by making scroll gestures on the track pad while pressing ALT).
 */
class ZoomPanController(val view: View<*>) {

	companion object {

		/** The name of the 'wheel zoom step' [Float] property in [Properties].*/
		const val PROP_WHEEL_ZOOM_STEP = "view.ZoomPanController.wheelZoomStep"

		/** The name of the 'wheel zoom requires meta' [Boolean] property in [Properties]*/
		const val PROP_WHEEL_ZOOM_REQUIRES_META = "view.ZoomPanController.wheelZoomRequiredMeta"

		/** The name of the 'wheel pan step' [Float] property in [Properties].*/
		const val PROP_WHEEL_PAN_STEP = "view.ZoomPanController.wheelPanStep"
	}

	var enabled: Boolean = false
		set(value) {
			println("enabled = $value")
			if (value) {
				view.addMouseListener(mouseController)
				view.addMouseMotionListener(mouseController)
				view.addMouseWheelListener(mouseController)
				view.addKeyListener(mouseWheelModeController)
			} else {
				view.removeMouseListener(mouseController)
				view.removeMouseMotionListener(mouseController)
				view.removeMouseWheelListener(mouseController)
				view.removeKeyListener(mouseWheelModeController)
			}
			field = value
		}

	var autoPanningEnabled: Boolean
		get() = autoPanning.enabled
		set(value) {
			autoPanning.enabled = value
		}

	private val mouseController = MouseController()

	private val mouseWheelModeController = MouseWheelModeController()

	private val autoPanning = AutoPanning(view)

	private var isMousePressed: Boolean = false

	private val wheelZoomStep: Double get() = BaseModule.properties.getFloat(PROP_WHEEL_ZOOM_STEP).toDouble()

	private val wheelPanStep: Int get() = BaseModule.properties.getInt(PROP_WHEEL_PAN_STEP)

	private val wheelZoomRequiresMeta: Boolean get() = BaseModule.properties.getBoolean(PROP_WHEEL_ZOOM_REQUIRES_META)

	private var startPos: Point2D = Point2D.ZERO

	private fun startPan(pos: Point2D) {
		startPos = pos
	}

	private fun pan(pos: Point2D) {
		val delta = pos.subtract(startPos)
		view.navigator.panBy(delta.x.toInt(), delta.y.toInt())
		startPos = pos
	}

	private inner class MouseController : MouseAdapter() {

		private fun isZoomOutWheelRotation(e: MouseEvent) = e.wheelRotation > 0

		private fun isZoomWithMetaIfRequired(e: MouseEvent) = e.isMetaDown || !wheelZoomRequiresMeta

		private fun getZoomChangeFactorFromWheelRotation(e: MouseEvent): Double {
			if (e.wheelRotation != 0) {
				return if (isZoomOutWheelRotation(e)) 1 / wheelZoomStep else wheelZoomStep
			}
			return 1.0
		}

		private fun getPanVectorFromWheelRotation(e: MouseEvent): Point2D {
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

		override fun mouseDragged(e: MouseEvent) {
			if (e.isLeftButtonDown || e.isMiddleButtonDown) {
				autoPanning.activate()
			}
			if (!e.isMiddleButtonDown) {
				return
			}
			pan(e.location)
		}

		override fun mouseReleased(e: MouseEvent) {
			isMousePressed = false
			if (e.button != Button.BUTTON3) {
				autoPanning.deactivate()
			}
			if (e.isMiddleButtonDown) {
				view.zoomStrategy = ZoomStrategy.NONE
			}
		}

		override fun mouseWheelRotated(e: MouseEvent) {
			if (isMousePressed) {
				// Don't zoom if a mouse button (especially the middle mouse button used for panning) is down
				return
			}
			if (!isZoomWithMetaIfRequired(e)) {
				return
			}

			// Set zoomStrategy BEFORE changing the zoomFactor to avoid switching mode in GraphFrame
			view.zoomStrategy = ZoomStrategy.NONE

			if (mouseWheelModeController.calculateIsWheelZoom()) {
				zoomByWheel(e)
			} else {
				panByWheel(e)
			}

			e.consume()
		}

		private fun panByWheel(e: MouseEvent) {
			startPos = Point2D.ZERO
			pan(getPanVectorFromWheelRotation(e))
			mouseWheelModeController.updateMouseWheelPanTime()
		}

		private fun zoomByWheel(e: MouseEvent) {
			view.navigator.multiplyZoomFactor(getZoomChangeFactorFromWheelRotation(e), e.location)
			mouseWheelModeController.updateMouseWheelZoomTime()
		}
	}

	/**
	 * Functionality to avoid that zoom/pan mode switches occur when the user presses/releases
	 * the ALT key while auto-repeated mouse wheel events are still coming in. "Auto-repeat" refers
	 * to scroll animation fade-outs either from mouse wheels or from trackpad gestures.
	 *
	 * The JVM does not provide the information whether such events are auto-repeated, so use a
	 * custom time-based approach to determine that.
	 */
	private class MouseWheelModeController : KeyAdapter() {

		companion object {

			/**
			 * The time (in ms) after the last mouse wheel rotation event to accept ALT keys for
			 * switching between zooming and panning.
			 */
			private const val WHEEL_COOL_DOWN = 200
		}

		private var lastMouseWheelZoomTime = 0L
		private var lastMouseWheelPanTime = 0L
		private var isAltDown = false
		private var isWheelZoom = true

		fun calculateIsWheelZoom(): Boolean {
			val now = System.currentTimeMillis()
			val switchMode =
				isWheelZoom && isAltDown && now - lastMouseWheelZoomTime > WHEEL_COOL_DOWN
					|| !isWheelZoom && !isAltDown && now - lastMouseWheelPanTime > WHEEL_COOL_DOWN

			isWheelZoom = isWheelZoom && !switchMode || !isWheelZoom && switchMode

			return isWheelZoom
		}

		fun updateMouseWheelZoomTime() {
			lastMouseWheelZoomTime = System.currentTimeMillis()
		}

		fun updateMouseWheelPanTime() {
			lastMouseWheelPanTime = System.currentTimeMillis()
		}

		override fun keyPressed(e: KeyEvent) {
			if (e.key == KeyEvent.VK_ALT) {
				isAltDown = true
			}
		}

		override fun keyReleased(e: KeyEvent) {
			if (e.key == KeyEvent.VK_ALT) {
				isAltDown = false
			}
		}
	}
}
