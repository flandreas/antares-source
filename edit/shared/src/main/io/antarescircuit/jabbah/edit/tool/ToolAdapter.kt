package io.antarescircuit.jabbah.edit.tool

import io.antarescircuit.jabbah.base.Status
import io.antarescircuit.jabbah.base.StatusType
import io.antarescircuit.jabbah.base.event.KeyEvent
import io.antarescircuit.jabbah.base.event.MouseEvent
import io.antarescircuit.jabbah.edit.Tool
import io.antarescircuit.jabbah.base.event.KeyListener
import io.antarescircuit.jabbah.edit.Editor

/**
 * [ToolAdapter] is an implementation of the [Tool] interface that provides an empty implementation of all
 * interface methods and can be used as a base class for deriving concrete [Tool]s.
 */
open class ToolAdapter(editor: Editor) : AbstractTool(editor) {

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