package io.antarescircuit.antares.model.addressable

import io.antarescircuit.jabbah.edit.Command
import io.antarescircuit.jabbah.edit.DrawingView
import io.antarescircuit.jabbah.edit.Undoable
import io.antarescircuit.jabbah.edit.command.AbstractCommand
import io.antarescircuit.jabbah.graph.model.vertice.ObjectLink
import io.antarescircuit.jabbah.graph.view.GraphElementView
import io.antarescircuit.jabbah.graph.view.GraphView

/** A [Command] for clearing the contents of an [Addressable].*/
class AddressableClearCommand(
	private val drawingView: DrawingView<GraphElementView<*>, GraphView>?,
	private val link: ObjectLink<Addressable>
) : AbstractCommand("antares.command.clearMemory"), Undoable {

	private val addressable: Addressable get() = link.getLinkedObject(drawingView?.drawing?.graph)

	private var oldContents: Memory? = null

	override fun execute() {
		oldContents = addressable.memory.copy()
		addressable.clear()
	}

	override fun undo() {
		addressable.memory.replaceWith(oldContents!!)
		addressable.update()
	}
}