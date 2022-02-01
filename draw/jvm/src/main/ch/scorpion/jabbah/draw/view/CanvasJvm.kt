package ch.scorpion.jabbah.draw.view

import ch.scorpion.jabbah.base.event.*
import ch.scorpion.jabbah.base.geom.Dimension2D
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.Canvas
import ch.scorpion.jabbah.draw.View
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.Cursor
import ch.scorpion.jabbah.draw.graphics.Graphics2DJvm
import ch.scorpion.jabbah.draw.module.DrawModuleJvm
import ch.scorpion.jabbah.draw.style.*
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.MouseInfo
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.awt.event.MouseWheelEvent
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JPopupMenu
import javax.swing.SwingUtilities
import javax.swing.event.PopupMenuEvent
import javax.swing.event.PopupMenuListener
import java.awt.event.KeyEvent as AwtKeyEvent
import java.awt.event.MouseEvent as AwtMouseEvent
import java.awt.event.MouseWheelEvent as AwtMouseWheelEvent

/**
 * Implements the [Canvas] interface on the JVM platform as a [JPanel].
 *
 * @param view the [View] to be displayed by this [Canvas]
 * @param styleProvider provides the [Style] that yields the background color of this [Canvas]
 */
class CanvasJvm(
	override val view: View<*>,
	private val styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	eventBus: EventBus = BaseModule.eventBus,
	private val propertyOwner: PropertyOwner<Any> = PropertyOwnerImpl()
) : JPanel(), Canvas, PropertyOwner<Any> by propertyOwner {

	private val mouseListeners: MutableList<MouseEventBridge> by lazy { mutableListOf() }
	private val mouseMotionListeners: MutableList<MouseMotionEventBridge> by lazy { mutableListOf() }
	private val mouseWheelListeners: MutableList<MouseWheelEventBridge> by lazy { mutableListOf() }
	private val keyListeners: MutableList<KeyEventBridge> by lazy { mutableListOf() }

	private val contextMenu = JPopupMenu()

	init {
		propertyOwner.source = this
		eventBus.register(ThemeEvent::class) { installBackgroundColor() }
		installBackgroundColor()

		layout = null
		view.canvas = this
		view.initialize()

		componentPopupMenu = contextMenu
		contextMenu.addPopupMenuListener(object : PopupMenuListener {
			override fun popupMenuWillBecomeInvisible(e: PopupMenuEvent?) {}
			override fun popupMenuCanceled(e: PopupMenuEvent?) {}
			override fun popupMenuWillBecomeVisible(e: PopupMenuEvent?) {
				val mousePos = view.viewToModel(Point2D(mousePosition.getX(), mousePosition.getY()))
				DrawModuleJvm.contextMenuProvider.fillContextMenu(view, mousePos.x, mousePos.y, contextMenu)
			}
		})

		this.addComponentListener(object : ComponentAdapter() {
			override fun componentResized(e: ComponentEvent?) {
				propertyOwner.fire(Canvas.PROP_DIMENSION, dimension, dimension)
			}
		})
	}

	private fun installBackgroundColor() {
		background = Graphics2DJvm.toAwtColor(styleProvider.getStyle(StyleType.BACKGROUND).color.backgroundColor)
	}

	override val devicePixelRatio: Int = 1

	override var backgroundColor: Color
		get() = Color(background.red, background.green, backgroundColor.blue, backgroundColor.alpha)
		set(value) {
			background = java.awt.Color(value.red, value.green, value.blue, value.alpha)
		}

	override val dimension: Dimension2D
		get() = Dimension2D(width.toDouble(), height.toDouble())

	override val mouseLocation: Point2D
		get() {
			val location = MouseInfo.getPointerInfo().location
			SwingUtilities.convertPointFromScreen(location, this)
			return Point2D(location.x, location.y)
		}

	override fun requestViewFocus() {
		super.requestFocusInWindow()
	}

	override fun setCursor(cursor: Cursor) {
		when (cursor) {
			Cursor.DEFAULT -> setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR))
			Cursor.WAIT -> setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR))
			Cursor.CLICK -> setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR))
			Cursor.MOVE -> setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR))
			Cursor.CROSSHAIR -> setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.CROSSHAIR_CURSOR))
			Cursor.NW_RESIZE -> setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.NW_RESIZE_CURSOR))
			Cursor.N_RESIZE -> setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.N_RESIZE_CURSOR))
			Cursor.NE_RESIZE -> setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.NE_RESIZE_CURSOR))
			Cursor.E_RESIZE -> setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.E_RESIZE_CURSOR))
			Cursor.SE_RESIZE -> setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.SE_RESIZE_CURSOR))
			Cursor.S_RESIZE -> setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.S_RESIZE_CURSOR))
			Cursor.SW_RESIZE -> setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.SW_RESIZE_CURSOR))
			Cursor.W_RESIZE -> setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.W_RESIZE_CURSOR))
			Cursor.TEXT -> setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.TEXT_CURSOR))
		}
	}

	override fun addMouseListener(l: MouseListener) {
		var bridge = mouseEventBridgeOf(l)
		if (bridge == null) {
			bridge = MouseEventBridge(l)
			mouseListeners.add(bridge)
			super.addMouseListener(bridge)
		}
	}

	override fun removeMouseListener(l: MouseListener) {
		val bridge = mouseEventBridgeOf(l)
		if (bridge != null) {
			mouseListeners.remove(bridge)
			super.removeMouseListener(bridge)
		}
	}

	override fun addMouseMotionListener(l: MouseMotionListener) {
		var bridge = mouseMotionEventBridgeOf(l)
		if (bridge == null) {
			bridge = MouseMotionEventBridge(l)
			mouseMotionListeners.add(bridge)
			super.addMouseMotionListener(bridge)
		}
	}

	override fun removeMouseMotionListener(l: MouseMotionListener) {
		val bridge = mouseMotionEventBridgeOf(l)
		if (bridge != null) {
			mouseMotionListeners.remove(bridge)
			super.removeMouseMotionListener(bridge)
		}
	}

	override fun addMouseWheelListener(l: MouseWheelListener) {
		var bridge = mouseWheelEventBridgeOf(l)
		if (bridge == null) {
			bridge = MouseWheelEventBridge(l)
			mouseWheelListeners.add(bridge)
			super.addMouseWheelListener(bridge)
		}
	}

	override fun removeMouseWheelListener(l: MouseWheelListener) {
		val bridge = mouseWheelEventBridgeOf(l)
		if (bridge != null) {
			mouseWheelListeners.remove(bridge)
			super.removeMouseWheelListener(bridge)
		}
	}

	override fun addKeyListener(l: KeyListener) {
		var bridge = keyEventBridgeOf(l)
		if (bridge == null) {
			bridge = KeyEventBridge(l)
			keyListeners.add(bridge)
			super.addKeyListener(bridge)
		}
	}

	override fun removeKeyListener(l: KeyListener) {
		val bridge = keyEventBridgeOf(l)
		if (bridge != null) {
			keyListeners.remove(bridge)
			super.removeKeyListener(bridge)
		}
	}

	override fun dispatchEvent(e: InputEvent) {
		dispatchEvent(e.event as java.awt.event.InputEvent)
	}

	/** ---- [JComponent] */

	override fun paintComponent(g: Graphics?) {
		super.paintComponent(g)
		view.paint(Graphics2DJvm(g as Graphics2D))
	}

	/** ---- [CanvasJvm] */

	private fun mouseEventBridgeOf(l: MouseListener): MouseEventBridge? =
		mouseListeners.firstOrNull { it.listener === l }

	private fun mouseMotionEventBridgeOf(l: MouseMotionListener): MouseMotionEventBridge? =
		mouseMotionListeners.firstOrNull { it.listener === l }

	private fun mouseWheelEventBridgeOf(l: MouseWheelListener): MouseWheelEventBridge? =
		mouseWheelListeners.firstOrNull { it.listener === l }

	private fun keyEventBridgeOf(l: KeyListener): KeyEventBridge? = keyListeners.firstOrNull { it.listener === l }
}

