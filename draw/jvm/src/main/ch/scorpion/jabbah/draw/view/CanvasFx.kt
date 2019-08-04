package ch.scorpion.jabbah.draw.view

import ch.scorpion.jabbah.base.event.*
import ch.scorpion.jabbah.base.exception.UnsupportedOperationException
import ch.scorpion.jabbah.base.geom.Dimension2D
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.draw.Canvas
import ch.scorpion.jabbah.draw.InputEventContext
import ch.scorpion.jabbah.draw.View
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.Cursor
import ch.scorpion.jabbah.draw.graphics.Graphics2DFx
import javafx.event.EventHandler
import javafx.event.EventType
import javafx.scene.input.KeyEvent
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import javafx.scene.input.ScrollEvent
import javafx.scene.layout.Region
import kotlin.math.ceil

class CanvasFx(
	val canvas: javafx.scene.canvas.Canvas,
	viewFactory: (Canvas) -> View<out InputEventContext>
) : Canvas {

	companion object {
		private val LOG by logger(CanvasFx::class)
	}

	private val g = Graphics2DFx(canvas.graphicsContext2D)

	private val mouseListeners: MutableList<MouseEventBridge> by lazy { mutableListOf<MouseEventBridge>() }
	private val mouseMotionListeners: MutableList<MouseMotionEventBridge> by lazy { mutableListOf<MouseMotionEventBridge>() }
	private val mouseWheelListeners: MutableList<MouseWheelEventBridge> by lazy { mutableListOf<MouseWheelEventBridge>() }
	private val keyListeners: MutableList<KeyEventBridge> by lazy { mutableListOf<KeyEventBridge>() }

	override val view = viewFactory.invoke(this)

	override val dimension: Dimension2D get() = Dimension2D(canvas.width, canvas.height)

	/** In JavaFX, the background color is rendered by the [Region] that contains this [Canvas], and not by the [Canvas] itself.*/
	override var backgroundColor: Color = Color.WHITE

	override val mouseLocation: Point2D
		get() = throw UnsupportedOperationException("getting mouseLocation not yet implemented")

	init {
		canvas.isFocusTraversable = true
		view.initialize()
	}

	override fun requestViewFocus() {
		canvas.requestFocus()
	}

	override fun setCursor(cursor: Cursor) {
		when (cursor) {
			Cursor.DEFAULT -> canvas.cursor = javafx.scene.Cursor.DEFAULT
			Cursor.WAIT -> canvas.cursor = javafx.scene.Cursor.WAIT
			Cursor.HAND -> canvas.cursor = javafx.scene.Cursor.HAND
			Cursor.CROSSHAIR -> canvas.cursor = javafx.scene.Cursor.CROSSHAIR
			Cursor.NW_RESIZE -> canvas.cursor = javafx.scene.Cursor.NW_RESIZE
			Cursor.N_RESIZE -> canvas.cursor = javafx.scene.Cursor.N_RESIZE
			Cursor.NE_RESIZE -> canvas.cursor = javafx.scene.Cursor.NE_RESIZE
			Cursor.E_RESIZE -> canvas.cursor = javafx.scene.Cursor.E_RESIZE
			Cursor.SE_RESIZE -> canvas.cursor = javafx.scene.Cursor.SE_RESIZE
			Cursor.S_RESIZE -> canvas.cursor = javafx.scene.Cursor.S_RESIZE
			Cursor.SW_RESIZE -> canvas.cursor = javafx.scene.Cursor.SW_RESIZE
			Cursor.W_RESIZE -> canvas.cursor = javafx.scene.Cursor.W_RESIZE
			Cursor.TEXT -> canvas.cursor = javafx.scene.Cursor.TEXT
		}
	}

	override fun repaint() {
		repaint(0, 0, ceil(canvas.width).toInt(), ceil(canvas.height).toInt())
	}

	override fun repaint(x: Int, y: Int, width: Int, height: Int) {
		g.g.clearRect(0.0, 0.0, canvas.width, canvas.height)
		view.paint(g)
	}

	override fun addMouseListener(l: MouseListener) {
		LOG.debug("CanvasFx: addMouseListener")
		var bridge = mouseEventBridgeOf(l)
		if (bridge == null) {
			bridge = MouseEventBridge(l)
			mouseListeners.add(bridge)
			canvas.addEventHandler(MouseEvent.MOUSE_CLICKED, bridge)
			canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, bridge)
			canvas.addEventHandler(MouseEvent.MOUSE_RELEASED, bridge)
			canvas.addEventHandler(MouseEvent.MOUSE_ENTERED, bridge)
			canvas.addEventHandler(MouseEvent.MOUSE_EXITED, bridge)
		}
	}

	override fun removeMouseListener(l: MouseListener) {
		val bridge = mouseEventBridgeOf(l)
		if (bridge != null) {
			mouseListeners.remove(bridge)
			canvas.removeEventHandler(MouseEvent.MOUSE_CLICKED, bridge)
			canvas.removeEventHandler(MouseEvent.MOUSE_PRESSED, bridge)
			canvas.removeEventHandler(MouseEvent.MOUSE_RELEASED, bridge)
			canvas.removeEventHandler(MouseEvent.MOUSE_ENTERED, bridge)
			canvas.removeEventHandler(MouseEvent.MOUSE_EXITED, bridge)
		}
	}

	override fun addMouseMotionListener(l: MouseMotionListener) {
		var bridge = mouseMotionEventBridgeOf(l)
		if (bridge == null) {
			bridge = MouseMotionEventBridge(l)
			mouseMotionListeners.add(bridge)
			canvas.addEventHandler(MouseEvent.MOUSE_MOVED, bridge)
			canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, bridge)
		}
	}

	override fun removeMouseMotionListener(l: MouseMotionListener) {
		val bridge = mouseMotionEventBridgeOf(l)
		if (bridge != null) {
			mouseMotionListeners.remove(bridge)
			canvas.removeEventHandler(MouseEvent.MOUSE_MOVED, bridge)
			canvas.removeEventHandler(MouseEvent.MOUSE_DRAGGED, bridge)
		}
	}

	override fun addMouseWheelListener(l: MouseWheelListener) {
		LOG.info("CanvasFx.addMouseWheelListener")
		var bridge = mouseWheelEventBridgeOf(l)
		if (bridge == null) {
			bridge = MouseWheelEventBridge(l)
			mouseWheelListeners.add(bridge)
			canvas.addEventHandler(ScrollEvent.SCROLL, bridge)
		}
	}

	override fun removeMouseWheelListener(l: MouseWheelListener) {
		val bridge = mouseWheelEventBridgeOf(l)
		if (bridge != null) {
			mouseWheelListeners.remove(bridge)
			canvas.removeEventHandler(ScrollEvent.SCROLL, bridge)
		}
	}

	override fun addKeyListener(l: KeyListener) {
		var bridge = keyEventBridgeOf(l)
		if (bridge == null) {
			bridge = KeyEventBridge(l)
			keyListeners.add(bridge)
			canvas.addEventHandler(KeyEvent.KEY_PRESSED, bridge)
			canvas.addEventHandler(KeyEvent.KEY_RELEASED, bridge)
		}
	}

	override fun removeKeyListener(l: KeyListener) {
		val bridge = keyEventBridgeOf(l)
		if (bridge != null) {
			keyListeners.remove(bridge)
			canvas.removeEventHandler(KeyEvent.KEY_PRESSED, bridge)
			canvas.removeEventHandler(KeyEvent.KEY_RELEASED, bridge)
		}
	}

	override fun setToolTipText(text: String?) {
		// TODO
	}

	override fun dispatchEvent(e: InputEvent) {
		// TODO
	}

	/** ---- [CanvasFx] */

	private class MouseEventJx(override val event: MouseEvent) : ch.scorpion.jabbah.base.event.MouseEvent {

		override val type: MouseEventType get() = convertEventType(event.eventType)
		override val x: Int get() = event.x.toInt()
		override val y: Int get() = event.y.toInt()
		override val button: Button get() = convertButton()
		override val clickCount: Int get() = event.clickCount
		override val wheelRotation: Int get() = 0
		override val source: Any get() = event.source
		override val modifiers: Int get() = convertModifiers()

		override fun consume() {
			event.consume()
		}

		override fun isConsumed(): Boolean = event.isConsumed

		private fun convertButton(): Button {
			return when (event.button) {
				MouseButton.NONE, null -> Button.NONE
				MouseButton.PRIMARY -> Button.BUTTON1
				MouseButton.MIDDLE -> Button.BUTTON2
				MouseButton.SECONDARY -> Button.BUTTON3
			}
		}

		private fun convertModifiers(): Int {
			var modifiers = 0
			if (event.isShiftDown) {
				modifiers = modifiers or ch.scorpion.jabbah.base.event.SHIFT_MASK
			}
			if (event.isControlDown) {
				modifiers = modifiers or ch.scorpion.jabbah.base.event.CTRL_MASK
			}
			if (event.isMetaDown) {
				modifiers = modifiers or ch.scorpion.jabbah.base.event.META_MASK
			}
			if (event.isAltDown) {
				modifiers = modifiers or ch.scorpion.jabbah.base.event.ALT_MASK
			}

			return modifiers
		}

		private fun convertEventType(type: EventType<out MouseEvent>): MouseEventType {
			// Not used, will be removed
			return MouseEventType.MOVED
		}
	}

	private class ScrollEventJx(private val scrollEvent: ScrollEvent) : ch.scorpion.jabbah.base.event.MouseEvent {

		override val type: MouseEventType get() = convertEventType(scrollEvent.eventType)
		override val x: Int get() = scrollEvent.x.toInt()
		override val y: Int get() = scrollEvent.y.toInt()
		override val button: Button get() = Button.NONE
		override val clickCount: Int get() = 0
		override val wheelRotation: Int get() = -scrollEvent.deltaY.toInt()
		override val event: Any? get() = scrollEvent
		override val source: Any get() = scrollEvent.source
		override val modifiers: Int get() = convertModifiers()

		override fun consume() {
			scrollEvent.consume()
		}

		override fun isConsumed(): Boolean = scrollEvent.isConsumed

		private fun convertModifiers(): Int {
			var modifiers = 0
			if (scrollEvent.isShiftDown) {
				modifiers = modifiers or ch.scorpion.jabbah.base.event.SHIFT_MASK
			}
			if (scrollEvent.isControlDown) {
				modifiers = modifiers or ch.scorpion.jabbah.base.event.CTRL_MASK
			}
			if (scrollEvent.isMetaDown) {
				modifiers = modifiers or ch.scorpion.jabbah.base.event.META_MASK
			}
			if (scrollEvent.isAltDown) {
				modifiers = modifiers or ch.scorpion.jabbah.base.event.ALT_MASK
			}

			return modifiers
		}

		private fun convertEventType(type: EventType<out ScrollEvent>): MouseEventType {
			// Not used, will be removed
			return MouseEventType.MOVED
		}
	}

	private fun mouseEventBridgeOf(l: MouseListener): MouseEventBridge? = mouseListeners.firstOrNull { it.listener == l }

	private fun mouseMotionEventBridgeOf(l: MouseMotionListener): MouseMotionEventBridge? = mouseMotionListeners.firstOrNull { it.listener == l }

	private fun mouseWheelEventBridgeOf(l: MouseWheelListener): MouseWheelEventBridge? = mouseWheelListeners.firstOrNull { it.listener == l }

	private fun keyEventBridgeOf(l: KeyListener): KeyEventBridge? = keyListeners.firstOrNull { it.listener == l }

	private inner class MouseEventBridge(val listener: MouseListener) : EventHandler<MouseEvent> {
		override fun handle(event: MouseEvent?) {
			if (event != null) {
				when (event.eventType) {
					MouseEvent.MOUSE_CLICKED -> listener.mouseClicked(MouseEventJx(event))
					MouseEvent.MOUSE_PRESSED -> listener.mousePressed(MouseEventJx(event))
					MouseEvent.MOUSE_RELEASED -> listener.mouseReleased(MouseEventJx(event))
					MouseEvent.MOUSE_ENTERED -> listener.mouseEntered(MouseEventJx(event))
					MouseEvent.MOUSE_EXITED -> listener.mouseExited(MouseEventJx(event))
				}
			}
		}
	}

	private inner class MouseMotionEventBridge(val listener: MouseMotionListener) : EventHandler<MouseEvent> {
		override fun handle(event: MouseEvent?) {
			if (event != null) {
				when (event.eventType) {
					MouseEvent.MOUSE_MOVED -> listener.mouseMoved(MouseEventJx(event))
					MouseEvent.MOUSE_DRAGGED -> listener.mouseDragged(MouseEventJx(event))
				}
			}
		}
	}

	private inner class MouseWheelEventBridge(val listener: MouseWheelListener) : EventHandler<ScrollEvent> {
		override fun handle(event: ScrollEvent?) {
			if (event != null) {
				when (event.eventType) {
					ScrollEvent.SCROLL -> listener.mouseWheelRotated(ScrollEventJx(event))
				}
			}
		}
	}

	private class KeyEventFx(
		override val event: KeyEvent
	) : ch.scorpion.jabbah.base.event.KeyEvent {

		override val type: KeyEventType get() = convertEventType(event.eventType)

		override val key: Int get() = event.code.ordinal

		override val keyChar: Char get() = event.character[0]

		override val source: Any get() = event.source

		override val modifiers: Int get() = convertModifiers()

		override fun consume() {
			event.consume()
		}

		override fun isConsumed(): Boolean = event.isConsumed

		private fun convertModifiers(): Int {
			var modifiers = 0
			if (event.isShiftDown) {
				modifiers = modifiers or ch.scorpion.jabbah.base.event.SHIFT_MASK
			}
			if (event.isControlDown) {
				modifiers = modifiers or ch.scorpion.jabbah.base.event.CTRL_MASK
			}
			if (event.isMetaDown) {
				modifiers = modifiers or ch.scorpion.jabbah.base.event.META_MASK
			}
			if (event.isAltDown) {
				modifiers = modifiers or ch.scorpion.jabbah.base.event.ALT_MASK
			}

			return modifiers
		}

		private fun convertEventType(type: EventType<out KeyEvent>): KeyEventType {
			// Not used, will be removed
			return KeyEventType.TYPED
		}
	}

	private class KeyEventBridge(val listener: KeyListener) : EventHandler<KeyEvent> {
		override fun handle(event: KeyEvent?) {
			LOG.debug("KeyEvent $event")
			if (event != null) {
				val e = KeyEventFx(event)
				when (event.eventType) {
					KeyEvent.KEY_PRESSED -> listener.keyPressed(e)
					KeyEvent.KEY_RELEASED -> listener.keyReleased(e)
				}
			}
		}
	}
}

