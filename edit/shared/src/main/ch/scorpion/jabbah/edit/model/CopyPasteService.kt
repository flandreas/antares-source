package ch.scorpion.jabbah.edit.model

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.edit.Command
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.DrawingView

/**
 * A domain service for copy/paste in a [Drawing].
 * Note that this service only deals with serialized forms of [Component]s and does NOT access the system
 * clipboard. For copy/paste involving the clipboard, use the corresponding application service.
 */
open class CopyPasteService {

	open fun reset() {
		throw NotImplementedError()
	}

	open fun copy(componentIds: Collection<Int>, drawing: Drawing<*>): String {
		throw NotImplementedError()
	}

	/**
	 * Used by redoable [Command]s to paste serialized [contents] of [Component]s into the current
	 * [Drawing] of [view].
	 */
	open fun paste(contents: String, view: DrawingView<Drawing<Component>>, dislocation: Point2D): List<Component> {
		throw NotImplementedError()
	}

	/**
	 * Used by services that create [Command]s to paste [contents] into the [Drawing] of the specified [DrawingView].
	 * @throws IllegalArgumentException if [contents] could not be parsed, probably not valid content
	 */
	open fun paste(contents: String, view: DrawingView<Drawing<Component>>): PasteInfo {
		throw NotImplementedError()
	}

	/**
	 * Pastes [contents] into the specified [Drawing]. Redoable logic should use the other [paste] methods.
	 */
	open fun paste(contents: String, drawing: Drawing<Component>, dislocation: Point2D): List<Component> {
		throw NotImplementedError()
	}

	open fun decrementPasteCount() {
		throw NotImplementedError()
	}
}

/**
 * Encapsulates information used for undoing paste operations, especially the displacement calculated by
 * [CopyPasteService.paste] used for subsequent pasts.
 * */
class PasteInfo(
	val componentIds: List<Int>,
	val dislocation: Point2D
)
