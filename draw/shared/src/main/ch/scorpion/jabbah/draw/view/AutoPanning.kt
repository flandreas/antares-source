package ch.scorpion.jabbah.draw.view

import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.event.MouseAdapter
import ch.scorpion.jabbah.base.event.MouseEvent
import ch.scorpion.jabbah.base.event.MouseEventImpl
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Vector2D
import ch.scorpion.jabbah.base.time.Timer
import ch.scorpion.jabbah.draw.View

/**
 * Automatically performs a pan operation when the user drags the mouse
 * near the border of the [View].
 */
class AutoPanning(private val view: View<*>) : MouseAdapter() {

	companion object {
		private const val AUTO_PAN_TIMER_DELAY = 50
		private const val AUTO_PAN_SIZE = 10
		private const val AUTO_PAN_REGION = 50
	}

	private val timer: Timer = System.createTimer()
	private var event: MouseEvent = MouseEventImpl()

	var enabled: Boolean = true

	init {
		timer.initialize(AUTO_PAN_TIMER_DELAY) { pan() }
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
		val dir = if (event.isLeftButtonDown) 1 else -1
		val panDirection = panDirection(event.location).multiply(dir * AUTO_PAN_SIZE.toDouble())
		view.navigator.panBy(panDirection.x.toInt(), panDirection.y.toInt())
		view.dispatchEvent(event)
	}

	private fun isInsideSensitiveRegion(p: Point2D): Boolean =
		p.x <= AUTO_PAN_REGION || p.x > view.width - AUTO_PAN_REGION
			|| p.y <= AUTO_PAN_REGION || p.y > view.height - AUTO_PAN_REGION

	private fun panDirection(p: Point2D): Vector2D =
		Vector2D(view.width / 2 - p.x, view.height / 2 - p.y).normalize
}