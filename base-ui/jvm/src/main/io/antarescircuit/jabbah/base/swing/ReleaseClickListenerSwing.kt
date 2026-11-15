package io.antarescircuit.jabbah.base.swing

import io.antarescircuit.jabbah.base.event.Button
import io.antarescircuit.jabbah.base.event.MouseClickRecognizer
import io.antarescircuit.jabbah.base.event.MouseEventImpl
import io.antarescircuit.jabbah.base.event.MouseEventType
import java.awt.Component
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent

/**
 * Recognizes mouse clicks on Swing components, even if the mouse was moved between mousePressed and mouseRelease.
 *
 * Override [targetAt] for individual list entries, table rows, tree paths, or icon regions.
 * A gesture must start and end on the same target.
 * [installOn] registers both listener interfaces so drags that return to their origin are rejected.
 */
abstract class ReleaseClickListenerSwing(
	private val clickCount: Int? = null,
	dragThreshold: Double = 5.0,
) : MouseAdapter() {

	private val recognizer = MouseClickRecognizer(dragThreshold)
	private var pressedTarget: Any? = null

	/** Implemented by subclasses with the logic to be executed when the user clicks the mouse button.*/
	protected abstract fun clicked(e: MouseEvent)

	fun installOn(component: Component) {
		if (component.mouseListeners.none { it === this }) component.addMouseListener(this)
		if (component.mouseMotionListeners.none { it === this }) component.addMouseMotionListener(this)
	}

	fun uninstallFrom(component: Component) {
		component.removeMouseListener(this)
		component.removeMouseMotionListener(this)
		reset()
	}

	fun reset() {
		recognizer.reset()
		pressedTarget = null
	}

	protected open fun targetAt(e: MouseEvent): Any? = e.component

	private fun enabledTargetAt(e: MouseEvent): Any? =
		if (e.component.isEnabled && e.component.contains(e.point)) targetAt(e) else null

	private fun sharedEvent(e: MouseEvent, type: MouseEventType) = MouseEventImpl(
		type = type,
		event = e,
		source = e.source,
		x = e.x,
		y = e.y,
		button = if (e.button == MouseEvent.NOBUTTON) Button.NONE else Button.BUTTON1,
		clickCount = e.clickCount,
		modifiers = e.modifiersEx,
	)

	/** ---- [MouseAdapter] */

	final override fun mouseClicked(e: MouseEvent) { }

	final override fun mousePressed(e: MouseEvent) {
		reset()
		if (e.button == MouseEvent.BUTTON1) {
			pressedTarget = enabledTargetAt(e)
			recognizer.mousePressed(sharedEvent(e, MouseEventType.PRESSED))
		}
	}

	final override fun mouseDragged(e: MouseEvent) {
		recognizer.mouseDragged(sharedEvent(e, MouseEventType.DRAGGED))
	}

	final override fun mouseReleased(e: MouseEvent) {
		if (e.button != MouseEvent.BUTTON1) return
		val target = pressedTarget
		val click = recognizer.mouseReleased(sharedEvent(e, MouseEventType.RELEASED))
		pressedTarget = null
		if (click != null && target != null && target == enabledTargetAt(e) &&
			(clickCount == null || click.clickCount == clickCount)) {
			clicked(e)
		}
	}

}
