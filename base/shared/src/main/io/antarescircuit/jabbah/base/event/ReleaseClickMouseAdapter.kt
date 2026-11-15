package io.antarescircuit.jabbah.base.event

/**
 * Delivers logical clicks on release rather than relying on native CLICKED events. Subclasses must
 * register this adapter for both mouse and mouse-motion events. Release always precedes the logical
 * click, and remains available for drag cleanup even when no click is recognized.
 */
open class ReleaseClickMouseAdapter(dragThreshold: Double) : MouseAdapter() {

	private val recognizer = MouseClickRecognizer(dragThreshold)
	private var generation = 0

	/** Cancel pending gestures when changing tools, modes, or canvases. */
	fun resetMouseGesture() {
		recognizer.reset()
		generation++
	}

	/** Deliver subsequent drag events immediately and suppress the logical click for this press. */
	protected fun startDragging() {
		recognizer.startDragging()
	}

	/** Empty-implemented method to be overwritten by subclasses to implement business logic. */

	protected open fun onMousePressed(e: MouseEvent) { }
	protected open fun onMouseDragged(e: MouseEvent) { }
	protected open fun onMouseReleased(e: MouseEvent) { }
	protected open fun onMouseClicked(e: MouseEvent) { }

	/**
	 * [MouseAdapter] method implementations that call [onMouseClicked] even if mouse pressed and
	 * mouse released didn't happen at the same location.
	 */

	final override fun mouseClicked(e: MouseEvent) { }

	final override fun mousePressed(e: MouseEvent) {
		recognizer.mousePressed(e)
		onMousePressed(e)
	}

	final override fun mouseDragged(e: MouseEvent) {
		if (recognizer.mouseDragged(e)) onMouseDragged(e)
	}

	final override fun mouseReleased(e: MouseEvent) {
		val wasDragging = recognizer.isDragging
		val releaseStartsDrag = recognizer.mouseDragged(e) && !wasDragging
		val click = recognizer.mouseReleased(e)
		val releaseGeneration = generation

		// Coalesced motion may first cross the threshold in the release event itself.
		if (releaseStartsDrag) {
			onMouseDragged(object : MouseEvent by e { override val type = MouseEventType.DRAGGED })
		}

		onMouseReleased(e)

		if (click != null && generation == releaseGeneration && !e.isEventConsumed()) {
			onMouseClicked(click)
		}
	}

}
