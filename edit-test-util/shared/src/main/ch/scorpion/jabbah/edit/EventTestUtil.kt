package ch.scorpion.jabbah.edit

import ch.scorpion.jabbah.base.event.Button
import ch.scorpion.jabbah.base.event.KeyEvent
import ch.scorpion.jabbah.base.event.KeyEventImpl
import ch.scorpion.jabbah.base.event.KeyEventType
import ch.scorpion.jabbah.base.event.MouseEvent
import ch.scorpion.jabbah.base.event.MouseEventImpl
import ch.scorpion.jabbah.base.event.MouseEventType
import ch.scorpion.jabbah.base.geom.Point2D

object EventTestUtil {

    fun moveMouseTo(p: Point2D, modifiers: Int = 0): MouseEvent = moveMouseTo(p.x.toInt(), p.y.toInt(), modifiers)

    fun moveMouseTo(x: Int, y: Int, modifiers: Int = 0): MouseEvent {
        return MouseEventImpl(type = MouseEventType.MOVED, x = x, y = y, modifiers = modifiers)
    }

    fun dragMouseTo(p: Point2D, modifiers: Int = 0): MouseEvent = dragMouseTo(p.x.toInt(), p.y.toInt(), modifiers)

    fun dragMouseTo(x: Int, y: Int, modifiers: Int = 0): MouseEvent {
        return MouseEventImpl(type = MouseEventType.DRAGGED, button = Button.BUTTON1, x = x, y = y, modifiers = modifiers)
    }

    fun pressMouseAt(p: Point2D, modifiers: Int = 0): MouseEvent = pressMouseAt(p.x.toInt(), p.y.toInt(), modifiers)

    fun pressMouseAt(x: Int, y: Int, modifiers: Int = 0): MouseEvent {
        return MouseEventImpl(type = MouseEventType.PRESSED, button = Button.BUTTON1, x = x, y = y, modifiers = modifiers)
    }

    fun releaseMouseAt(p: Point2D, modifiers: Int = 0): MouseEvent = releaseMouseAt(p.x.toInt(), p.y.toInt(), modifiers)

    fun releaseMouseAt(x: Int, y: Int, modifiers: Int = 0): MouseEvent {
        return MouseEventImpl(type = MouseEventType.RELEASED, x = x, y = y, modifiers = modifiers)
    }

    fun clickMouseAt(p: Point2D, modifiers: Int = 0): MouseEvent = clickMouseAt(p.x.toInt(), p.y.toInt(), modifiers)

    fun clickMouseAt(x: Int, y: Int, modifiers: Int = 0): MouseEvent {
        return MouseEventImpl(type = MouseEventType.CLICKED, x = x, y = y, modifiers = modifiers)
    }

    fun pressKey(key: Int, modifiers: Int = 0): KeyEvent {
        return KeyEventImpl(type = KeyEventType.PRESSED, keyChar = ' ', key = key, modifiers = modifiers)
    }
}