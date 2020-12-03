package ch.scorpion.jabbah.graph

import ch.scorpion.jabbah.base.event.*
import ch.scorpion.jabbah.base.geom.Dimension2D
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.draw.Canvas
import ch.scorpion.jabbah.draw.InputEventContext
import ch.scorpion.jabbah.draw.View
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.Cursor

/**
 * A [Canvas] implementation used for testing event processing features in common modules.
 * Contains an API for sending events to the [View] without bridging platform events to [ch.scorpion.jabbah] events.
 *
 * TODO: Move to draw package once test utility classes can be shared between Kotlin MPP gradle modules (KT-35073).
 */
class VirtualCanvas(
	viewFactory: (Canvas) -> View<out InputEventContext>,
) : Canvas {

	private val mouseListeners = mutableListOf<MouseListener>()
	private val mouseMotionListeners = mutableListOf<MouseMotionListener>()
	private val mouseWheelListeners = mutableListOf<MouseWheelListener>()
	private val keyListeners = mutableListOf<KeyListener>()

	/** ---- [Canvas] interface */

	override val view: View<*> by lazy { viewFactory.invoke(this) }

	override val dimension: Dimension2D = Dimension2D(1000, 1000)

	override var backgroundColor: Color = Color.WHITE

	override var mouseLocation: Point2D = Point2D.ZERO
		private set

	override fun requestViewFocus() { }

	override fun setCursor(cursor: Cursor) { }

	override fun repaint() { }

	override fun repaint(x: Int, y: Int, width: Int, height: Int) { }

	override fun addMouseListener(l: MouseListener) { mouseListeners.add(l) }

	override fun removeMouseListener(l: MouseListener) { mouseListeners.remove(l) }

	override fun addMouseMotionListener(l: MouseMotionListener) { mouseMotionListeners.add(l) }

	override fun removeMouseMotionListener(l: MouseMotionListener) { mouseMotionListeners.remove(l) }

	override fun addMouseWheelListener(l: MouseWheelListener) { mouseWheelListeners.add(l) }

	override fun removeMouseWheelListener(l: MouseWheelListener) { mouseWheelListeners.remove(l) }

	override fun addKeyListener(l: KeyListener) { keyListeners.add(l) }

	override fun removeKeyListener(l: KeyListener) { keyListeners.remove(l) }

	override fun setToolTipText(text: String?) { }

	override fun dispatchEvent(e: InputEvent) { }

	/** Event sending API */

	fun moveMouseTo(x: Int, y: Int, modifiers: Int = 0): VirtualCanvas {
		mouseLocation = toViewCoordinates(x, y)
		MouseEventImpl(MouseEventType.MOVED, mouseLocation.x.toInt(), mouseLocation.y.toInt(), modifiers).also { event ->
			mouseMotionListeners.forEach { it.mouseMoved(event) }
		}
		return this
	}

	fun pressMouseAt(x: Int, y: Int, modifiers: Int = 0): VirtualCanvas {
		mouseLocation = toViewCoordinates(x, y)
		MouseEventImpl(MouseEventType.PRESSED, mouseLocation.x.toInt(), mouseLocation.y.toInt(), modifiers).also { event ->
			mouseListeners.forEach { it.mousePressed(event) }
		}
		return this
	}


	fun clickMouseAt(x: Int, y: Int, modifiers: Int = 0, clickCount: Int = 1): VirtualCanvas {
		mouseLocation = toViewCoordinates(x, y)
		MouseEventImpl(MouseEventType.CLICKED, mouseLocation.x.toInt(), mouseLocation.y.toInt(), modifiers, clickCount = clickCount).also { event ->
			mouseListeners.forEach { it.mouseClicked(event) }
		}
		return this
	}

	private fun toViewCoordinates(x: Int, y: Int): Point2D {
		return view.modelToView(Point2D(x, y))
	}
}