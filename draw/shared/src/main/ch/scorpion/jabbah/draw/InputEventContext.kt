package ch.scorpion.jabbah.draw

import ch.scorpion.jabbah.base.event.KeyEvent
import ch.scorpion.jabbah.base.event.MouseEvent

/**
 * An argument object for [InputEventHandler]s.
 */
open class InputEventContext(
    val view: View<*>,
    val mouseEvent: MouseEvent? = null,
    val keyEvent: KeyEvent? = null,
    val x: Double = 0.0,
    val y: Double = 0.0)