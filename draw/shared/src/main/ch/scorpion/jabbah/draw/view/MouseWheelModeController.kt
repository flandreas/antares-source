package ch.scorpion.jabbah.draw.view

import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.event.KeyAdapter
import ch.scorpion.jabbah.base.event.KeyEvent
import ch.scorpion.jabbah.draw.module.DrawModule

/**
 * Functionality to avoid that zoom/pan mode switches occur when the user presses/releases
 * the ALT key while auto-repeated mouse wheel events are still coming in. "Auto-repeat" refers
 * to scroll animation fade-outs either from mouse wheels or from trackpad gestures.
 *
 * The JVM does not provide the information whether such events are auto-repeated, so use a
 * custom time-based approach to determine that.
 */
class MouseWheelModeController : KeyAdapter() {

	companion object {

		/**
		 * The time (in ms) after the last mouse wheel rotation event to accept ALT keys for
		 * switching between zooming and panning.
		 */
		private const val WHEEL_COOL_DOWN = 200
	}

	private var lastMouseWheelZoomTime = 0L
	private var lastMouseWheelPanTime = 0L
	private var isModifierDown = false

	private var isWheelZoom = true

	var pressedKeyCode: Int? = null
		private set

	fun reset() {
		isModifierDown = false
		isWheelZoom = true
		lastMouseWheelPanTime = 0L
		lastMouseWheelPanTime = 0L
	}

	fun calculateIsWheelZoom(): Boolean {
		val now = System.currentTimeMillis()
		val switchMode =
			isWheelZoom && isModifierDown && now - lastMouseWheelZoomTime > WHEEL_COOL_DOWN
				|| !isWheelZoom && !isModifierDown && now - lastMouseWheelPanTime > WHEEL_COOL_DOWN

		isWheelZoom = isWheelZoom && !switchMode || !isWheelZoom && switchMode

		return isWheelZoom
	}

	fun updateMouseWheelZoomTime() {
		lastMouseWheelZoomTime = System.currentTimeMillis()
	}

	fun updateMouseWheelPanTime() {
		lastMouseWheelPanTime = System.currentTimeMillis()
	}

	/** ---- [KeyAdapter] */

	override fun keyPressed(e: KeyEvent) {
		pressedKeyCode = e.key
		if (e.key == DrawModule.mouseWheelPanModifier) {
			isModifierDown = true
		}
	}

	override fun keyReleased(e: KeyEvent) {
		pressedKeyCode = null
		if (e.key == DrawModule.mouseWheelPanModifier) {
			isModifierDown = false
		}
	}
}