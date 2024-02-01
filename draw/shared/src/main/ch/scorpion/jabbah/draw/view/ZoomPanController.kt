package ch.scorpion.jabbah.draw.view

import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.base.event.*
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.View
import ch.scorpion.jabbah.draw.ZoomStrategy

/**
 * Allows the user to zoom in a [View] by using the mouse wheel and to pan in the [View] using
 * the [CurrentPanMethod] (or by making scroll gestures on the track pad while pressing ALT).
 */
class ZoomPanController(
	val view: View<*>,
	private val eventBus: EventBus = BaseModule.eventBus
) {

	companion object {

		private val LOG by logger(ZoomPanController::class)

		/** The name of the 'wheel zoom step' [Float] property in [Properties].*/
		const val PROP_WHEEL_ZOOM_STEP = "view.ZoomPanController.wheelZoomStep"

		/** The name of the 'wheel zoom requires meta' [Boolean] property in [Properties]*/
		const val PROP_WHEEL_ZOOM_REQUIRES_META = "view.ZoomPanController.wheelZoomRequiredMeta"

		/** The name of the 'wheel pan step' [Float] property in [Properties].*/
		const val PROP_WHEEL_PAN_STEP = "view.ZoomPanController.wheelPanStep"
	}

	var enabled: Boolean = false
		set(value) {
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

	/** Controls switching between "Pan" and "Zoom" according to the [CurrentPanMethod]. */
	private val mouseWheelModeController = MouseWheelModeController()

	private val autoPanning = AutoPanning(view)

	private var isMousePressed: Boolean = false

	private val wheelZoomStep: Double get() = BaseModule.properties.getFloat(PROP_WHEEL_ZOOM_STEP).toDouble()

	private val wheelPanStep: Int get() = BaseModule.properties.getInt(PROP_WHEEL_PAN_STEP)

	private val wheelZoomRequiresMeta: Boolean get() = BaseModule.properties.getBoolean(PROP_WHEEL_ZOOM_REQUIRES_META)

	private var startPos: Point2D = Point2D.ZERO

	/**
	 * Reset [MouseWheelModeController] to compensate for ALT key release events not arriving if
	 * ALT-doubleClick leads to a change of the active view.
	 */
	private val activeViewChangeHandler: EventHandler<ActiveContentViewChangedEvent> = {
		LOG.trace("activeContentViewChanged, resetting mouseWheelModeController")
		mouseWheelModeController.reset()
	}

	init {
		eventBus.register(ActiveContentViewChangedEvent::class, activeViewChangeHandler)
	}

	fun dispose() {
		eventBus.unregister(activeViewChangeHandler)
	}

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
					Point2D(-e.wheelRotation * wheelPanStep, 0)
				} else {
					Point2D(0, -e.wheelRotation * wheelPanStep)
				}
			}
			return Point2D.ZERO
		}

		/** ---- [MouseAdapter] */

		override fun mousePressed(e: MouseEvent) {
			isMousePressed = true
			if (!CurrentPanMethod.panMethod.isActivatedByPressed(e)) {
				return
			}
			startPan(e.location)
		}

		override fun mouseDragged(e: MouseEvent) {
			if (e.isLeftButtonDown || e.isMiddleButtonDown) {
				autoPanning.activate()
			}
			if (!CurrentPanMethod.panMethod.isActivatedByPressed(e)) {
				return
			}
			pan(e.location)
		}

		override fun mouseReleased(e: MouseEvent) {
			isMousePressed = false
			if (e.button != Button.BUTTON3) {
				autoPanning.deactivate()
			}
			if (CurrentPanMethod.panMethod.isActivatedByPressed(e)) {
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

			e.consumeEvent()
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
}