private class MouseEventJvm(
	override val event: AwtMouseEvent
) : MouseEvent {

	override val type: MouseEventType get() = convertEventType(event.id)

	override val source: Any get() = event.source

	override val modifiers: Int get() = event.modifiersEx

	override val x: Int get() = event.x

	override val y: Int get() = event.y

	override val button: Button get() = convertButton(event.button)

	override val clickCount: Int get() = event.clickCount

	override val wheelRotation: Int get() = (event as? AwtMouseWheelEvent)?.wheelRotation ?: 0

	override val isLeftButtonDown: Boolean get() = SwingUtilities.isLeftMouseButton(event)

	override val isMiddleButtonDown: Boolean get() = SwingUtilities.isMiddleMouseButton(event)

	override val isRightButtonDown: Boolean get() = SwingUtilities.isRightMouseButton(event)

	override fun consume() {
		event.consume()
	}

	override fun isConsumed(): Boolean = event.isConsumed

	override fun toString(): String = "MouseEvent $type"

	private fun convertButton(jvmButton: Int): Button =
		when (jvmButton) {
			AwtMouseEvent.NOBUTTON -> Button.NONE
			AwtMouseEvent.BUTTON1 -> Button.BUTTON1
			AwtMouseEvent.BUTTON2 -> Button.BUTTON2
			AwtMouseEvent.BUTTON3 -> Button.BUTTON3
			else -> Button.UNKNOWN
		}

	private fun convertEventType(id: Int): MouseEventType =
		when (id) {
			AwtMouseEvent.MOUSE_CLICKED -> MouseEventType.CLICKED
			AwtMouseEvent.MOUSE_PRESSED -> MouseEventType.PRESSED
			AwtMouseEvent.MOUSE_RELEASED -> MouseEventType.RELEASED
			AwtMouseEvent.MOUSE_ENTERED -> MouseEventType.ENTERED
			AwtMouseEvent.MOUSE_EXITED -> MouseEventType.EXITED
			AwtMouseEvent.MOUSE_MOVED -> MouseEventType.MOVED
			AwtMouseEvent.MOUSE_DRAGGED -> MouseEventType.DRAGGED
			AwtMouseEvent.MOUSE_WHEEL -> MouseEventType.WHEEL_ROTATED
			else -> MouseEventType.UNKNOWN
		}
}

