package ch.scorpion.jabbah.edit.tool

import ch.scorpion.jabbah.base.Status
import ch.scorpion.jabbah.base.StatusType
import ch.scorpion.jabbah.base.event.KeyEvent
import ch.scorpion.jabbah.base.event.MouseEvent
import ch.scorpion.jabbah.edit.Tool
import ch.scorpion.jabbah.base.event.KeyListener
import ch.scorpion.jabbah.edit.Editor

/**
 * [ToolAdapter] is an implementation of the [Tool] interface that provides an empty implementation of all
 * interface methods and can be used as a base class for deriving concrete [Tool]s.
 */
open class ToolAdapter(val editor: Editor) : Tool {

    /** ---- [Tool] interface */

    override fun activate() {
        // empty
    }

    override fun deactivate() {
        clearPointStatus()
	    Status.set(StatusType.Tool, null)
    }

    override fun mouseClicked(e: MouseEvent, x: Double, y: Double) {
        // empty
    }

    override fun mousePressed(e: MouseEvent, x: Double, y: Double) {
        // empty
    }

    override fun mouseReleased(e: MouseEvent, x: Double, y: Double) {
        // empty
    }

    override fun mouseMoved(e: MouseEvent, x: Double, y: Double) {
	    reportPointStatus(x, y)
    }

    override fun mouseDragged(e: MouseEvent, x: Double, y: Double) {
        // empty
    }

    /** ---- [KeyListener] interface */

    override fun keyTyped(e: KeyEvent) {
        // empty
    }

    override fun keyPressed(e: KeyEvent) {
        // empty
    }

    override fun keyReleased(e: KeyEvent) {
        // empty
    }

	/** ---- [ToolAdapter] */

	protected fun reportPointStatus(x: Double, y: Double) {
		Status.set(StatusType.Small, "${x.toInt()},${y.toInt()}")
	}

	private fun clearPointStatus() {
		Status.set(StatusType.Small, null)
	}
}