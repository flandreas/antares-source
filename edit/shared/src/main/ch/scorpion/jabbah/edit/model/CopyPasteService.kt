package ch.scorpion.jabbah.edit.model

import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.DrawingView

/**
 * A domain service for copy/paste in a [Drawing].
 * Note that this service only deals with serialized forms of [Component]s and does NOT access the system
 * clipbard. For copy/paste involving the clipboard, use the corresponding application service.
 */
open class CopyPasteService {

	open fun copy(componentIds: Collection<Int>, drawing: Drawing<Component>): String {
		throw NotImplementedError()
	}

	open fun paste(contents: String, view: DrawingView<Drawing<Component>>): List<Component> {
		throw NotImplementedError()
	}
}