private class KeyEventJvm(override val event: AwtKeyEvent) : KeyEvent {
	override val type: KeyEventType get() = convertEventType(event.id)
	override val source: Any get() = event.source
	override val modifiers: Int get() = event.modifiersEx
	override val key: Int get() = event.keyCode
	override val keyChar: Char get() = event.keyChar

	override fun consume() {
		event.consume()
	}

	override fun isConsumed(): Boolean = event.isConsumed

	override fun toString(): String = "KeyEvent $type $key"

	private fun convertEventType(id: Int): KeyEventType =
		when (id) {
			AwtKeyEvent.KEY_TYPED -> KeyEventType.TYPED
			AwtKeyEvent.KEY_PRESSED -> KeyEventType.PRESSED
			AwtKeyEvent.KEY_RELEASED -> KeyEventType.RELEASED
			else -> KeyEventType.UNKNOWN
		}
}

private class KeyEventBridge(val listener: KeyListener) : java.awt.event.KeyListener {

	override fun keyTyped(e: AwtKeyEvent) { }

	override fun keyPressed(e: AwtKeyEvent) = listener.keyPressed(KeyEventJvm(e))

	override fun keyReleased(e: AwtKeyEvent) = listener.keyReleased(KeyEventJvm(e))
}

private class MouseMotionEventBridge(val listener: MouseMotionListener) : java.awt.event.MouseMotionListener {

	override fun mouseMoved(e: AwtMouseEvent) = listener.mouseMoved(MouseEventJvm(e))
	override fun mouseDragged(e: AwtMouseEvent) = listener.mouseDragged(MouseEventJvm(e))
}

private class MouseWheelEventBridge(val listener: MouseWheelListener) : java.awt.event.MouseWheelListener {

	override fun mouseWheelMoved(e: MouseWheelEvent) = listener.mouseWheelRotated(MouseEventJvm(e))
}

private class MouseEventBridge(val listener: MouseListener) : java.awt.event.MouseListener {

	override fun mouseEntered(e: AwtMouseEvent) = listener.mouseEntered(MouseEventJvm(e))

	override fun mouseClicked(e: AwtMouseEvent) {
		listener.mouseClicked(MouseEventJvm(e))
	}
	override fun mouseReleased(e: AwtMouseEvent) {
		listener.mouseReleased(MouseEventJvm(e))
	}
	override fun mouseExited(e: AwtMouseEvent) = listener.mouseExited(MouseEventJvm(e))

	override fun mousePressed(e: AwtMouseEvent) = listener.mousePressed(MouseEventJvm(e))
}
