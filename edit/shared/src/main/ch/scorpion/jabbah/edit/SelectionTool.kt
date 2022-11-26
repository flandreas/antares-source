package ch.scorpion.jabbah.edit

import ch.scorpion.jabbah.edit.select.RubberBandHandler

/**
 * A [Tool] for selecting [Component]s in a [Drawing] and for moving selected [Component]s around.
 *
 * A [SelectionTool] (like every [Tool]) is owned and controlled by an [Editor], which calls this [Tool]'s
 * event handling methods whenever it is the current [Tool] of the [Editor]. A [SelectionTool] is responsible
 * for creating and registering appropriate [Command]s when moving [Component]s.
 */
interface SelectionTool : Tool {

	override val enabledInUneditableView: Boolean get() = true

	val rubberBandHandler: RubberBandHandler
}

/** A factory that creates a [SelectionTool] for an [Editor].*/
typealias SelectionToolFactory = (Editor) -> SelectionTool
