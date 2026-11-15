package io.antarescircuit.jabbah.base.event

/**
 * Recognizes clicks from matching press/release pairs in view coordinates.
 *
 * Once a gesture reaches [dragThreshold], returning to the press location does not turn it back into a click.
 * Use an infinite threshold for controls whose entire area is a clickable target.
 */
class MouseClickRecognizer(private val dragThreshold: Double) {

	private var press: MouseEvent? = null

	var isDragging: Boolean = false
		private set

	fun reset() {
		press = null
		isDragging = false
	}

	fun mousePressed(e: MouseEvent) {
		reset()
		if (e.button != Button.NONE && e.button != Button.UNKNOWN) {
			press = e
		}
	}

	/** Commit the current press to dragging without waiting for the movement threshold. */
	fun startDragging() {
		if (press != null) isDragging = true
	}

	fun mouseDragged(e: MouseEvent): Boolean {
		val pressed = press ?: return false
		if (pressed.source !== e.source || (e.type == MouseEventType.RELEASED && pressed.button != e.button)) return false
		if (pressed.location.distance(e.location) >= dragThreshold) {
			isDragging = true
		}
		return isDragging
	}

	fun mouseReleased(e: MouseEvent): MouseEvent? {
		val pressed = press ?: return null
		if (pressed.button != e.button) return null
		mouseDragged(e)
		val click = if (!isDragging && pressed.source === e.source) {
			object : MouseEvent by e {
				override val type = MouseEventType.CLICKED
				// The release count is not available on every platform; use the corresponding press.
				override val clickCount = pressed.clickCount.coerceAtLeast(1)
			}
		} else null
		reset()
		return click
	}
}
